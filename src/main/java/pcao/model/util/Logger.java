package pcao.model.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Logger {

    private static final String DEFAULT_LOGGER_DEST = "logs.txt", DEFAULT_ERROR_DEST = "errors.txt", DEFAULT_RESULTS_DEST = "results.txt";
    private static PrintWriter logger, error, results;

    public static void init() {
        try {
            logger = new PrintWriter(DEFAULT_LOGGER_DEST);
            error = new PrintWriter(DEFAULT_ERROR_DEST);
            results = new PrintWriter(DEFAULT_RESULTS_DEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setLoggerDestination(String fileName) {
        logger.close();
        try {
            logger = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setErrorDestination(String fileName) {
        error.close();
        try {
            error = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setResultsDest(String fileName) {
        results.close();
        try {
            results = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void log(String text) {
        logger.println(text);
        logger.flush();
    }

    public static void error(String text) {
        error.println(text);
        error.flush();
    }

    public static void error(Exception e) {
        e.printStackTrace(error);
    }

    public static void result(String date, double value) {
        results.println(date + " " + value);
        results.flush();
    }

    public static void close() {
        logger.close();
        error.close();
        results.close();
    }
}