# Fast Launch
Now deployed In huggingface https://huggingface.co/spaces/Mahiruoshi/mdpg4
## Test directly
```
import requests

url = "https://mahiruoshi-mdpg4.hf.space/"  # 你的 Space 地址
file_path = "20230825_122540_jpg.rf.f0620856e7afdbd116ceffdfd512b03a.jpg"

with open(file_path, "rb") as f:
    files = {"file": f}
    response = requests.post(url, files=files)

print("Status:", response.status_code)
try:
    print("Response:", response.json())
except:
    print("Response:", response.text)
```
##
You can use docker following huggingface instructions directly

## Local Implement
Base on Yolov5
```bash
pip install -r requirements.txt
```
# Inference Server
Start the server by

```bash
python main.py
```
Test script
```bash
import requests

SERVER_URL = "http://localhost:5000"

image_file = "20230825_122540_jpg.rf.f0620856e7afdbd116ceffdfd512b03a.jpg"


with open(image_file, 'rb') as f:
    files = {'file': f}
    response = requests.post(f"{SERVER_URL}/image", files=files)

print(response.status_code)
print(response.json())

```
Mapping Name to ID
```bash
name_to_id = {
    "NA": 'NA',
    "Bullseye": 10,
    "One": 11,
    "Two": 12,
    "Three": 13,
    "Four": 14,
    "Five": 15,
    "Six": 16,
    "Seven": 17,
    "Eight": 18,
    "Nine": 19,
    "A": 20,
    "B": 21,
    "C": 22,
    "D": 23,
    "E": 24,
    "F": 25,
    "G": 26,
    "H": 27,
    "S": 28,
    "T": 29,
    "U": 30,
    "V": 31,
    "W": 32,
    "X": 33,
    "Y": 34,
    "Z": 35,
    "Up": 36,
    "Down": 37,
    "Right": 38,
    "Left": 39,
    "Up Arrow": 36,
    "Down Arrow": 37,
    "Right Arrow": 38,
    "Left Arrow": 39,
    "Stop": 40}
```
# Training
```bash
git clone https://github.com/ultralytics/yolov5  # clone repo
cd yolov5
pip install -qr requirements.txt # install dependencies
```
Prepare dataset, pretrained model and config
```bash
data.yaml
!cp "Week_8.pt" "best.pt"
```
Train
```bash
# First time
python train.py --img 416 --batch 128 --epochs 150 --data E:/workspace/mdp/data.yaml --weights best.pt --cache

#python train.py --img 416 --batch 128 --epochs 150 --data E:/workspace/mdp/data.yaml --weights best.pt --cache --hyp hyp.yaml 
```
