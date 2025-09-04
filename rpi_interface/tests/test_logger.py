import logging
from logger.logger import setup_logging, logger


def init_logging():
    setup_logging()


def test_logger_basic_levels(caplog):
    with caplog.at_level(logging.INFO):
        logger.debug("debug message")  # should not appear
        logger.info("info message")
        logger.warning("warning message")
        logger.error("error message")
        logger.critical("critical message")

    assert "info message" in caplog.text
    assert "warning message" in caplog.text
    assert "error message" in caplog.text
    assert "critical message" in caplog.text
    assert "debug message" not in caplog.text


def test_logger_exception(caplog):
    with caplog.at_level(logging.ERROR):
        try:
            1 / 0
        except ZeroDivisionError:
            logger.exception("exception message")

    assert "exception message" in caplog.text
    assert "ZeroDivisionError" in caplog.text
