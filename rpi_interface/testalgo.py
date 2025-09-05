#!/usr/bin/env python3
import requests
from settings import API_IP, API_PORT


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
            print("API is up!")
            return True
        return False
    # If error, then log, and return False
    except ConnectionError:
        print("API Connection Error")
        return False
    except requests.Timeout:
        print("API Timeout")
        return False
    except Exception as e:
        print(f"API Exception: {e}")
        return False


if __name__ == "__main__":
    check_api()

