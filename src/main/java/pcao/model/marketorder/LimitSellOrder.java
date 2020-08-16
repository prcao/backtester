package pcao.model.marketorder;

import pcao.model.data.StockInfo;
import pcao.model.portfolio.PortfolioSnapshot;
import pcao.model.util.StockUtil;

public class LimitSellOrder extends LimitOrder {

    public LimitSellOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate) {
        super(snapshot, ticker, quantity, limitPrice, openDate, snapshot.date);
    }

    public LimitSellOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate, String expirationDate) {
        super(snapshot, ticker, quantity, limitPrice, openDate, expirationDate);
    }

    protected MarketOrderExecutionResult executeHelper(PortfolioSnapshot snapshot) {

        StockInfo info = StockUtil.getQuote(ticker, snapshot.date);
        
        // if price does not reach limit price, fail to execute
        if(info.getHigh() > limitPrice) {
            return new MarketOrderExecutionResult(false, "Stock price did not hit limit price");
        }

        // if not enough stonks, fail to execute
        double numShares = snapshot.positions.getOrDefault(ticker, 0.0);

        if(numShares < quantity) {
            return new MarketOrderExecutionResult(false, "Trying to sell " + quantity + " shares, but only own " + numShares + " shares");
        }

        snapshot.cash += limitPrice * quantity;
        snapshot.positions.put(ticker, snapshot.positions.getOrDefault(ticker, 0.0) - quantity);
        return new MarketOrderExecutionResult(true);
    }

    protected MarketOrderType getType() {
        return MarketOrderType.SELL;
    }

    @Override
    public String toString() {
        return "SELL " + super.toString();
    }
}