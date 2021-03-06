package pcao.model.portfolio;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import pcao.model.data.DataPoint;
import pcao.model.data.DataSet;
import pcao.model.marketorder.event.MarketEvent;
import pcao.model.strategy.Strategy;
import pcao.model.time.MarketDay;
import pcao.model.time.TimeUtil;
import pcao.model.util.Logger;
import pcao.model.util.StockUtil;

public class Portfolio {

    public TreeMap<String, PortfolioSnapshot> portfolio = new TreeMap<>();
    private List<MarketEvent> history = new LinkedList<>();
    private Strategy strategy;

    public Portfolio(Strategy strategy) {
        this.strategy = strategy;
    }

    public void backtest(PortfolioSnapshot initialSnapshot) {
        
        TreeSet<MarketDay> datestamps = TimeUtil.getMarketDays(initialSnapshot.date);

        // fill snapshots til today
        PortfolioSnapshot snapshot = initialSnapshot;
        int complete = 0;
        for (MarketDay day : datestamps) {

            String datestamp = day.getDate();

            // might be a holiday, idk
            if (StockUtil.getQuote(TimeUtil.OLD_STOCK, datestamp) != null) {

                PortfolioSnapshot prevSnapshot = snapshot;

                // execute outstanding orders, then strategy
                PortfolioSnapshot result = new PortfolioSnapshot(prevSnapshot, datestamp);
                result.updateAndExecuteOutstandingOrders();
                result = new PortfolioSnapshot(strategy.execute(result, datestamp), datestamp);

                //set result data
                result.setPreviousSnapshot(snapshot);
                portfolio.put(datestamp, result);
                
                snapshot = result;
            }

            complete++;
            Logger.log("Completed day " + complete + " out of " + datestamps.size());
        }

        Logger.log("Completed calculations of portfolio");
        System.out.println("Final value: " + snapshot.value);
    }

    public void addHistoryEvent(MarketEvent event) {
        history.add(event);
    }

    public List<MarketEvent> getHistory() {
        return history;
    }

    public String getHistoryJSON() {
        return StockUtil.gson.toJson(history);
    }

    public String getSnapshotJSON() {
        return StockUtil.gson.toJson(portfolio.values());
    }

    public DataSet getData() {
        DataSet ds = new DataSet();

        for (String datestamp : portfolio.keySet()) {
            ds.addData(new DataPoint(datestamp, portfolio.get(datestamp).value));
        }

        return ds;
    }
}