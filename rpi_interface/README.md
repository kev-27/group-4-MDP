### Chore

#### Formatting SD card

1. Check the file system, make sure it is unmounted.

```bash
$ lsblk -f

NAME        FSTYPE FSVER LABEL UUID                                 FSAVAIL FSUSE% MOUNTPOINTS
sda                                                                                
sdb                                                                                
└─sdb1      vfat   FAT32       091F-2305                                           
sdc                                                                                
nvme0n1                                                                            
├─nvme0n1p1 vfat   FAT32       C13B-8A5E                             849.4M    17% /boot
├─nvme0n1p2 swap   1           d73d48cb-2d01-4ab7-91e7-af843ef2bed6                [SWAP]
└─nvme0n1p3 ext4   1.0         a7ce6abf-71a2-4e1b-8d7a-291206dcc898  406.2G     7% /
```

2. Wipe it and verify 

```bash
$ mkfs.vfat -I -F 32 -n RPI_BOOT /dev/sdb
$ lsblk -f

NAME        FSTYPE FSVER LABEL    UUID                                 FSAVAIL FSUSE% MOUNTPOINTS
sda                                                                                   
sdb         vfat   FAT32 RPI_BOOT 7A51-1215                                           
sdc                                                                                   
nvme0n1                                                                               
├─nvme0n1p1 vfat   FAT32          C13B-8A5E                             849.4M    17% /boot
├─nvme0n1p2 swap   1              d73d48cb-2d01-4ab7-91e7-af843ef2bed6                [SWAP]
└─nvme0n1p3 ext4   1.0            a7ce6abf-71a2-4e1b-8d7a-291206dcc898  406.2G     7% /
```

> :warning: Double check to prevent regrets

#### Writing to SD card

1. Install `rpi-imager` using your preferred package manager

```bash
$ pacman -S rpi-imager
```

2. Follow the `gui` and install the preferred OS version based on the specific needs

> I have a backup of the `buster` image on my local machine

3. Mount the SD card and `cd` into the boot partition. Create an empty `ssh` file to 
allow `ssh` (this file will be deleted after the first boot)

```bash
$ touch ssh
```

4. Create `wpa_supplicant.conf` and **configure it**, so that it receives a local IP from our router. 

```bash
$ nvim wpa_supplicant.conf
```

5. If `.local` resolution does not work, use `nmap` and enumerate over the LAN subnet

```bash
$ ip addr
$ nmap -sn xxxxx
```

### Set up 

On RPI, install the requirements

```bash
$ python3 -m venv .venv 
$ source .venv/bin/activate
$ pip install -r requirements.txt
```

### :warning: WARNING :warning:

Files like `consts.py`, `settings.py` etc. are directly lifted, we need to change accordingly. The
following is to set up the RPI environment, not for your local machines

### MAC address of A7

```bash
90:EE:C7:E7:D3:72
```

---

### Communication 


Messages between the Android app and Raspi will be in the following format:

```json
{"cat": "xxx", "value": "xxx"}
```

The `cat` (for category) field with the following possible values:
- `info`: general messages
- `error`: error messages, usually in response of an invalid action
- `location`: the current location of the robot (in Path mode)
- `image-rec`: image recognition results
- `mode`: the current mode of the robot (`manual` or `path`)
- `status`: status updates of the robot (`running` or `finished`)
- `obstacle`: list of obstacles

#### Android to RPi

##### Set Obstacles

The contents of `obstacles` together with the configured turning radius (`settings.py`) will be passed to the Algorithm API.

```json
{
    "cat": "obstacles",
    "value": {
        "obstacles": [{"x": 5, "y": 10, "id": 1, "d": 2}],
        "mode": "0"
    }
}
```

RPi will store the received commands and path and make a call to the Algorithm API

##### Start

Signals to the robot to start dispatching the commands (when obstacles were set).

```json
{"cat": "control", "value": "start"}
```

If there are no commands in the queue, the RPi will respond with an error:

```json
{"cat": "error", "value": "Command queue is empty, did you set obstacles?"}
```

#### rpi to Android

##### Image recognition results

```json
{"cat": "image-rec", "value": {"image_id": "A", "obstacle_id":  "1"}}
```

##### Location Updates (ppi to Android)

In Path mode, the robot will periodically notify Android with the updated location of the robot.
```json
{"cat": "location", "value": {"x": 1, "y": 1, "d": 0}}
```
where `x`, `y` is the location of the robot, and `d` is its direction.

