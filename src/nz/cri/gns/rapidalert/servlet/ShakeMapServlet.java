package nz.cri.gns.rapidalert.servlet;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.org.riskscape.wrappers.hazard.quake.shakemap.ShakeMapParser;

@WebServlet(urlPatterns="/shakemap.png")
public class ShakeMapServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String slug = request.getParameter("event");
		
		InputStream in = ShakeMapParser.getXMLFor(slug);
		
		if (in == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		ShakeMapParser parser = new ShakeMapParser(in);
		double[][] pga = parser.processData("PGA").get(0);
		
		response.setContentType("image/png");
		createImage(pga, response.getOutputStream());
	}

	private void createImage(double[][] pga, OutputStream outputStream) throws IOException {
		BufferedImage image = new BufferedImage(pga.length, pga[0].length, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		int[] pixel = new int[4];
		pixel[3] = 128;	//50% alpha
		pixel[2] = 0; //Blue is always zero
		for (int i=0; i<pga.length; i++) {
			for (int j=0; j<pga[0].length; j++) {
				double value = pga[i][j] / 100;
				if (value < 0.1) {
					pixel[0] = 0;
					pixel[1] = 255;
				} else if (value < 0.15) {
					pixel[0] = 255;
					pixel[1] = 255;
				} else if (value < .2) {
					pixel[0] = 255;
					pixel[1] = 140;
				} else {
					pixel[0] = 255;
					pixel[1] = 0;
				}
				raster.setPixel(i, j, pixel);
			}
		}
		
		ImageIO.write(image, "PNG", outputStream);
	}

	public static void main(String[] args) throws IOException {
		InputStream in = new FileInputStream(new File("C:\\Users\\matchami\\Desktop\\http _shakemap.geonet.org.nz_data_2016p858000_output_grid.xml"));
		
		ShakeMapParser parser = new ShakeMapParser(in);
		double[][] pga = parser.processData("PGA").get(0);
		
		FileOutputStream out = new FileOutputStream(new File("C:\\Users\\matchami\\Desktop\\image.png"));
		new ShakeMapServlet().createImage(pga, out);
		out.close();
	}
	
}
