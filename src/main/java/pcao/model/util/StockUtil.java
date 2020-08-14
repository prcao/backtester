package pcao.model.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;

import pcao.model.data.StockInfo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class StockUtil {

    public static final String STOCK_LIST = "tickers.txt";

    private static List<String> listOfAllStocks;
    private static JedisPool jedisPool;
    private static Gson gson;

    public static void init() {
        jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
        gson = new Gson();
    }

    public static void shutdown() {
        jedisPool.close();
    }

    public static StockInfo getQuote(String ticker, String date) {

        Jedis jedis = jedisPool.getResource();
        String json = jedis.hget(ticker, date);

        StockInfo quote = gson.fromJson(json, StockInfo.class);

        if (quote == null) {
            Logger.error("Something went wrong while trying to get data for ticker " + ticker + " and date " + date);
            Logger.error("Make sure you're not trying to get a day when markets aren't open");
        }

        jedis.close();

        return quote;
    }

    public static List<String> getAllStocks() {

        if (listOfAllStocks != null) {
            return listOfAllStocks;
        }

        List<String> list = new LinkedList<>();

        Scanner in = null;
        try {
            in = new Scanner(new File(STOCK_LIST));
        } catch (FileNotFoundException e) {
            Logger.error(e);
            Logger.error("ERROR: " + STOCK_LIST + " was not found");
            return new LinkedList<>();
        }

        while (in.hasNextLine()) {
            list.add(in.nextLine());
        }

        in.close();

        listOfAllStocks = list;
        return list;
    }
}