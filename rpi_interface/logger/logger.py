import logging
import logging.config
import logging.handlers
import atexit
import json
import pathlib
import queue

logger = logging.getLogger("root")

def setup_logging():
    BASE_DIR = pathlib.Path(__file__).resolve().parent  # "logger/"
    CONFIG_FILE = BASE_DIR / "logging_configs.json"
    LOG_DIR = BASE_DIR / "logs"
    LOG_DIR.mkdir(parents=True, exist_ok=True)

    # Load the JSON config
    with open(CONFIG_FILE) as file:
        config = json.load(file)

    # Make log file paths absolute
    for handler in config.get("handlers", {}).values():
        if "filename" in handler:
            handler["filename"] = str(LOG_DIR / pathlib.Path(handler["filename"]).name)

    # Apply the dictConfig **without the queue handler**
    logging.config.dictConfig(config)

    # Set up a QueueHandler and QueueListener for async logging
    log_queue = queue.Queue()

    # This will send all messages to the root logger's handlers
    queue_handler = logging.handlers.QueueHandler(log_queue)
    root_logger = logging.getLogger()
    root_logger.addHandler(queue_handler)

    # Collect all existing handlers except the QueueHandler itself
    handlers = [h for h in root_logger.handlers if h is not queue_handler]

    listener = logging.handlers.QueueListener(log_queue, *handlers)
    listener.start()
    atexit.register(listener.stop)

setup_logging()

def main() -> None:
    setup_logging()

if __name__ == "__main__":
    main()

