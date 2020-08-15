package pcao.model;

import pcao.model.time.TimeUtil;
import pcao.model.util.Logger;
import pcao.model.util.StockUtil;

public class Backtester {
    public static void init() {
        Logger.init();
        StockUtil.init();
        TimeUtil.init();
    }

    public static void close() {
        Logger.close();
        StockUtil.shutdown();
        TimeUtil.shutdown();
    }
}