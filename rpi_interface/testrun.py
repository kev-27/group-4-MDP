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
from consts import SYMBOL_MAP
from logger.logger import logger
from settings import IMG_API_IP, IMG_API_PORT, ALGO_API_IP, ALGO_API_PORT
import sys


class PiAction:
    """
    Wrapper for an action to be executed.

    Takes in a key-value pair of category and context dependent value
    """

    def __init__(self, cat, value):
        """
        :param cat: The category of the action. Can be 'info', 'mode', 'path', 'snap', 'obstacle', 'location', 'failed', 'success'
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
    - process actions for image rec: snap an image, stitch together images
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
        # self.proc_android_sender = None
        self.proc_command_follower = None  # movement command
        self.proc_rpi_action = None  # snap images / stitching
        # =================================================

        # ========= flags =================
        self.rs_flag = False  # checks resets command RS00
        self.failed_attempt = False
        # ============================

        # =========== shared resources from Manager() ==========
        self.success_obstacles = self.manager.list()  # obstacles recognised
        self.failed_obstacles = self.manager.list()  # obstacles not recognised
        self.obstacles = self.manager.dict()  # known obstacles
        self.current_location = self.manager.dict()  # X, Y, D
        # =========================================

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
                self.logger.warning(f"Received corrupted / invalid JSON: {msg_str!r}")
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

            ## Command: Set obstacles ##
            if message["cat"] == "obstacles":
                self.rpi_action_queue.put(PiAction(**message))
                self.logger.debug(f"Set obstacles PiAction added to queue: {message}")

            # elif message["cat"] == "manual":
            # if not self.unpause.is_set():
            # self.logger.info("Gryo reset!")
            # self.stm_link.send("RS00")
            # Main trigger to start movement #
            # self.unpause.set()
            # self.logger.info("Start command received, starting robot on path!")
            # self.android_queue.put(AndroidMessage("status", "running"))

            # cmd = message["value"]
            # self.logger.info(f"Manual command received: {cmd}")
            # self.command_queue.put(cmd)
            # self.android_queue.put(AndroidMessage("info", f"Manual command enqueued: {cmd}"))

            ## Command: Start Moving ##
            elif message["cat"] == "control":
                if message["value"] == "start":
                    # Check API
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

                    # Commencing path following
                    if not self.command_queue.empty():
                        self.logger.info("Gryo reset!")
                        # self.stm_link.send("RS00")
                        # Main trigger to start movement #
                        self.unpause.set()
                        self.logger.info(
                            "Start command received, starting robot on path!"
                        )
                        self.android_queue.put(
                            AndroidMessage("info", "Starting robot on path!")
                        )
                        self.android_queue.put(AndroidMessage("status", "running"))
                    else:
                        self.logger.warning(
                            "The command queue is empty, please set obstacles."
                        )
                    """
                       self.android_queue.put(
                            AndroidMessage(
                                "error",
                                "Command queue is empty, did you set obstacles?",
                            )
                        )
                    """

    def recv_stm(self) -> None:
        """
        [Child Process] Receive acknowledgement messages from STM32, and release the movement lock

        The very first command is presumed to be RS00.
        """
        while True:
            message: str = self.stm_link.recv()
            self.logger.debug(f"Received {message}")
            if message.startswith("DONE"):
                # if self.rs_flag == False:
                # self.rs_flag = True
                self.logger.debug("ACK for RS00 from STM32 received.")
                # continue
                try:
                    self.movement_lock.release()
                    try:
                        self.retrylock.release()
                    except:
                        pass
                    self.logger.debug(
                        "ACK from STM32 received, movement lock released."
                    )

                    cur_location = self.path_queue.get_nowait()  # non-blocking

                    self.current_location["x"] = cur_location["x"]
                    self.current_location["y"] = cur_location["y"]
                    self.current_location["d"] = cur_location["d"]
                    self.logger.info(f"self.current_location = {self.current_location}")

                    """
                    self.android_queue.put(
                        AndroidMessage(
                            "location",
                            {
                                "x": cur_location["x"],
                                "y": cur_location["y"],
                                "d": cur_location["d"],
                            },
                        )
                    )
                  """

                except Exception:
                    self.logger.warning("Tried to release a released lock!")
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

        Just switch statements

        There are three types of commands:
        - movement commands to send to STM32
        - "snapping" an image: i.e. taking a still image of obstacle
        - finish command signalling completion of path in the maze
        """
        while True:
            # Retrieve next movement command
            command: str = self.command_queue.get()
            self.logger.debug("wait for unpause")
            # Wait for unpause event to be true [Main Trigger]
            try:
                self.logger.debug("wait for retrylock")
                self.retrylock.acquire()
                self.retrylock.release()
            except:
                self.logger.debug("wait for unpause")
                self.unpause.wait()
            self.logger.debug("wait for movelock")
            # Acquire lock first (needed for both moving, and snapping pictures)
            self.movement_lock.acquire()

            # STM32 Commands - Send straight to STM32
            # needs refactoring, consts being defined within class methods is goofy
            stm32_prefixes = (
                "FS",
                "BS",
                "FW",
                "BW",
                "FL",
                "FR",
                "BL",
                "BR",
                "TL",
                "TR",
                "A",
                "C",
                "DT",
                "STOP",
                "ZZ",
                "RS",
                # following is for demo
                "W",
                "A",
                "S",
                "D",
                "Z",
                "Q",
                "E",
                "X",
                "P",
            )
            if command.startswith(stm32_prefixes):
                self.stm_link.send(command)
                self.logger.debug(f"Sending to STM32: {command}")

            # Snap command
            elif command.startswith("SNAP"):
                obstacle_id_with_signal = command.replace("SNAP", "")

                self.rpi_action_queue.put(
                    PiAction(cat="snap", value=obstacle_id_with_signal)
                )

            # End of path
            elif command == "FIN":
                self.logger.info(
                    f"At FIN, self.failed_obstacles: {self.failed_obstacles}"
                )
                self.logger.info(
                    f"At FIN, self.current_location: {self.current_location}"
                )

                if len(self.failed_obstacles) != 0 and self.failed_attempt == False:
                    new_obstacle_list = list(self.failed_obstacles)
                    for i in list(self.success_obstacles):
                        # {'x': 5, 'y': 11, 'id': 1, 'd': 4}
                        i["d"] = 8
                        new_obstacle_list.append(i)

                    self.logger.info("Attempting to go to failed obstacles")
                    self.failed_attempt = True
                    self.request_algo(
                        {"obstacles": new_obstacle_list, "mode": "0"},
                        self.current_location["x"],
                        self.current_location["y"],
                        self.current_location["d"],
                        retrying=True,
                    )
                    self.retrylock = self.manager.Lock()
                    self.movement_lock.release()
                    continue

                self.unpause.clear()
                self.movement_lock.release()
                self.logger.info("Commands queue finished.")
                self.android_queue.put(
                    AndroidMessage("info", "Commands queue finished.")
                )
                self.android_queue.put(AndroidMessage("status", "finished"))
                self.rpi_action_queue.put(PiAction(cat="stitch", value=""))
            else:
                raise Exception(f"Unknown command: {command}")

    def rpi_action(self):
        """
        [Child Process] process to handle image recognition and path finding tasks

        Only three categories are accepted:
        - obstacles: position of obstacle
        - snap: taking still image
        - stitch: compile images together
        """
        while True:
            action: PiAction = self.rpi_action_queue.get()
            self.logger.debug(
                f"PiAction retrieved from queue: {action.cat} {action.value}"
            )

            if action.cat == "obstacles":
                for obs in action.value["obstacles"]:
                    self.obstacles[obs["id"]] = obs
                self.request_algo(action.value)
            elif action.cat == "snap":
                self.snap_and_rec(obstacle_id_with_signal=action.value)
            elif action.cat == "stitch":
                self.request_stitch()

    def snap_and_rec(self, obstacle_id_with_signal: str) -> None:
        """
        RPi snaps an image and calls the API for image-rec.
        The response is then forwarded back to the android
        :param obstacle_id_with_signal: the current obstacle ID followed by underscore followed by signal
        """

        obstacle_id, signal = obstacle_id_with_signal.split("_")
        self.logger.info(f"Capturing image for obstacle id: {obstacle_id}")

        """
        self.android_queue.put(
            AndroidMessage("info", f"Capturing image for obstacle id: {obstacle_id}")
        )
        """

        filename = f"{int(time.time())}_{obstacle_id}_{signal}.jpg"
        url = f"http://{IMG_API_IP}:{IMG_API_PORT}/image"

        # Capture image with PiCamera
        with PiCamera() as camera:
            camera.resolution = (800, 800)  # simple resolution, can adjust
            camera.start_preview()
            time.sleep(0.5)  # let auto-adjust settle
            camera.capture(filename)
            self.logger.info(f"Image captured: {filename}")

        # Send to API
        try:
            with open(filename, "rb") as f:
                response = requests.post(
                    url,
                    files={"file": f},
                    data={
                        "NUM_OBSTACLES": int(obstacle_id)
                    },  # this is the obstalce ID, bad naming
                )
            results = json.loads(response.content)
        except Exception as e:
            self.logger.error(f"Error calling image-rec API: {e}")
            return

        # Handle "NA" or successful recognition
        if results["predicted_id"] == "-1":
            self.failed_obstacles.append(int(self.obstacles[results["num_obstacles"]]))
            self.logger.info(
                f"Added Obstacle {results['num_obstacles']} to failed obstacles."
            )
        else:
            obstacle_id = obstacle_id_with_signal.split("_")[0]
            self.success_obstacles.append(int(self.obstacles[obstacle_id]))

            self.logger.info(
                f"Obstacle {results['num_obstacles']} successfully recognized."
            )
            res = f"obstacleID: {int(results['num_obstacles'])}, imageID: {int(results['predicted_id'])}"
        # self.android_queue.put(AndroidMessage("target", res))

        # Log results
        self.logger.info(f"Image recognition results: {results}")
        # self.android_queue.put(AndroidMessage("image-rec", results))

    def request_algo(self, data, robot_x=1, robot_y=1, robot_dir=0, retrying=False):
        """
        Requests for a series of commands and the path from the Algo API.
        The received commands and path are then queued in the respective queues
        """
        self.logger.info("Requesting path from algo...")
        self.android_queue.put(AndroidMessage("info", "Requesting path from algo..."))
        self.logger.info(f"data: {data}")
        body = {
            **data,
            "big_turn": "0",
            "robot_x": robot_x,
            "robot_y": robot_y,
            "robot_dir": robot_dir,
            "retrying": retrying,
        }
        url = f"http://{ALGO_API_IP}:{ALGO_API_PORT}/path"
        response = requests.post(url, json=body)

        # Error encountered at the server, return early
        if response.status_code != 200:
            self.android_queue.put(
                AndroidMessage(
                    "error", "Something went wrong when requesting path from Algo API."
                )
            )
            self.logger.error(
                "Something went wrong when requesting path from Algo API."
            )
            return

        # Parse response
        result = json.loads(response.content)["data"]
        commands = result["commands"]
        path = result["path"]

        # Log commands received
        self.logger.debug(f"Commands received from API: {commands}")

        # Put commands and paths into respective queues
        self.clear_queues()
        for c in commands:
            self.command_queue.put(c)
        for p in path[
            1:
        ]:  # ignore first element as it is the starting position of the robot
            self.path_queue.put(p)

        self.android_queue.put(
            AndroidMessage(
                "info", "Commands and path received Algo API. Robot is ready to move."
            )
        )
        self.logger.info("Commands and path received Algo API. Robot is ready to move.")

    def request_stitch(self):
        """Sends a stitch request to the image recognition API to stitch the different images together"""
        url = f"http://{IMG_API_IP}:{IMG_API_PORT}/stitch"
        response = requests.get(url)

        # If error, then log, and send error to Android
        if response.status_code != 200:
            # Notify android
            self.android_queue.put(
                AndroidMessage(
                    "error", "Something went wrong when requesting stitch from the API."
                )
            )
            self.logger.error(
                "Something went wrong when requesting stitch from the API."
            )
            return

        self.logger.info("Images stitched!")
        self.android_queue.put(AndroidMessage("info", "Images stitched!"))

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
    # Check image recognition API
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

    # Check algorithm API
    algo_ok = False
    algo_url = f"http://{ALGO_API_IP}:{ALGO_API_PORT}/status"
    try:
        response = requests.get(algo_url, timeout=1)
        if response.status_code == 200:
            self.logger.debug("Algorithm API is up!")
            algo_ok = True
        else:
            self.logger.warning("Algorithm API returned non-200 status.")
    except requests.ConnectionError:
        self.logger.warning("Algorithm API Connection Error")
    except requests.Timeout:
        self.logger.warning("Algorithm API Timeout")
    except Exception as e:
        self.logger.warning(f"Algorithm API Exception: {e}")

    # Only return True if both are OK
    return image_ok and algo_ok


if __name__ == "__main__":
    rpi = RaspberryPi()
    rpi.start()
