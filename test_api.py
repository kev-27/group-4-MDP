import requests

BASE_URL = "http://rpi-ip>:5000"

def test_status():
    print("\n--- Testing /status ---")
    try:
        response = requests.get(f"{BASE_URL}/status", timeout=5)
        print("Status response:", response.json())
    except Exception as e:
        print("Error calling /status:", e)

def test_image(image_path):
    print("\n--- Testing /image ---")
    try:
        with open(image_path, "rb") as f:
            files = {"file": f}
            response = requests.post(f"{BASE_URL}/image", files=files, timeout=10)
            print("Image response:", response.json())
    except Exception as e:
        print("Error calling /image:", e)

def test_stitch():
    print("\n--- Testing /stitch ---")
    try:
        response = requests.get(f"{BASE_URL}/stitch", timeout=10)
        print("Stitch response:", response.json())
    except Exception as e:
        print("Error calling /stitch:", e)

if __name__ == "__main__":
    # 1. Test health check
    test_status()

    # 2. Test image upload 
    # filename format: "<timestamp>_<obstacle_id>_<signal>.jpeg"
    test_image("123456_1_left.jpg")

    # 3. Test stitching
    test_stitch()
