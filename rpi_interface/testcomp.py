#!/usr/bin/env python3
import json
import time
import requests
import sys

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
    """Standalone STM32 communication test."""
    rpi = RaspberryPi()
    rpi.logger.info("=== STM32 Communication Test Started ===")

    from multiprocessing import Process

    rpi.proc_command_follower = Process(target=rpi.command_follower)
    rpi.proc_command_follower.start()

    TEST_COMMANDS = ["FW05", "BW05", "FR00", "FIN"]
    for cmd in TEST_COMMANDS:
        rpi.logger.debug(f"Enqueuing command: {cmd}")
        rpi.command_queue.put(cmd)

    rpi.unpause.set()

    time.sleep(2)

    rpi.proc_command_follower.terminate()
    rpi.proc_command_follower.join()
    rpi.logger.info("=== STM32 Communication Test Ended ===")


def test_camera_snap():
    """Standalone test: snap image, call image recognition API, send result to Android."""
    rpi = RaspberryPi()
    rpi.android_link.connect()
    rpi.logger.info("=== Camera Snap & Android Test Started ===")

    check_api()

    try:
        # Example obstacle id with signal
        test_obstacle_id = "1_demo"

        # Snap image and send result to Android
        rpi.snap_and_rec(test_obstacle_id)

        # Optionally, log what was sent to Android

        while not rpi.android_queue.empty():
            msg = rpi.android_queue.get()
            rpi.logger.info(f"Sent to Android: cat={msg.cat}, value={msg.value}")

        # Short wait to ensure Android can receive message
        time.sleep(2)

    except KeyboardInterrupt:
        rpi.logger.info("Keyboard interrupt received, shutting down.")
    finally:
        rpi.android_link.disconnect()
        rpi.logger.info("=== Camera Snap & Android Test Ended ===")


if __name__ == "__main__":
    if "--test-android" in sys.argv:
        test_android_comm_only()
    elif "--test-algo" in sys.argv:
        test_algo_comm_only()
    elif "--test-stm" in sys.argv:
        test_stm_comm_only()
    elif "--test-snap" in sys.argv:
        test_camera_snap()

    else:
        print("Usage: pyt#hon tests.py [--test-android | --test-algo | --test-stm | --test-snap]")
