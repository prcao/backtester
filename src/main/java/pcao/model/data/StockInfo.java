package pcao.model.data;

import pcao.model.time.TimeUtil;
import yahoofinance.histquotes.HistoricalQuote;

public class StockInfo {

    private String symbol, date;
    private double open, low, high, close, adjClose;
    private long volume;

    public StockInfo() { }

    public StockInfo(HistoricalQuote hq) {
        this.symbol = hq.getSymbol();
        this.date = TimeUtil.FORMAT.format(hq.getDate().getTime());
        this.open = hq.getOpen().doubleValue();
        this.low = hq.getLow().doubleValue();
        this.high = hq.getHigh().doubleValue();
        this.close = hq.getClose().doubleValue();
        this.adjClose = hq.getAdjClose().doubleValue();
        this.volume = hq.getVolume();
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }

    public double getClose() {
        return close;
    }

    public double getAdjClose() {
        return adjClose;
    }

    public long getVolume() {
        return volume;
    }
}