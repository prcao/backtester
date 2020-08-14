package pcao.model.portfolio;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pcao.model.data.StockInfo;
import pcao.model.marketorder.MarketOrder;
import pcao.model.marketorder.MarketOrder.MarketOrderStatus;
import pcao.model.marketorder.event.MarketEvent;
import pcao.model.marketorder.event.MarketEvent.MarketEventType;
import pcao.model.time.TimeUtil;
import pcao.model.util.Logger;
import pcao.model.util.StockUtil;

public class PortfolioSnapshot {

    private PortfolioSnapshot previousSnapshot;
    public Portfolio portfolio;
    public String date;
    public HashMap<String, Double> positions;
    public double cash;
    public List<MarketOrder> outstandingOrders = new ArrayList<>();

    public PortfolioSnapshot(PortfolioSnapshot snapshot) {
        this(snapshot, snapshot.date);
    }

    public PortfolioSnapshot(PortfolioSnapshot snapshot, String newDate) {
        this(snapshot.portfolio, newDate, snapshot.positions, snapshot.cash);
        outstandingOrders = new ArrayList<>(snapshot.outstandingOrders);
    }

    public PortfolioSnapshot(Portfolio portfolio, String date, HashMap<String, Double> positions, double cash) {
        this.portfolio = portfolio;
        this.date = date;
        this.positions = new HashMap<>(positions);
        this.cash = cash;
    }

    // get total value of this snapshot
    public double getValue() {

        double total = cash;

        for (Map.Entry<String, Double> position : positions.entrySet()) {
            String stock = position.getKey();

            double quantity = position.getValue();
            StockInfo quote = StockUtil.getQuote(stock, date);

            if (quote == null) {
                Logger.log("Couldn't get quote for ticker " + stock + " date " + date);
                continue;
            }

            double price = quote.getClose();
            total += price * quantity;
        }

        return total;
    }

    // try to execute all outstanding orders/update status of all outstanding orders
    public void updateAndExecuteOutstandingOrders() {

        LinkedList<MarketOrder> completedOrders = new LinkedList<>();
        for (MarketOrder order : outstandingOrders) {

            // check if order needs to be expired
            // null == does not expire
            if (order.getExpirationDate() != null) {

                Date expiration = TimeUtil.parse(order.getExpirationDate());
                Date now = TimeUtil.parse(date);

                if (expiration.compareTo(now) <= 0) {
                    order.setMarketOrderStatus(MarketOrderStatus.EXPIRED);
                    portfolio.addHistoryEvent(new MarketEvent(order, MarketEventType.EXPIRED, date));
                    continue;
                }
            }

            // attempt to execute order
            if (order.execute(this)) {
                completedOrders.add(order);
            }
        }


        //remove completed orders
        // for(MarketOrder order : completedOrders) {
        //     outstandingOrders.remove(order);
        // }
        outstandingOrders = 
            outstandingOrders
                .stream()
                .filter(order -> order.getMarketOrderStatus() == MarketOrderStatus.OPEN)
                .collect(Collectors.toList());
    }

    // queue market order, but first try to execute it
    public void queueOrderAtOpen(MarketOrder order) {

        if (!order.execute(this)) {
            outstandingOrders.add(order);
        }
    }

    // queue market order without trying to execute
    public void queueOrderEOD(MarketOrder order) {
        outstandingOrders.add(order);
    }

    public PortfolioSnapshot getPreviousSnapshot() {
        return getPreviousSnapshot(1);
    }

    public PortfolioSnapshot getPreviousSnapshot(int numDays) {

        if (numDays <= 0) {
            throw new IllegalArgumentException("Can't get the <= 0 previous snapshot");
        }

        PortfolioSnapshot snapshot = this;

        for (int i = 0; i < numDays; i++) {

            if (snapshot == null) {
                return null;
            }

            snapshot = snapshot.previousSnapshot;
        }

        return snapshot;
    }

    public void setPreviousSnapshot(PortfolioSnapshot previousSnapshot) {
        this.previousSnapshot = previousSnapshot;
    }

    public String toString() {
        return positions.toString();
    }
}