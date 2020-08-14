package pcao.model.data;

import pcao.model.time.TimeUtil;

public class DataPoint {
    
    private long time;
    private double price;

    public DataPoint(String datestamp, double price) {
        this.time = TimeUtil.parse(datestamp).getTime();
        this.price = price;
    }

    public long getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }
}