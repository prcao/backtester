package pcao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import pcao.model.Backtester;
import pcao.model.data.StockInfo;
import pcao.model.marketorder.LimitBuyOrder;
import pcao.model.marketorder.LimitSellOrder;
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

        Backtester.init();

        MarketScannerCondition condition = (String ticker, String date) -> {

            String lastMarketDay = TimeUtil.previousMarketDay(date);
            StockInfo info = StockUtil.getQuote(ticker, date);
            StockInfo lastInfo = StockUtil.getQuote(ticker, lastMarketDay);

            if(info == null || lastInfo == null) return false;

            return info.getVolume() > 500 * lastInfo.getVolume();
        };
  

        Strategy strategy = (PortfolioSnapshot snapshot, String date) -> {
            
            String stonk = "AMD";
            StockInfo stockInfo = StockUtil.getQuote(stonk, date);
            double open = stockInfo.getOpen();
            double close = stockInfo.getClose();

            PortfolioSnapshot eod = new PortfolioSnapshot(snapshot);

            if(snapshot.cash > 0) {
                eod.executeOrderAndQueue(new LimitBuyOrder(eod, stonk, 100, close, date));
            }
        
             return eod;
        };

        // strategy = new HoldStrategy();
        
        HashMap<String, Double> initialPositions = new HashMap<>();

        Portfolio p = new Portfolio(strategy);
        PortfolioSnapshot init = new PortfolioSnapshot(p, "2020-06-01", initialPositions, 100000);
        p.backtest(init);
        System.out.println(p.getHistoryJSON());
        // System.out.println(p.getSnapshotJSON());
        //p.getData().saveJSONToFile("results.txt");
        Backtester.close();
    }
}
