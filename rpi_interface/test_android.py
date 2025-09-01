def test_android_connection():
    """
    Minimal test for AndroidLink over Bluetooth.
    Waits for a client to connect and sends/receives a message.
    """
    from communication.android import AndroidLink, AndroidMessage

    android_link = AndroidLink()
    print("Starting AndroidLink server...")
    android_link.connect()
    print("Waiting for client connection...")

    try:
        # Send a test message
        msg = AndroidMessage("info", "Hello from RPi!")
        android_link.send(msg)
        print("Sent test message.")

        # Receive a message from client
        received = android_link.recv()
        print(f"Received from client: {received}")

    except Exception as e:
        print(f"Error during test: {e}")
    finally:
        android_link.disconnect()
        print("Bluetooth test finished. Connection closed.")


if __name__ == "__main__":
    test_android_connection()

