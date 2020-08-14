package pcao.model.marketorder;

import pcao.model.data.StockInfo;
import pcao.model.portfolio.PortfolioSnapshot;
import pcao.model.util.StockUtil;

public class LimitSellOrder extends LimitOrder {

    public LimitSellOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate) {
        super(snapshot, ticker, quantity, limitPrice, openDate);
    }

    protected boolean executeHelper(PortfolioSnapshot snapshot) {

        StockInfo info = StockUtil.getQuote(ticker, snapshot.date);
        
        // if price does not reach limit price, fail to execute
        if(info.getHigh() > limitPrice) {
            return false;
        }

        // if not enough stonks, fail to execute
        if(snapshot.positions.getOrDefault(ticker, 0.0) < quantity) {
            return false;
        }

        snapshot.cash += limitPrice * quantity;
        snapshot.positions.put(ticker, snapshot.positions.getOrDefault(ticker, 0.0) - quantity);
        return true;
    }

    @Override
    public String toString() {
        return "SELL " + super.toString();
    }
}