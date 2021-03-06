package pcao.model.marketorder;

import pcao.model.data.StockInfo;
import pcao.model.portfolio.PortfolioSnapshot;
import pcao.model.util.StockUtil;

public class LimitBuyOrder extends LimitOrder {

    public LimitBuyOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice,
            String openDate) {
        super(snapshot, ticker, quantity, limitPrice, openDate, snapshot.date);
    }

    public LimitBuyOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate,
            String expirationDate) {
        super(snapshot, ticker, quantity, limitPrice, openDate, expirationDate);
    }

    protected MarketOrderExecutionResult executeHelper(PortfolioSnapshot snapshot) {

        StockInfo info = StockUtil.getQuote(ticker, snapshot.date);

        // if price does not reach limit price, fail to execute
        if (info.getLow() > limitPrice) {
            return new MarketOrderExecutionResult(false, "Stock price did not hit limit price");
        }

        // if not enough money, fail to execute
        if (snapshot.cash < limitPrice * quantity) {
            return new MarketOrderExecutionResult(false, quantity + " shares costs " + limitPrice * quantity + ", but only holding $" + snapshot.cash);
        }

        snapshot.cash -= limitPrice * quantity;
        snapshot.positions.put(ticker, snapshot.positions.getOrDefault(ticker, 0.0) + quantity);
        return new MarketOrderExecutionResult(true);
    }

    protected MarketOrderType getType() {
        return MarketOrderType.BUY;
    }

    @Override
    public String toString() {
        return "BUY " + super.toString();
    }
}