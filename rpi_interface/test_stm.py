#!/usr/bin/env python3
import time
from communication.stm32 import STMLink


def main():
    # Initialize STM link
    stm = STMLink()

    # Connect to STM32
    print("Connecting to STM32...")
    stm.connect()
    print("Connected!")

    try:
        while True:
            # Send a test command
            test_cmd = "R"  # or another command your STM32 expects
            print(f"Sending: {test_cmd}")
            stm.send(test_cmd)

            # Wait for response
            response = stm.recv()
            if response:
                print(f"Received: {response}")

            time.sleep(1)  # adjust as needed

    except KeyboardInterrupt:
        print("Test stopped by user")

    finally:
        stm.disconnect()
        print("Disconnected from STM32")


if __name__ == "__main__":
    main()
