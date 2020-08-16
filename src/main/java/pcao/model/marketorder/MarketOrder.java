package pcao.model.marketorder;

import pcao.model.marketorder.event.MarketEvent;
import pcao.model.marketorder.event.MarketEvent.MarketEventType;
import pcao.model.portfolio.PortfolioSnapshot;

public abstract class MarketOrder {

    public enum MarketOrderStatus {
        FILLED, EXPIRED, OPEN;
    }

    public enum MarketOrderType {
        BUY, SELL;
    }

    protected MarketOrderType orderType;
    protected MarketOrderStatus status;
    protected String ticker, openDate, expirationDate;
    protected double quantity;

    // we don't store snapshot. we add it solely for logging purposes
    public MarketOrder(PortfolioSnapshot snapshot, String ticker, double quantity, String openDate, String expirationDate) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.openDate = openDate;
        this.expirationDate = expirationDate;
        this.status = MarketOrderStatus.OPEN;

        this.orderType = getType();

        snapshot.portfolio.addHistoryEvent(new MarketEvent(this, MarketEventType.OPENED, openDate));
    }

    protected abstract MarketOrderType getType();

    protected abstract boolean executeHelper(PortfolioSnapshot snapshot);

    public boolean execute(PortfolioSnapshot snapshot) {

        boolean success = executeHelper(snapshot);

        if (success) {
            setMarketOrderStatus(MarketOrderStatus.FILLED);
            snapshot.portfolio.addHistoryEvent(new MarketEvent(this, MarketEventType.EXECUTED, snapshot.date));
        } else {
            snapshot.portfolio.addHistoryEvent(new MarketEvent(this, MarketEventType.FAILED_TO_EXECUTE, snapshot.date));
        }

        return success;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setMarketOrderStatus(MarketOrderStatus status) {
        this.status = status;
    }

    public MarketOrderStatus getMarketOrderStatus() {
        return status;
    }

    @Override
    public String toString() {
        return quantity + " " + ticker;
    }
}