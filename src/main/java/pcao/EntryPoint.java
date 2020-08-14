package pcao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import pcao.model.data.StockInfo;
import pcao.model.marketorder.LimitBuyOrder;
import pcao.model.portfolio.Portfolio;
import pcao.model.portfolio.PortfolioSnapshot;
import pcao.model.strategy.Strategy;
import pcao.model.time.TimeUtil;
import pcao.model.util.Logger;
import pcao.model.util.MarketScannerCondition;
import pcao.model.util.StockUtil;

/**
 * Entry point for this program
 */
public class EntryPoint {

    public static void main(String[] args) throws IOException, FileNotFoundException {

        StockUtil.init();
        Logger.init();
        TimeUtil.init();

        MarketScannerCondition condition = (String ticker, String date) -> {

            String lastMarketDay = TimeUtil.previousMarketDay(date);
            StockInfo info = StockUtil.getQuote(ticker, date);
            StockInfo lastInfo = StockUtil.getQuote(ticker, lastMarketDay);

            if(info == null || lastInfo == null) return false;

            return info.getVolume() > 500 * lastInfo.getVolume();
        };

        String stonk = "AMD";

        Strategy strategy = (PortfolioSnapshot snapshot, String date) -> {
            
            StockInfo stockInfo = StockUtil.getQuote(stonk, date);
            double open = stockInfo.getOpen();
            double close = stockInfo.getClose();
            double pctChange = 100 * (close - open) / open;

            PortfolioSnapshot eod = new PortfolioSnapshot(snapshot);

            if(snapshot.cash > 0) {
                eod.queueOrderAtOpen(new LimitBuyOrder(eod, stonk, 100, open, date));
            }
        
             return eod;
        };

        // strategy = new HoldStrategy();
        
        HashMap<String, Double> initialPositions = new HashMap<>();

        Portfolio p = new Portfolio(strategy);
        PortfolioSnapshot init = new PortfolioSnapshot(p, "2020-08-01", initialPositions, 100000);// PortfolioSnapshot.getAllCashPortfolio("2020-01-02", 100000);
        p.fill(init);

        p.getData().saveJSONToFile("results.txt");
        StockUtil.shutdown();
        Logger.close();
        TimeUtil.shutdown();
    }
}
