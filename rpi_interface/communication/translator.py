from typing import Optional

def android_to_stm(cmd: str) -> Optional[str]:
    """
    Translate Android input into STM32 command string.
    """

    DEFAULT_DIST = 1000   # 1m 
    DEFAULT_PIVOT_TIME = 1000  # 1s pivot

    mapping = {
        "forward": f"F {DEFAULT_DIST}",
        "backward": f"B {DEFAULT_DIST}",
        "forward_left": f"F {DEFAULT_DIST}\nL",
        "forward_right": f"F {DEFAULT_DIST}\nR",
        "backward_left": f"B {DEFAULT_DIST}\nL",
        "backward_right": f"B {DEFAULT_DIST}\nR",
        "pivot_anticlockwise": f"P {DEFAULT_PIVOT_TIME}",   # anticlockwise pivot
        "pivot_clockwise": f"P {DEFAULT_PIVOT_TIME}",       # clockwise pivot
        "stop": "STOP"
    }

    return mapping.get(cmd)
