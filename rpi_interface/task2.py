#!/usr/bin/env python3
from picamera import PiCamera
import json
import queue
import time
from multiprocessing import Process, Manager
from typing import Optional
import os
import requests
from communication.android import AndroidLink, AndroidMessage
from communication.stm32 import STMLink
from logger.logger import logger
from settings import IMG_API_IP, IMG_API_PORT, ALGO_API_IP, ALGO_API_PORT
import sys

LEFT_ARROW = 39
RIGHT_ARROW = 38
BULLSEYE = -1
FAILED = -999

marker_map = {
    LEFT_ARROW: "left",
    RIGHT_ARROW: "right",
    BULLSEYE: "bullesye",
    FAILED: "failed",
}


class PiAction:
    """
    Wrapper for an action to be executed.

    Takes in a key-value pair of category and context dependent value
    """

    def __init__(self, cat, value):
        """
        :param cat: The category of the action. Can be 'info', 'mode', 'path', 'snap', 'obstacle', 'location'
        :param value: The value of the action. Can be a string, a list of coordinates, or a list of obstacles.
        """
        self._cat = cat
        self._value = value

    @property
    def cat(self):
        return self._cat

    @property
    def value(self):
        return self._value


class RaspberryPi:
    """
    Class that defines how rpi handles communication between other components.

    It contains many large monolithic methods that may require refactoring if time permits

    Divide the following tasks into parallel processes:
    - listen msg from android
    - send msg to android
    - listen msg from stm
    - process movement command
    - process actions for image rec: snap an image
    """

    def __init__(self):
        """
        Initialises the Raspberry Pi and multiprocessing Environment.
        """

        # ======= init env ========
        self.logger = logger
        self.android_link = AndroidLink()
        self.stm_link = STMLink()
        # =============================

        # ========== multi-processing env ==================

        # Managers provide a way to create data which can be shared between
        # different processes. A manager object controls a
        # server process which manages shared objects. Other processes can
        # access the shared objects by using proxies.

        self.manager = Manager()  # manages shared resources

        self.android_dropped = self.manager.Event()  # if android disconnects

        self.unpause = self.manager.Event()

        # self.movement_lock = self.manager.Lock()
        self.movement_lock = self.manager.Lock()

        self.android_queue = self.manager.Queue()  # Messages to send to Android

        # Messages that need to be processed by RPi
        self.rpi_action_queue = self.manager.Queue()

        # Messages that need to be processed by STM32, as well as snap commands
        self.command_queue = self.manager.Queue()

        # X,Y,D coordinates of the robot after execution of a command
        # D: direction
        #
        #        NORTH - UP - 0
        #        EAST - RIGHT - 2
        #        SOUTH - DOWN - 4
        #        WEST - LEFT 6

        self.path_queue = self.manager.Queue()
        # ======================================================

        # ============= child processes ====================
        # self.proc_recv_android = None  # listens incoming msgs from android
        self.proc_recv_stm32 = None  # listens incoming msgs from stm
        self.proc_android_sender = None
        self.proc_command_follower = None  # movement command
        self.proc_rpi_action = None  # snap images
        # =================================================

    def start(self):
        try:
            # ========= init =========================
            self.android_link.connect()
            self.android_queue.put(
                AndroidMessage("info", "welcome message: connected to rpi")
            )
            self.stm_link.connect()
            # Check whether image recognition and algorithm API server is up and running
            self.check_api()
            # ======================================

            # Set the defined class methods as a parallel process
            # The class methods are defined at the end
            self.proc_recv_android = Process(target=self.recv_android)
            self.proc_recv_stm32 = Process(target=self.recv_stm)
            self.proc_android_sender = Process(target=self.android_sender)
            self.proc_command_follower = Process(target=self.command_follower)
            self.proc_rpi_action = Process(target=self.rpi_action)

            # Start child processes
            self.proc_recv_android.start()
            self.proc_recv_stm32.start()
            self.proc_android_sender.start()
            self.proc_command_follower.start()
            self.proc_rpi_action.start()

            self.logger.info("Child Processes started.")
            # Send success message to Android
            self.android_queue.put(AndroidMessage("info", "Robot is ready!"))
            self.android_queue.put(AndroidMessage("mode", "path"))
            self.reconnect_android()

        except KeyboardInterrupt:
            self.stop()

    def stop(self):
        """Stops all processes on the RPi and disconnects with Android and STM32"""
        self.android_link.disconnect()
        self.logger.info("Android disconnected.")
        self.stm_link.disconnect()
        self.logger.info("STM board disconnected.")
        self.logger.info("Program exited!")

    def reconnect_android(self):
        """Handles the reconnection to Android in the event of a lost connection."""
        self.logger.info("Reconnection handler is watching...")

        while True:
            # Wait for android connection to drop
            self.android_dropped.wait()

            self.logger.error("Android link is down!")

            # Kill child processes
            self.logger.debug("Killing android child processes")
            self.proc_android_sender.kill()
            self.proc_recv_android.kill()

            # Wait for the child processes to finish
            self.proc_android_sender.join()
            self.proc_recv_android.join()
            assert self.proc_android_sender.is_alive() is False
            assert self.proc_recv_android.is_alive() is False
            self.logger.debug("Android child processes killed")

            # Clean up old sockets
            self.android_link.disconnect()

            # Reconnect
            self.android_link.connect()

            # Recreate Android processes
            self.proc_recv_android = Process(target=self.recv_android)
            self.proc_android_sender = Process(target=self.android_sender)

            # Start previously killed processes
            self.proc_recv_android.start()
            self.proc_android_sender.start()

            self.logger.info("Android child processes restarted")
            self.android_queue.put(AndroidMessage("info", "You are reconnected!"))
            self.android_queue.put(AndroidMessage("mode", "path"))

            self.android_dropped.clear()

    def recv_android(self) -> None:
        """
        [Child Process] Processes the messages received from Android
        """

        def validate_integrity(msg_str):
            try:
                return json.loads(msg_str)
            except json.JSONDecodeError:
                self.logger.warning(f"This message is not a JSON: {msg_str!r}")
                return None

        while True:
            msg_str: Optional[str] = None
            try:
                msg_str = self.android_link.recv()
                self.logger.debug(f"Recevied raw: {msg_str!r}")
            except OSError:
                self.android_dropped.set()
                self.logger.debug("Event set: Android connection dropped")

            if msg_str is None:
                self.logger.debug("in recv_android: msg is none")
                continue

            message = validate_integrity(msg_str)
            if message is None:
                self.logger.debug("in recv_android: msg is none or corrupted")
                continue

            if message["cat"] == "control":
                if message["value"] == "start":

                    if not self.check_api():
                        self.logger.error(
                            "Image / Algo API is down! Start command aborted."
                        )
                        self.android_queue.put(
                            AndroidMessage(
                                "error",
                                "Image / Algo API is down, start command aborted.",
                            )
                        )
                        continue

                    obs1_first_marker = self.snap_and_rec("small")
                    self.logger.info(
                        f"Small obstacle first manouver is {marker_map.get(obs1_first_marker)}"
                    )

                    self.logger.info("Start command received, starting robot.")
                    self.android_queue.put(AndroidMessage("status", "running"))
                    self.unpause.set()

    def recv_stm(self) -> None:
        """
        [Child Process] Receive acknowledgement messages from STM32, and release the movement lock
        """
        while True:
            message: str = self.stm_link.recv()
            self.logger.debug(f"Received {message}")
            if not message:
                continue

            if message.startswith("DONE"):
                self.logger.debug("Acknowledgement 'DONE' from STM32 received.")

                # Release the movement lock safely
                try:
                    self.movement_lock.release()
                    self.logger.debug("movement_lock released (STM DONE).")
                except Exception:
                    self.logger.warning(
                        "movement_lock was already released — ignoring duplicate DONE."
                    )

            else:
                self.logger.warning(f"Ignored unknown message from STM: {message}")

    def android_sender(self) -> None:
        """
        [Child process] Responsible for retrieving messages from android_queue and sending them over the Android link.
        """
        while True:
            try:
                # Retrieve message from message queue
                message: AndroidMessage = self.android_queue.get(
                    timeout=0.5
                )  # blocking, up to 0.5 seconds
            except queue.Empty:
                continue

            try:
                self.android_link.send(message)  # sends message to android
            except OSError:
                self.android_dropped.set()  # check for disconnect
                self.logger.debug("Event set: Android dropped")

    def command_follower(self) -> None:
        """
        [Child Process] processes commands in command_queue.

        Three types of commands:
        - movement commands to send to STM32
        - "snapping" an image
        - finish command signaling completion of path
        """
        while True:
            # Retrieve next movement command
            command: str = self.command_queue.get()

            # Wait until start/unpause signal
            self.logger.debug("Waiting for unpause signal...")
            self.unpause.wait()

            # Acquire lock before sending command to STM32
            self.logger.debug("Acquiring movement_lock before sending command...")
            self.movement_lock.acquire()

            # STM32 Commands - Send straight to STM32
            stm32_prefixes = (
                "A",
                "C",
                "R",
                "W",
                "S",
                "D",
                "Z",
                "Q",
                "E",
                "X",
                "P",
            )
            if command.startswith(stm32_prefixes) and not command.startswith("SNAP"):
                # recv_stm is running as an independent child process
                self.stm_link.send(command)
                self.logger.debug(f"Sending to STM32: {command}")
                self.logger.info("Waiting for STM32 ack to release movement lock...")

            # Snap command releases lock once its done
            elif command.startswith("SNAP"):
                obstacle_id_with_signal = command.replace("SNAP", "")
                self.rpi_action_queue.put(
                    PiAction(cat="snap", value=obstacle_id_with_signal)
                )

            # End of path
            elif command == "FIN":

                self.unpause.clear()
                self.movement_lock.release()

                self.logger.info("Commands queue finished.")
                self.android_queue.put(
                    AndroidMessage("info", "Commands queue finished.")
                )
                self.android_queue.put(AndroidMessage("status", "finished"))

            else:
                raise Exception(f"Unknown command: {command}")

    def rpi_action(self):
        while True:
            action: PiAction = self.rpi_action_queue.get()
            self.logger.debug(
                f"PiAction retrieved from queue: {action.cat} {action.value}"
            )
            if action.cat == "snap":
                self.snap_and_rec(name=action.value)

    def snap_and_rec(self, name: str) -> int:
        """
        Captures an image and sends it to the image recognition API.
        Returns the predicted marker ID if successful and valid; otherwise, returns FAILED.

        :param name: Identifier for the obstacle (used in filename and logs)
        :return: int - marker ID or FAILED
        """

        predicted_id = FAILED
        valid_markers = {BULLSEYE, LEFT_ARROW, RIGHT_ARROW}

        try:
            self.logger.info(f"Capturing image for obstacle: {name}")
            filename = f"{int(time.time())}_{name}.jpg"
            url = f"http://{IMG_API_IP}:{IMG_API_PORT}/image"

            with PiCamera() as camera:
                camera.resolution = (800, 800)
                camera.start_preview()
                time.sleep(0.5)
                camera.capture(filename)
                self.logger.info(f"Image captured: {filename}")

            try:
                with open(filename, "rb") as f:
                    response = requests.post(url, files={"file": f})
                results = json.loads(response.content)
            except Exception as e:
                self.logger.error(f"Error calling image-rec API: {e}")
                return FAILED

            if results:
                try:
                    predicted_id = int(results.get("predicted_id", -999))
                except (KeyError, ValueError) as e:
                    self.logger.error(f"Malformed response from API: {results} - {e}")
                    predicted_id = FAILED

                if predicted_id in valid_markers:
                    self.logger.info(
                        f"{name} obstacle marker successfully recognised, predicted ID: {predicted_id}, direction: {marker_map.get(predicted_id)}."
                    )
                    self.logger.info(f"Image recognition results: {results}")
                    self.android_queue.put(AndroidMessage("image-rec", results))
                else:
                    self.logger.info(f"{name} obstacle marker failed to be recognised")
                    predicted_id = FAILED

        except Exception as e_outer:
            self.logger.error(f"Unexpected error during snap_and_rec: {e_outer}")
            predicted_id = FAILED

        finally:
            self.logger.info("Snap completed, releasing movement lock")
            try:
                self.movement_lock.release()
            except Exception:
                self.logger.warning("Lock already released")

        return predicted_id

    def clear_queues(self):
        """Clear both command and path queues"""
        while not self.command_queue.empty():
            self.command_queue.get()
        while not self.path_queue.empty():
            self.path_queue.get()

    def check_api(self) -> bool:
        """Check whether image recognition and algorithm API server is up and running

        Returns:
            bool: True if both are running, False otherwise.
        """
        image_ok = False
        image_url = f"http://{IMG_API_IP}:{IMG_API_PORT}/status"
        try:
            response = requests.get(image_url, timeout=1)
            if response.status_code == 200:
                self.logger.debug("Image API is up!")
                image_ok = True
            else:
                self.logger.warning("Image API returned non-200 status.")
        except requests.ConnectionError:
            self.logger.warning("Image recognition API Connection Error")
        except requests.Timeout:
            self.logger.warning("Image recognition API Timeout")
        except Exception as e:
            self.logger.warning(f"Image API Exception: {e}")

        return image_ok


if __name__ == "__main__":
    rpi = RaspberryPi()
    rpi.start()
