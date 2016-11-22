package nz.cri.gns.rapidalert.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/quakes/*")
public class RewriteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String url = req.getRequestURL().toString();
		
		int index = url.indexOf("/quakes/");
		if (index == -1) {
			//They asked for /quakes???
			resp.sendRedirect("index.jsp");
			return;
		}
		
		String slug = url.substring(index + "/quakes/".length());
		
		resp.sendRedirect("../detail.jsp?quake=" + slug);
		//Nicer way, but needs some work to get all the refs right, so stick with easy for now
		//req.getRequestDispatcher("/detail.jsp?id=" + slug).forward(req, resp);
	}
}
