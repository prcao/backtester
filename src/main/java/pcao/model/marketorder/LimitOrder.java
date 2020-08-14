package pcao.model.marketorder;

import pcao.model.portfolio.PortfolioSnapshot;

public abstract class LimitOrder extends MarketOrder {

    protected double limitPrice;

    public LimitOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate) {
        super(snapshot, ticker, quantity, openDate);
        this.limitPrice = limitPrice;
    }

    @Override
    public String toString() {
        return super.toString() + " @" + limitPrice;
    }
}