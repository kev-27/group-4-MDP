from communication.android import AndroidLink 

def test_init():
    instance = AndroidLink()
    AndroidLink().connect()

test_init()
