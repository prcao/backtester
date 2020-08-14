package pcao.model.portfolio.position;

public class Position {
    public String stock;
    public double quantity, costBasis;

    public Position(String stock, double quantity, double price) {
        this.stock = stock;
        this.quantity = quantity;
    }
}