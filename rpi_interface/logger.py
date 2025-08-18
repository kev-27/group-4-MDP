import logging.config
import logging.handlers
import atexit
import json
import pathlib

logger = logging.getLogger(__name__)

def setup_logging():
    config_file = pathlib.Path("configs/logging_configs.json")
    with open(config_file) as file:
        config = json.load(file)
    logging.config.dictConfig(config)
    queue_handler = logging.getHandlerByName("queue_handler")
    if queue_handler is not None:
        queue_handler.listener.start()
        atexit.register(queue_handler.listener.stop)

def main():
    setup_logging()

    # boilerplates to test functions
    """
    logging.basicConfig(level="INFO")
    logger.debug("debug message")
    logger.info("info message")
    logger.warning("warning message")
    logger.error("error message")
    logger.critical("critical message")
    try:
        1 / 0
    except ZeroDivisionError:
        logger.exception("exception message")
    """

if __name__ == "__main__":
    main()

