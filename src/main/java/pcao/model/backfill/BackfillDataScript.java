package pcao.model.backfill;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pcao.model.time.TimeUtil;
import pcao.model.util.Logger;
import pcao.model.util.StockUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class BackfillDataScript {

    final static int NUM_CORES = Runtime.getRuntime().availableProcessors();
    static JedisPool jedisPool;
    static Date beginningOfTimeDate = TimeUtil.parse("1950-01-02");
    static PrintWriter failedWriter;
    static Integer numCompleted = 0, numErrors = 0;

    static class SingleTickerBackfillRunnable implements Runnable {

        String ticker;

        SingleTickerBackfillRunnable(String ticker) {
            this.ticker = ticker;
        }

        public void run() {

            Jedis jedis = jedisPool.getResource();

            try {
                Calendar beginningOfTime = Calendar.getInstance();
                beginningOfTime.setTime(beginningOfTimeDate);
                Stock stock = YahooFinance.get(ticker, beginningOfTime, Calendar.getInstance(), Interval.DAILY);
                List<HistoricalQuote> quotes = stock.getHistory();

                Transaction tx = jedis.multi();

                for (HistoricalQuote hq : quotes) {
                    String datestamp = null;

                    try {
                        datestamp = TimeUtil.FORMAT.format(hq.getDate().getTime());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Logger.error("Array index OOB exception for datestamp " + datestamp + " ticker " + ticker);
                        Logger.error(e);
                        continue;
                    }

                    if (datestamp == null || hq.getOpen() == null) {
                        Logger.error(ticker + " " + datestamp + " is null!");
                        continue;
                    }

                    StringBuilder json = new StringBuilder("{");
                    json.append("\"date\":\"").append(datestamp).append("\",");
                    json.append("\"open\":").append(hq.getOpen().doubleValue()).append(",");
                    json.append("\"high\":").append(hq.getHigh().doubleValue()).append(",");
                    json.append("\"low\":").append(hq.getLow().doubleValue()).append(",");
                    json.append("\"close\":").append(hq.getClose().doubleValue()).append(",");
                    json.append("\"adjClose\":").append(hq.getAdjClose().doubleValue()).append(",");
                    json.append("\"volume\":").append(hq.getVolume().doubleValue()).append(",");
                    json.append("\"symbol\":\"").append(hq.getSymbol()).append("\"}");

                    tx.hset(ticker, datestamp, json.toString());
                }

                tx.exec();

                synchronized (numCompleted) {
                    numCompleted++;
                    Logger.log("Completed " + ticker + " #" + numCompleted);
                }
            } catch (Exception e) {
                synchronized (numErrors) {
                    numErrors++;
                    Logger.error("Error getting data for ticker " + ticker);
                    failed(ticker);
                    if (e != null) {
                        Logger.error(e);
                    }
                }
            } finally {
                jedis.close();
            }
        }
    }

    public static void failed(String text) {
        try {
            failedWriter.println(text);
            failedWriter.flush();
        } catch (Exception e) {
            if (e != null) {
                Logger.error(e.getMessage());
            }
        }
    }

    public static void backfillDates() {

        Logger.log("Backfilling dates");

        Jedis jedis = jedisPool.getResource();
        Set<String> dates = jedis.hkeys(TimeUtil.OLD_STOCK);

        for (String date : dates) {
            if (jedis.zrank(TimeUtil.OPEN_MARKET_DAYS, date) == null) {
                long unixTime = TimeUtil.parse(date).getTime();
                jedis.zadd(TimeUtil.OPEN_MARKET_DAYS, unixTime, date);
            }
        }

        Logger.log("Done backfilling " + dates.size() + " dates");

        jedis.close();
    }

    public static void backfill(String tickersFileName, int numThreads) throws Exception {

        numCompleted = 0;
        numErrors = 0;

        // append
        PrintWriter benchmarkResultsWriter = new PrintWriter(new FileOutputStream(new File("benchmark.txt"), true));

        long startTime = System.currentTimeMillis();
        File tickers = new File(tickersFileName);
        int numLines = 0;
        try (Scanner lineCounter = new Scanner(tickers)) {
            while (lineCounter.hasNextLine()) {
                numLines++;
                lineCounter.nextLine();
            }
        }

        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        HashMap<String, Future<?>> futures = new HashMap<>();

        Logger.log("Starting backfill of " + numLines + " tickers in " + tickersFileName + " using " + numThreads
                + " threads");

        try (Scanner scanner = new Scanner(tickers)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String ticker = line; // .split("\s")[0];
                Future<?> future = pool.submit(new SingleTickerBackfillRunnable(ticker));
                futures.put(ticker, future);
            }
        }

        for (String ticker : futures.keySet()) {
            try {
                Future<?> future = futures.get(ticker);
                future.get();
            } catch (ExecutionException e) {
                Logger.error(ticker + " task failed");
            }
        }

        long time = System.currentTimeMillis() - startTime;
        Logger.log("Completed backfill of " + numLines + " stocks");
        Logger.log("Time: " + time + " ms (" + (time / 60000) + " minutes)");
        Logger.log("Successes: " + numCompleted);
        Logger.log("Failures: " + numErrors);
        benchmarkResultsWriter.println(numThreads + " threads | " + time + "ms | " + numLines + " tickers");

        pool.shutdown();
        benchmarkResultsWriter.close();
    }

    public static void main(String[] args) throws Exception {

        Logger.init();
        StockUtil.init();

        jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
        failedWriter = new PrintWriter("failed.txt");
        Logger.log("Logging start\n");
        Logger.error("Error logs start\n");

        backfillDates();
        backfill("tickers.txt", NUM_CORES);

        jedisPool.close();
        Logger.close();
        failedWriter.close();
    }
}