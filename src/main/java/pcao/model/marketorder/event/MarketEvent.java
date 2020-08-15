package pcao.model.marketorder.event;

import pcao.model.marketorder.MarketOrder;
import pcao.model.util.StockUtil;

/**
 * This class is for logging purposes, Records the opening or execution of a
 * trade
 */
public class MarketEvent {
    public enum MarketEventType {
        OPENED, EXECUTED, FAILED_TO_EXECUTE, EXPIRED;
    }

    private MarketOrder order;
    private MarketEventType type;
    private String date;

    public MarketEvent(MarketOrder order, MarketEventType type, String date) {
        this.order = order;
        this.type = type;
        this.date = date;
    }

    public String getJSON() {
        return StockUtil.gson.toJson(this);
    }

    public String toString() {
        return "[" + date + "]" + order.toString() + " " + type.toString();
    }
}