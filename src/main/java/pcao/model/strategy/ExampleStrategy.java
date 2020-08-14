package pcao.model.strategy;

import java.util.HashMap;

import pcao.model.data.StockInfo;
import pcao.model.portfolio.PortfolioSnapshot;
import pcao.model.util.StockUtil;

// This strategy will all in AMD on red days and sell all AMD on green days
public class ExampleStrategy implements Strategy {

    private String stock;

    public ExampleStrategy(String stock) {
        this.stock = stock;
    }

    @Override
    public PortfolioSnapshot execute(PortfolioSnapshot init, String currentDatestamp) {

        HashMap<String, Double> positions = init.positions;
        double currentCash = init.cash;
        if(init.getPreviousSnapshot() == null) {
            return new PortfolioSnapshot(init.portfolio, currentDatestamp, positions, currentCash);
        }

        StockInfo prev = StockUtil.getQuote(stock, init.getPreviousSnapshot().date);
        StockInfo curr = StockUtil.getQuote(stock, init.date);

        double closePrice = curr.getClose();
        

        if(prev.getClose() < prev.getOpen()) {
            // red day, buy
            int quantityToBuy = (int) (init.cash / closePrice);
            positions.put(stock, positions.get(stock) + quantityToBuy);
            currentCash -= closePrice * quantityToBuy;
        } else {
            // green day, sell
            currentCash += closePrice * positions.getOrDefault(stock, 0.0);
            positions.put(stock, 0.0);
        }

        return new PortfolioSnapshot(init.portfolio, currentDatestamp, positions, currentCash);
    }
    
}