package pcao.server;

import java.util.HashMap;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import pcao.model.data.DataSet;
import pcao.model.portfolio.Portfolio;
import pcao.model.portfolio.PortfolioSnapshot;
import pcao.model.strategy.ExampleStrategy;

public class BacktesterServer {

    public static DataSet ds;

    public static void init() {

        HashMap<String, Double> positions = new HashMap<>();
        // PortfolioSnapshot initialSnapshot = new PortfolioSnapshot("2019/01/02", positions, 10000);
        // Portfolio p = new Portfolio(initialSnapshot, new ExampleStrategy("INTC"));
        // ds = p.getData();
    }

    public static void main(String[] args) throws Exception {

        init();

        Server server = new Server(8080);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(APIServlet.class, "/api");
        server.start();
        server.join();
    }
}