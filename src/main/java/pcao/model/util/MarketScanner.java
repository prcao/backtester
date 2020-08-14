package pcao.model.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MarketScanner {

    // NON MULTITHREADED
    public static List<String> scan(MarketScannerCondition condition, String date) {

        List<String> stocks = StockUtil.getAllStocks();
        List<String> results = new LinkedList<>();

        for (String stock : stocks) {
            if (condition.validate(stock, date)) {
                results.add(stock);
            }
        }

        return results;
    }

    public static List<String> scan(MarketScannerCondition condition, String date, boolean multithreading) {

        if (!multithreading) {
            return scan(condition, date);
        }

        int numCores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(numCores);
        List<String> stocks = StockUtil.getAllStocks();
        List<String> results = new LinkedList<>();

        List<Future<?>> threads = new LinkedList<>();

        for (String stock : stocks) {

            Future<?> task = pool.submit(() -> {
                if (condition.validate(stock, date)) {
                    synchronized (results) {
                        results.add(stock);
                    }
                }
            });

            threads.add(task);
        }

        for (Future<?> thread : threads) {
            try {
                thread.get();
            } catch (InterruptedException | ExecutionException e) {
                Logger.error(e);
            }
        }

        return results;
    }
}