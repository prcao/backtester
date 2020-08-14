package pcao.server;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class APIServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = -5070374459116087007L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Allow-Methods", "POST, GET");
		response.addHeader("Access-Control-Allow-Headers", "Content-Type");

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");
		response.getWriter().println(BacktesterServer.ds.getJSONWithDatestamp());
	}

}