package pcao.model.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import pcao.model.util.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class TimeUtil {
    public static final String OLD_STOCK = "MSFT";
    public static final String OPEN_MARKET_DAYS = "open_market_days";
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static TreeSet<MarketDay> marketDays;
    private static JedisPool jedisPool;

    public static void init() {
        jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
        marketDays = getMarketDays();
    }

    public static void shutdown() {
        jedisPool.close();
    }

    public static String previousMarketDay(String currentDate) {
        return marketDays.lower(new MarketDay(currentDate)).getDate();
    }

    public static Date parse(String datetime) {
        try {
            return FORMAT.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static TreeSet<MarketDay> getMarketDays(String fromDateString) {

        Jedis jedis = jedisPool.getResource();

        Date from = TimeUtil.parse(fromDateString);
        Date now = new Date();
        String minDateString = jedis.zrange(OPEN_MARKET_DAYS, 0, 0).iterator().next();
        Date minDate = TimeUtil.parse(minDateString);

        if (from.compareTo(minDate) < 0) {
            Logger.error("WARNING: trying to get market days from " + fromDateString + " bit mindate in redis cache is "
                    + minDateString);
        }

        Set<String> dates = jedis.zrangeByScore(OPEN_MARKET_DAYS, from.getTime(), now.getTime());
        TreeSet<MarketDay> marketDays = new TreeSet<>();

        for (String date : dates) {
            marketDays.add(new MarketDay(date));
        }

        jedis.close();

        return marketDays;
    }

    public static TreeSet<MarketDay> getMarketDays() {

        Jedis jedis = jedisPool.getResource();
        String min = jedis.zrange(OPEN_MARKET_DAYS, 0, 0).iterator().next();
        jedis.close();

        return getMarketDays(min);
    }
}