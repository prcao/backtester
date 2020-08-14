package pcao.model.marketorder;

import pcao.model.data.StockInfo;
import pcao.model.portfolio.PortfolioSnapshot;
import pcao.model.util.StockUtil;

public class LimitBuyOrder extends LimitOrder {

    public LimitBuyOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate) {
        super(snapshot, ticker, quantity, limitPrice, openDate, snapshot.date);
    }

    public LimitBuyOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate, String expirationDate) {
        super(snapshot, ticker, quantity, limitPrice, openDate, expirationDate);
    }

    protected boolean executeHelper(PortfolioSnapshot snapshot) {

        StockInfo info = StockUtil.getQuote(ticker, snapshot.date);
        
        // if price does not reach limit price, fail to execute
        if(info.getLow() > limitPrice) {
            return false;
        }

        // if not enough money, fail to execute
        if(snapshot.cash < limitPrice * quantity) {
            return false;
        }

        snapshot.cash -= limitPrice * quantity;
        snapshot.positions.put(ticker, snapshot.positions.getOrDefault(ticker, 0.0) + quantity);
        return true;
    }

    @Override
    public String toString() {
        return "BUY " + super.toString();
    }
}