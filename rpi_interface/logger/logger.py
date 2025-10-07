import logging
import logging.config
import logging.handlers
import atexit
import json
import pathlib
import queue

logger = logging.getLogger("root")


def setup_logging():
    """Set up logging using JSON config + optional async QueueHandler for non-blocking logging."""
    BASE_DIR = pathlib.Path(__file__).resolve().parent
    CONFIG_FILE = BASE_DIR / "logging_configs.json"
    LOG_DIR = BASE_DIR / "logs"
    LOG_DIR.mkdir(parents=True, exist_ok=True)

    # Load JSON config
    with open(CONFIG_FILE) as file:
        config = json.load(file)

    # Make log file paths absolute
    for handler in config.get("handlers", {}).values():
        if "filename" in handler:
            handler["filename"] = str(LOG_DIR / pathlib.Path(handler["filename"]).name)

    # Apply dictConfig
    logging.config.dictConfig(config)

    # Set up QueueHandler for non-blocking logging
    log_queue = queue.Queue()
    queue_handler = logging.handlers.QueueHandler(log_queue)

    root_logger = logging.getLogger()

    # Store existing handlers from dictConfig, then remove them from root logger
    handlers = root_logger.handlers[:]
    root_logger.handlers = []

    # Attach only the QueueHandler to root
    root_logger.addHandler(queue_handler)

    # Create QueueListener to dispatch messages to original handlers
    listener = logging.handlers.QueueListener(log_queue, *handlers)
    listener.start()

    # Stop listener on program exit
    atexit.register(listener.stop)


if __name__ == "__main__":
    setup_logging()
    logger.info("Logger setup complete.")
