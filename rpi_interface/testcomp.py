#!/usr/bin/env python3
import json
import time
import requests
import sys
from picamera import PiCamera
from logger.logger import logger
from settings import API_IP, API_PORT
from communication.android import AndroidMessage

# Import RaspberryPi ONLY for the test cases that need it
from testrun import RaspberryPi


def check_api() -> bool:
    """Check whether image recognition and algorithm API server is up and running

    Returns:
        bool: True if running, False if not.
    """
    # Check image recognition API
    url = f"http://{API_IP}:{API_PORT}/status"
    try:
        response = requests.get(url, timeout=1)
        if response.status_code == 200:
            logger.debug("API is up!")
            return True
        return False
    # If error, then log, and return False
    except ConnectionError:
        logger.warning("API Connection Error")
        return False
    except requests.Timeout:
        logger.warning("API Timeout")
        return False
    except Exception as e:
        logger.warning(f"API Exception: {e}")
        return False


def test_android_comm_only():
    """Standalone test of Android <-> RPi communication."""
    rpi = RaspberryPi()
    try:
        rpi.android_link.connect()
        rpi.logger.info("=== Android Communication Test Started ===")

        from multiprocessing import Process

        rpi.proc_recv_android = Process(target=rpi.recv_android)
        rpi.proc_android_sender = Process(target=rpi.android_sender)

        rpi.proc_recv_android.start()
        rpi.proc_android_sender.start()

        rpi.android_queue.put(AndroidMessage("info", "RPi test connection active"))
        rpi.android_queue.put(AndroidMessage("mode", "test"))

        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        rpi.logger.info("Keyboard interrupt received, shutting down.")
    finally:
        rpi.android_link.disconnect()
        if rpi.proc_recv_android:
            rpi.proc_recv_android.kill()
        if rpi.proc_android_sender:
            rpi.proc_android_sender.kill()
        rpi.logger.info("=== Android Communication Test Ended ===")


def test_manual_control():
    rpi = RaspberryPi()
    try:
        rpi.android_link.connect()
        rpi.logger.info("=== Manual Control Test Started ===")

        from multiprocessing import Process

        rpi.proc_recv_android = Process(target=rpi.recv_android)
        rpi.proc_android_sender = Process(target=rpi.android_sender)
        rpi.proc_command_follower = Process(target=rpi.command_follower)

        rpi.proc_recv_android.start()
        rpi.proc_android_sender.start()
        rpi.proc_command_follower.start()

        rpi.android_queue.put(AndroidMessage("info", "RPi test connection active"))
        rpi.android_queue.put(AndroidMessage("mode", "test"))

        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        rpi.logger.info("Keyboard interrupt received, shutting down.")
    finally:
        rpi.android_link.disconnect()
        if rpi.proc_recv_android:
            rpi.proc_recv_android.kill()
        if rpi.proc_android_sender:
            rpi.proc_android_sender.kill()
        if rpi.proc_command_follower:
            rpi.proc_command_follower.kill()
        rpi.logger.info("=== Manual Control Test Ended ===")


def test_algo_comm_only():
    """Standalone Algo API test (no Android or STM needed)."""
    logger.info("=== Algo Communication Test Started ===")

    TEST_OBSTACLES = {
        "obstacles": [
            {"x": 2, "y": 5, "id": 1, "d": 1},
            {"x": 7, "y": 3, "id": 2, "d": 2},
        ]
    }
    body = {
        **TEST_OBSTACLES,
        "big_turn": "0",
        "robot_x": 1,
        "robot_y": 1,
        "robot_dir": 0,
        "retrying": False,
    }

    if check_api():
        logger.info("api is up")
    else:
        logger.warning("api is down")

    url = f"http://{API_IP}:{API_PORT}/path"
    try:
        response = requests.post(url, json=body)
        logger.info(f"Algo API status: {response.status_code}")
        if response.status_code == 200:
            logger.info(f"Algo API response: {json.dumps(response.json(), indent=4)}")
        else:
            logger.error(f"Algo API error: {response.text}")
    except Exception as e:
        logger.error(f"Algo API connection failed: {e}")



def test_stm_comm_only():
    """Standalone STM32 communication test with custom commands."""
    rpi = RaspberryPi()
    rpi.logger.info("=== STM32 Communication Test Started ===")

    try:
        # Ensure STM connection is established
        rpi.stm_link.connect()

        from multiprocessing import Process
        rpi.proc_command_follower = Process(target=rpi.command_follower)
        rpi.proc_command_follower.start()

        # Custom test commands (STM32 prefixes are supported in command_follower)
        TEST_COMMANDS = ["F0001000", "B0001000", "L0000000", "R0000000", "P0000000", "FIN"]

        for cmd in TEST_COMMANDS:
            rpi.logger.debug(f"Enqueuing command: {cmd}")
            rpi.command_queue.put(cmd)

        # Trigger command follower to start processing
        rpi.unpause.set()

        # Let commands run for a bit
        time.sleep(5)

    except KeyboardInterrupt:
        rpi.logger.info("Keyboard interrupt received, shutting down.")
    finally:
        # Clean up processes and STM link
        if rpi.proc_command_follower:
            rpi.proc_command_follower.terminate()
            rpi.proc_command_follower.join()
        rpi.stm_link.disconnect()
        rpi.logger.info("=== STM32 Communication Test Ended ===")

def test_camera_snap():
    """
    Repeatedly take snapshots and test image recognition API.
    """
    logger.info("=== Camera Snap Test Started ===")

    check_api()

    img_cnt = 1
    url = f"http://{API_IP}:{API_PORT}/image"

    try:
        while True:
            input("Press Enter to take photo (Ctrl+C to quit): ")

            img_name = f"img_{img_cnt}.jpg"
            logger.debug("Capturing photo with rpi...")
            with PiCamera() as camera:
                camera.resolution = (800, 800)
                camera.start_preview()
                time.sleep(0.5)
                camera.capture(img_name)
                logger.info(f"Image captured: {img_name}")

            img_cnt += 1

            logger.debug("Uploading to API...")
            with open(img_name, "rb") as f:
                response = requests.post(url, files={"file": f})
            logger.debug(f"Upload response: {response.status_code}")

    except KeyboardInterrupt:
        logger.info("Keyboard interrupt received, shutting down.")

    logger.info("=== Camera Snap Test Ended ===")


if __name__ == "__main__":
    if "--test-android" in sys.argv:
        test_android_comm_only()
    elif "--test-algo" in sys.argv:
        test_algo_comm_only()
    elif "--test-stm" in sys.argv:
        test_stm_comm_only()
    elif "--test-snap" in sys.argv:
        test_camera_snap()
    elif "--test-move" in sys.argv:
        test_manual_control()

    else:
        print(
            "Usage: pyt#hon tests.py [--test-android | --test-algo | --test-stm | --test-snap | --test-move]"
        )
