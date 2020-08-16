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
    private MarketEventType eventType;
    private String date, message;

    public MarketEvent(MarketOrder order, MarketEventType eventType, String date) {
        this(order, eventType, date, null);
    }

    public MarketEvent(MarketOrder order, MarketEventType eventType, String date, String message) {
        this.order = order;
        this.eventType = eventType;
        this.date = date;
        this.message = message;
    }

    public String getJSON() {
        return StockUtil.gson.toJson(this);
    }

    public String toString() {
        return "[" + date + "]" + order.toString() + " " + eventType.toString() + " | " + message;
    }
}