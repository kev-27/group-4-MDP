import json
import os
import socket
import signal
import sys
import atexit
import bluetooth
from typing import Optional
from communication.link import Link


class AndroidMessage:
    """
    Class for communicating with Android tablet over Bluetooth connection.
    """

    def __init__(self, cat: str, value: str):
        self._cat = cat
        self._value = value

    @property
    def cat(self) -> str:
        return self._cat

    @property
    def value(self) -> str:
        return self._value

    @property
    def jsonify(self) -> str:
        return json.dumps({"cat": self._cat, "value": self._value})


class AndroidLink(Link):
    """
    Class for communicating with Android tablet over Bluetooth (RFCOMM).
    Messages follow a JSON format: {"cat": "xxx", "value": "xxx"}
    """

    def __init__(self):
        super().__init__()
        self.client_sock: Optional[bluetooth.BluetoothSocket] = None
        self.server_sock: Optional[bluetooth.BluetoothSocket] = None
        self.uuid = "b2a5ef6a-ec41-45b5-8aae-0f9ff16c09ce"

        # Ensure sockets are cleaned up on program exit or interruption
        atexit.register(self.disconnect)
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)

    def _signal_handler(self, sig, frame):
        self.logger.info(f"Signal {sig} received. Cleaning up Bluetooth sockets.")
        self.disconnect()
        sys.exit(0)

    def connect(self):
        """
        Connect to Android via Bluetooth.
        - Ensures clean RFCOMM state.
        - Advertises as Serial Port Profile (SPP) with custom UUID.
        """
        self.logger.info("Bluetooth connection starting...")

        try:
            # Make Pi discoverable
            os.system("sudo hciconfig hci0 piscan")

            # Create server socket
            self.server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
            self.server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_sock.bind(("", bluetooth.PORT_ANY))
            self.server_sock.listen(1)

            port = self.server_sock.getsockname()[1]

            # Advertise as custom + SPP
            bluetooth.advertise_service(
                self.server_sock,
                "mdpgrp4",
                service_id=self.uuid,
                service_classes=[self.uuid, bluetooth.SERIAL_PORT_CLASS],
                profiles=[bluetooth.SERIAL_PORT_PROFILE],
            )

            self.logger.info(
                f"Awaiting Bluetooth connection on RFCOMM channel {port}..."
            )
            self.client_sock, client_info = self.server_sock.accept()
            self.logger.info(f"Accepted Bluetooth connection from {client_info}")

        except Exception as e:
            self.logger.error(f"Error establishing Bluetooth connection: {e}")
            self.disconnect()

    def disconnect(self):
        """
        Disconnect from Android and close all sockets cleanly.
        """
        try:
            if self.client_sock:
                self.client_sock.close()
                self.client_sock = None
            if self.server_sock:
                self.server_sock.close()
                self.server_sock = None
            self.logger.info("Bluetooth link disconnected successfully.")
        except Exception as e:
            self.logger.error(f"Failed to disconnect Bluetooth link cleanly: {e}")

    def send(self, message: AndroidMessage):
        """
        Send a JSON message to Android.
        """
        try:
            if not self.client_sock:
                raise ConnectionError("No active Bluetooth client socket.")
            payload = f"{message.jsonify}\n".encode("utf-8")
            self.client_sock.send(payload)
            self.logger.debug(f"Sent to Android: {message.jsonify}")
        except Exception as e:
            self.logger.error(f"Error sending message to Android: {e}")
            raise

    def recv(self) -> Optional[str]:
        """
        Receive a JSON message from Android.
        """
        try:
            if not self.client_sock:
                raise ConnectionError("No active Bluetooth client socket.")
            data = self.client_sock.recv(1024)
            if not data:
                return None
            message = data.decode("utf-8").strip()
            self.logger.debug(f"Received from Android: {message}")
            return message
        except Exception as e:
            self.logger.error(f"Error receiving message from Android: {e}")
            raise
