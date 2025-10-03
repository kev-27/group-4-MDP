#!/usr/bin/env python3
import json
import time
import requests
import sys
from picamera import PiCamera
from logger.logger import logger
from communication.android import AndroidMessage
from communication.stm32 import STMLink
import serial
from settings import (
    IMG_API_IP,
    IMG_API_PORT,
    ALGO_API_IP,
    ALGO_API_PORT,
    SERIAL_PORT,
    BAUD_RATE,
)

# Import RaspberryPi ONLY for the test cases that need it
from testrun import RaspberryPi


def check_api() -> bool:
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
            logger.debug("Image API is up!")
            image_ok = True
        else:
            logger.warning("Image API returned non-200 status.")
    except requests.ConnectionError:
        logger.warning("Image recognition API Connection Error")
    except requests.Timeout:
        logger.warning("Image recognition API Timeout")
    except Exception as e:
        logger.warning(f"Image API Exception: {e}")

    # Check algorithm API
    algo_ok = False
    algo_url = f"http://{ALGO_API_IP}:{ALGO_API_PORT}/status"
    try:
        response = requests.get(algo_url, timeout=1)
        if response.status_code == 200:
            logger.debug("Algorithm API is up!")
            algo_ok = True
        else:
            self.logger.warning("Algorithm API returned non-200 status.")
    except requests.ConnectionError:
        logger.warning("Algorithm API Connection Error")
    except requests.Timeout:
        logger.warning("Algorithm API Timeout")
    except Exception as e:
        logger.warning(f"Algorithm API Exception: {e}")

    # Only return True if both are OK
    return image_ok and algo_ok


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
            {"x": 5, "y": 8, "id": 1, "d": 6},
            {"x": 8, "y": 12, "id": 2, "d": 4},
            {"x": 1, "y": 18, "id": 3, "d": 4},
            {"x": 17, "y": 2, "id": 4, "d": 6},
            {"x": 8, "y": 15, "id": 5, "d": 2},
            # {"x": 17, "y": 4, "id": 6, "d": 0},
            # {"x": 18, "y": 19, "id": 7, "d": 4},
            # {"x": 14, "y": 19, "id": 8, "d": 4},
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

    url = f"http://{ALGO_API_IP}:{ALGO_API_PORT}/path"
    try:
        response = requests.post(url, json=body)
        logger.info(f"Algo API status: {response.status_code}")
        if response.status_code == 200:
            logger.info(f"Algo API response: {json.dumps(response.json(), indent=4)}")
        else:
            logger.error(f"Algo API error: {response.text}")
    except Exception as e:
        logger.error(f"Algo API connection failed: {e}")


def test_stm_comm_only_1():
    """Standalone STM32 communication test with custom commands."""
    rpi = RaspberryPi()
    rpi.logger.info("=== STM32 Communication Test Started ===")

    try:
        # Ensure STM connection is established
        rpi.stm_link.connect()

        from multiprocessing import Process

        rpi.android_link.connect()
        rpi.logger.info("=== Android Communication Test Started ===")

        rpi.proc_recv_android = Process(target=rpi.recv_android)
        rpi.proc_android_sender = Process(target=rpi.android_sender)

        rpi.proc_recv_android.start()
        rpi.proc_android_sender.start()

        rpi.android_queue.put(AndroidMessage("info", "RPi test connection active"))
        rpi.android_queue.put(AndroidMessage("mode", "test"))

        rpi.proc_command_follower = Process(target=rpi.command_follower)
        rpi.proc_command_follower.start()

        # Custom test commands (STM32 prefixes are supported in command_follower)
        TEST_COMMANDS = ["F", "FIN"]

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
        rpi.android_link.disconnect()
        if rpi.proc_recv_android:
            rpi.proc_recv_android.kill()
        if rpi.proc_android_sender:
            rpi.proc_android_sender.kill()
        rpi.logger.info("=== STM32 Communication Test Ended ===")


def test_stm_comm_only():
    """Continuous STM32 communication test. Keeps sending until Ctrl+C."""
    rpi = RaspberryPi()
    rpi.logger.info("=== STM32 Communication Test Started ===")

    try:
        # Ensure STM connection is established
        rpi.stm_link.connect()

        # Loop until user interrupts
        while True:
            cmd = "R"  # adjust format as per STM firmware
            rpi.logger.info(f"Sending command to STM32: {cmd.strip()}")
            rpi.stm_link.send(cmd)

            rpi.logger.debug("sleeping")
            time.sleep(2)  # avoid spamming too fast, adjust as needed
            rpi.logger.debug("woke up")

            cmd = "L"  # adjust format as per STM firmware
            rpi.logger.info(f"Sending command to STM32: {cmd.strip()}")
            rpi.stm_link.send(cmd)
            break

        cmd = "S"  # adjust format as per STM firmware

    except KeyboardInterrupt:
        rpi.logger.info("Keyboard interrupt received, shutting down.")
    finally:
        rpi.stm_link.disconnect()
        rpi.logger.info("=== STM32 Communication Test Ended ===")


