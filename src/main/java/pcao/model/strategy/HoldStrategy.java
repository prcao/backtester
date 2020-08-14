package pcao.model.strategy;

import pcao.model.portfolio.PortfolioSnapshot;

// Do nothing, just hold
public class HoldStrategy implements Strategy {

    @Override
    public PortfolioSnapshot execute(PortfolioSnapshot init, String currentDatestamp) {
        return new PortfolioSnapshot(init.portfolio, currentDatestamp, init.positions, init.cash);
    }
    
}