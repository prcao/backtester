package pcao.model.time;

public class MarketDay implements Comparable<MarketDay> {
    private String date;
    private long unixTime;

    public MarketDay(String date) {
        this.date = date;
        this.unixTime = TimeUtil.parse(date).getTime();
    }

    public String getDate() {
        return date;
    }

    @Override
    public int compareTo(MarketDay o) {
        return Long.compare(unixTime, o.unixTime);
    }

    @Override
    public String toString() {
        return date;
    }
}