def test_char():
    # Open serial connection directly
    with serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1) as ser:
        # Send the character "F" followed by newline
        ser.write(b"F\n")
        print("Sent: F")

        # Read a line back from STM32 (e.g., expected "ACK")
        response = ser.readline().strip().decode("utf-8")
        print(f"STM32 responded: {response}")


def test_camera_snap():
    """
    Repeatedly take snapshots and test image recognition API.
    """
    logger.info("=== Camera Snap Test Started ===")

    check_api()

    img_cnt = 1
    url = f"http://{IMG_API_IP}:{IMG_API_PORT}/image"

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
            NUM_OBSTACLES = 1  
            with open(img_name, "rb") as f:
                response = requests.post(
                    url, files={"file": f}, data={"NUM_OBSTACLES": NUM_OBSTACLES}
                )
            logger.debug(f"Upload response: {response.status_code}")

    except KeyboardInterrupt:
        logger.info("Keyboard interrupt received, shutting down.")

    logger.info("=== Camera Snap Test Ended ===")


def test_checklist_C9():
    try:
        rpi = RaspberryPi()
        rpi.android_link.connect()
        rpi.logger.info("=== Checklist C9 Test Started ===")

        from multiprocessing import Process

        rpi.proc_recv_android = Process(target=rpi.recv_android)
        rpi.proc_android_sender = Process(target=rpi.android_sender)

        rpi.proc_recv_android.start()
        rpi.proc_android_sender.start()

        rpi.android_queue.put(AndroidMessage("info", "RPi test connection active"))
        rpi.android_queue.put(AndroidMessage("mode", "testing checklist C9"))

        while True:
            obstacleID = int(input("Enter obstacle ID: "))
            imgID = int(input("Enter imgID: "))
            json_pair = {"id1": obstacleID, "id2": imgID}
            rpi.android_queue.put(AndroidMessage("target", json_pair))
            time.sleep(1)

    except KeyboardInterrupt:
        rpi.logger.info("keyboard interrupt received, shutting down")
    finally:
        rpi.android_link.disconnect()
        if rpi.proc_recv_android:
            rpi.proc_recv_android.kill()
        if rpi.proc_android_sender:
            rpi.proc_android_sender.kill()
        rpi.logger.info("=== Checklist C9 Test Ended ===")


def test_checklist_C3():
    rpi = RaspberryPi()
    rpi.logger.info("=== Checklist C3 Test Started ===")

    try:
        from multiprocessing import Process

        rpi.stm_link.connect()
        rpi.android_link.connect()
        rpi.proc_recv_android = Process(target=rpi.recv_android)
        rpi.proc_android_sender = Process(target=rpi.android_sender)

        rpi.proc_recv_android.start()
        rpi.proc_android_sender.start()

        rpi.android_queue.put(AndroidMessage("info", "RPi test connection active"))
        rpi.android_queue.put(AndroidMessage("mode", "testing checklist C3"))

        rpi.proc_command_follower = Process(target=rpi.command_follower)
        rpi.proc_command_follower.start()

    except KeyboardInterrupt:
        rpi.proc_command_queue.put("FIN")
        rpi.logger.info("Keyboard interrupt received, shutting down.")
    finally:
        # Clean up processes and STM link
        if rpi.proc_command_follower:
            rpi.proc_command_follower.terminate()
            rpi.proc_command_follower.join()
        rpi.stm_link.disconnect()
        rpi.android_link.disconnect()
        if rpi.proc_recv_android:
            rpi.proc_recv_android.kill()
        if rpi.proc_android_sender:
            rpi.proc_android_sender.kill()
        rpi.logger.info("=== Checklist C3 Test Ended ===")


