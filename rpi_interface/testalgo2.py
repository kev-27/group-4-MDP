#!/usr/bin/env python3
import requests
from settings import API_IP, API_PORT

def check_algo_connection() -> bool:
    """Minimal test for checking if Algo/Image API server is reachable"""
    url = f"http://{API_IP}:{API_PORT}/path"
    try:
        response = requests.get(url, timeout=2)
        if response.status_code == 200:
            print("✅ API is up and running.")
            return True
        else:
            print(f"⚠️ API responded with status code {response.status_code}")
            return False
    except requests.Timeout:
        print("❌ API request timed out.")
        return False
    except requests.ConnectionError:
        print("❌ Could not connect to API server.")
        return False
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False


if __name__ == "__main__":
    check_algo_connection()

