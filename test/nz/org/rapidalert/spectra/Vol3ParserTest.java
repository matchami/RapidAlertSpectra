package nz.org.rapidalert.spectra;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import nz.cri.gns.rapidalert.spectra.Vol3Parser;

public class Vol3ParserTest {

	@Test
	public void test() throws IOException {
		File file = new File("TestData/BWRS.v3a");
		try (InputStream in = new FileInputStream(file)) {
			Vol3Parser parser = new Vol3Parser();
			parser.read(in);
			
			assertEquals("BWRS", parser.getStationCode());
			assertEquals("41 26 22S  173 54 18E", parser.getStationLatLng());
			assertEquals("Waikakaho Road", parser.getStationName());
			assertEquals("20161113_120414_BWRS_20", parser.getAccelerogram());
			
			List<String> components = parser.getComponents();
			assertEquals(3, components.size());
			assertEquals("N00E  Horizontal Axis 1", components.get(0));
			assertEquals("N90W  Horizontal Axis 2", components.get(1));
			assertEquals("Up    Vertical Accelerometer Axis", components.get(2));
			
			List<Double> damping = parser.getDamping(0);
			assertEquals(5, damping.size());
			
			double[] expectedDamping = {0.000, 0.020, 0.050, 0.100, 0.200};
			for (int i=0; i<damping.size(); i++) {
				assertEquals(expectedDamping[i], damping.get(i), Double.MIN_VALUE);
			}
			
			List<Double> periods = parser.getPeriods(0);
			assertEquals(100, periods.size());
			
			assertEquals(0.04, periods.get(0), Double.MIN_VALUE);
			assertEquals(0.24, periods.get(29), Double.MIN_VALUE);
			
			//Component,Damping
			double[] sa = parser.getSA(0, 2);
			assertEquals(56.76, sa[0], Double.MIN_VALUE);
			assertEquals(62.16, sa[8], Double.MIN_VALUE);
		}
		
	}

}