def test_checklist_C3A():
    try:
        rpi = RaspberryPi()
        rpi.android_link.connect()
        rpi.logger.info("=== Checklist C3A Test Started ===")

        from multiprocessing import Process

        rpi.proc_recv_android = Process(target=rpi.recv_android)
        rpi.proc_android_sender = Process(target=rpi.android_sender)

        rpi.proc_recv_android.start()
        rpi.proc_android_sender.start()

        rpi.android_queue.put(AndroidMessage("info", "RPi test connection active"))
        rpi.android_queue.put(AndroidMessage("mode", "testing checklist C3A"))

        while True:
            time.sleep(1)
            x = int(input("enter x: "))
            y = int(input("enter y: "))
            d = int(input("enter d: "))
            position = {"x": x, "y": y, "d": d}
            rpi.android_queue.put(AndroidMessage("location", position))

    except KeyboardInterrupt:
        rpi.logger.info("keyboard interrupt received, shutting down")
    finally:
        rpi.android_link.disconnect()
        if rpi.proc_recv_android:
            rpi.proc_recv_android.kill()
        if rpi.proc_android_sender:
            rpi.proc_android_sender.kill()
        rpi.logger.info("=== Checklist C9 Test Ended ===")


def test_A4():
    cmd = [
        "W1000",
        "A0270",
        "A0090",
        "A0360",
        "A0120",
        "D0270",
        "D0120",
        "D0360",
        "D0090",
        "SEX",
        "PORN"
    ]

    rpi = RaspberryPi()
    rpi.logger.info("=== STM32 Communication Test Started ===")

    try:
        # Ensure STM connection is established
        rpi.stm_link.connect()

        # Loop until user interrupts
        while True:
            for i, v in enumerate(cmd):
                print(f"index: {i}, cmd: {v}")

            num = -1

            while num == -1:
                num = int(input("Enter the index: "))
                if not 0 <= num <= len(cmd) - 1:
                    num = -1
                if num == -1:
                    continue

                rpi.logger.debug("sending")
                rpi.stm_link.send(cmd[num])
                rpi.logger.debug("sent")

    except KeyboardInterrupt:
        rpi.logger.info("Keyboard interrupt received, shutting down.")
    finally:
        rpi.stm_link.disconnect()
        rpi.logger.info("=== STM32 Communication Test Ended ===")


def test_A5():
    rpi = RaspberryPi()
    rpi.logger.info("=== A5 test Started ===")

    check_api()

    img_cnt = 1
    url = f"http://{API_IP}:{API_PORT}/image"

    try:
        check_api()
        rpi.stm_link.connect()

        image_id = -1

        while image_id == -1:
            # === Capture photo ===
            img_name = f"img_{img_cnt}.jpg"
            rpi.logger.debug("Capturing photo with rpi...")
            with PiCamera() as camera:
                camera.resolution = (800, 800)
                camera.start_preview()
                time.sleep(1)
                camera.capture(img_name)
                rpi.logger.info(f"Image captured: {img_name}")

            img_cnt += 1

            # === Upload to API ===
            rpi.logger.debug("Uploading to API...")
            with open(img_name, "rb") as f:
                response = requests.post(url, files={"file": f})
            rpi.logger.debug(f"Upload response: {response.status_code}")

            if response.ok:
                try:
                    data = response.json()
                    image_id = int(data.get("image_id", -1))
                    rpi.logger.info(f"Parsed image_id: {image_id}")
                except Exception as e:
                    rpi.logger.error(f"Failed to parse JSON: {e}")
            else:
                rpi.logger.error(f"Upload failed: {response.text}")

            # === If still no valid id, tell STM32 to move again ===
            if image_id == -1:
                rpi.logger.debug("Sending command to STM32...")
                rpi.stm_link.send("X6969")
                rpi.logger.debug("Command sent, waiting for next DONEz")
            else:
                rpi.logger.debug("Sending stop command to STM32...")
                rpi.stm_link.send("P6969")
                rpi.logger.debug("Command sent, waiting for next DONEz")

            # === Wait for STM32 DONEz before doing anything ===
    #            rpi.logger.debug("Waiting for STM32 DONEz...")
    #            ack = None
    #            while ack != "DONEz":
    #                ack = rpi.stm_link.recv()
    #                if ack is None:
    #                    rpi.logger.warning("No ACK received yet...")
    #                else:
    #                    rpi.logger.debug(f"Received from STM32: {ack}")
    #

    except KeyboardInterrupt:
        rpi.logger.info("Keyboard interrupt received, shutting down.")

    rpi.logger.info("=== A5 Test Ended ===")


