import logging.config
import logging.handlers
import atexit
import json
import pathlib

logger = logging.getLogger("root")

def setup_logging():

    BASE_DIR = pathlib.Path(__file__).resolve().parent # "logger/"
    CONFIG_FILE = BASE_DIR / "logging_configs.json"
    LOG_DIR = BASE_DIR / "logs"    
    LOG_DIR.mkdir(parents=True, exist_ok=True)

    with open(CONFIG_FILE) as file:
        config = json.load(file)

    # make log file paths absolute
    for handler in config.get("handlers", {}).values():
        if "filename" in handler:
            handler["filename"] = str(LOG_DIR / pathlib.Path(handler["filename"]).name)
    logging.config.dictConfig(config)

    queue_handler = logging.getHandlerByName("queue_handler")
    if queue_handler is not None:
        queue_handler.listener.start()
        atexit.register(queue_handler.listener.stop)

setup_logging()

def main() -> None:
    setup_logging()

if __name__ == "__main__":
    main()

