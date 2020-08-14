package pcao.model.util;

public interface MarketScannerCondition {
    public boolean validate(String ticker, String date);
}