def test_integration():
    """Integration Test: STM32 + Algo API + Image Recognition (no Android)"""
    import time
    import json
    import requests
    from picamera import PiCamera
    from communication.stm32 import STMLink
    from logger.logger import logger
    from consts import IMG_API_IP, IMG_API_PORT, ALGO_API_IP, ALGO_API_PORT

    def check_api():
        """Check if both APIs are alive"""
        try:
            img_ok = requests.get(f"http://{IMG_API_IP}:{IMG_API_PORT}/status", timeout=1).status_code == 200
            algo_ok = requests.get(f"http://{ALGO_API_IP}:{ALGO_API_PORT}/status", timeout=1).status_code == 200
            return img_ok and algo_ok
        except Exception as e:
            logger.error(f"API check failed: {e}")
            return False

    def snap_and_recognize(obstacle):
        """Capture image and send to image recognition API"""
        filename = f"test_{obstacle['id']}.jpg"
        url = f"http://{IMG_API_IP}:{IMG_API_PORT}/image"

        try:
            with PiCamera() as camera:
                camera.resolution = (800, 800)
                camera.start_preview()
                time.sleep(0.5)
                camera.capture(filename)
                logger.info(f"Image captured: {filename}")
        except Exception as e:
            logger.error(f"Camera error: {e}")
            return

        try:
            with open(filename, "rb") as f:
                response = requests.post(
                    url,
                    files={"file": f},
                    data={"NUM_OBSTACLES": obstacle["id"]}  # misnamed, but used as obstacle ID
                )
            if response.status_code != 200:
                logger.error(f"Image API error: {response.status_code}")
                return

            results = response.json()
            logger.info(f"Image recognition results: {json.dumps(results, indent=2)}")
        except Exception as e:
            logger.error(f"Image API call failed: {e}")

    logger.info("=== Integration Test Started ===")

    if not check_api():
        logger.error("API check failed. Ensure Algo and Image API servers are running.")
        return

    # Connect to STM32
    stm = STMLink()
    try:
        stm.connect()
        logger.info("STM32 connected.")
    except Exception as e:
        logger.error(f"Failed to connect to STM32: {e}")
        return

    # Define test obstacles
    obstacles = [
        {"x": 5, "y": 8, "id": 1, "d": 6},
        {"x": 8, "y": 12, "id": 2, "d": 4},
    ]
    obstacle_map = {obs["id"]: obs for obs in obstacles}

    # Request path from Algo API
    body = {
        "obstacles": obstacles,
        "big_turn": "0",
        "robot_x": 1,
        "robot_y": 1,
        "robot_dir": 0,
        "retrying": False,
    }

    try:
        algo_url = f"http://{ALGO_API_IP}:{ALGO_API_PORT}/path"
        response = requests.post(algo_url, json=body)
        if response.status_code != 200:
            logger.error(f"Algo API returned {response.status_code}")
            logger.error(f"Algo response: {response.text}")
            return

        result = response.json()["data"]
        commands = result["commands"]
        path = result["path"]

        logger.info(f"Commands: {commands}")
        logger.info(f"Path: {path}")
    except Exception as e:
        logger.error(f"Algo API call failed: {e}")
        return

    # Execute commands
    for command in commands:
        if command.startswith("SNAP"):
            try:
                obs_id = int(command.replace("SNAP", ""))
                logger.info(f"Taking snapshot for obstacle ID {obs_id}")
                snap_and_recognize(obstacle_map[obs_id])
            except Exception as e:
                logger.error(f"Failed to handle SNAP command: {e}")
        else:
            logger.info(f"Sending command to STM32: {command}")
            stm.send(command)
            # Wait for STM32 to reply with DONEz
            while True:
                try:
                    msg = stm.recv()
                    logger.info(f"STM32 says: {msg}")
                    if msg.startswith("DONEz"):
                        break
                except Exception as e:
                    logger.error(f"STM recv error: {e}")
                    break

    logger.info("=== Integration Test Completed ===")

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
    elif "--test-char" in sys.argv:
        test_char()
    elif "--test-a" in sys.argv:
        test_stm_comm_only_1()
    elif "--test-C9" in sys.argv:
        test_checklist_C9()
    elif "--test-C3" in sys.argv:
        test_checklist_C3()
    elif "--test-C3A" in sys.argv:
        test_checklist_C3A()
    elif "--test-A4" in sys.argv:
        test_A4()
    elif "--test-A5" in sys.argv:
        test_A5()
    elif "--test-integration" in sys.argv:
        test_integration()

    else:
        print(
            "Usage: pyt#hon tests.py [--test-android | --test-algo | --test-stm | --test-snap | --test-move]"
        )
