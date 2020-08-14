package pcao.model.strategy;

import pcao.model.portfolio.PortfolioSnapshot;

public interface Strategy {
    public PortfolioSnapshot execute(PortfolioSnapshot init, String currentDatestamp);
}