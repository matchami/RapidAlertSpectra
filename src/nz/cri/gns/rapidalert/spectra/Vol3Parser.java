package nz.cri.gns.rapidalert.spectra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Vol3Parser {

	private List<Component> components = new ArrayList<>();

	/**
	 * See 
	 * http://info.geonet.org.nz/display/appdata/Response+Spectra+Data+Filenames+and+Formats
	 * 
	 * @param in
	 * @throws IOException
	 */
	public void read(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		Component component;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("Response Spectra")) {
				component = new Component();
				components.add(component);
				
				//Read the header info for the component
				line = br.readLine();
				if (!line.startsWith("Site"))
					throw new IllegalArgumentException("File is not in recognised format: 2nd line of component does not start with 'Site'");
				
				line = line.substring(5, line.indexOf("Basalt"));
				component.setStationCode(line.substring(0, line.indexOf(" ")));
				component.setStationLatLng(line.substring(line.indexOf(" ")).trim());
				
				component.setStationName((br.readLine().trim()));
				
				//Read through Instrument and Resolution lines
				br.readLine();
				br.readLine();
				line = br.readLine();
				component.setAccelerogram(line.split("\\s")[1]);
				
				//Read through location, date, epicentre, metrics, units, filter
				for (int i=0; i<6; ++i) {
					br.readLine();
				}
				//Component name
				line = br.readLine();
				component.setName(line.substring("Component ".length()).trim());
				
				//Read through remaining text headers(10 lines), integer headers (5 lines) and double headers (6 lines)
				for (int i=0; i<14; ++i) {
					br.readLine();
				}
				
				String[] damping = br.readLine().trim().split("\\s+");
				List<Damping> dampings = new ArrayList<>();
				for (String val : damping) {
					dampings.add(new Damping(Double.parseDouble(val)));
				}
				component.setDampings(dampings);
				
				List<Double> periods = new ArrayList<Double>();
				//Periods are shared between all dampings
				while (!(line = br.readLine()).trim().startsWith("Spectral Accel")) {
					for (String val : line.trim().split("\\s+")) {
						periods.add(new Double(val));
					}
				}
				component.setPeriods(periods);
				
				int dataLength = periods.size();
				
				for (int i=0; i<dampings.size(); ++i) {
					double[] data = new double[dataLength];
					LinkedList<String> lineInUse = new LinkedList<>();
					for (int j=0; j<dataLength; ++j) {
						data[j] = getNextDouble(br, lineInUse);
					}
					dampings.get(i).setData(data);
				}
			}
		}
	}

	private double getNextDouble(BufferedReader br, LinkedList<String> line) throws IOException {
		if (line.size() == 0) {
			String nextLine = br.readLine();
			for (String val : nextLine.trim().split("\\s+")) {
				line.add(val);
			}
		}
		return Double.parseDouble(line.removeFirst());
	}

	public String getStationCode() {
		return components.get(0).getStationCode();
	}

	public String getStationName() {
		return components.get(0).getStationName();
	}

	public String getStationLatLng() {
		return components.get(0).getStationLatLng();
	}
	
	public String getAccelerogram() {
		return components.get(0).getAccelerogram();
	}

	public List<String> getComponents() {
		return components.stream().map(Component::getName).collect(Collectors.toList());
	}

	public List<Double> getDamping(int componentIndex) {
		return components.get(componentIndex).getDampings().stream().map(Damping::getDamping).map(Double::new).collect(Collectors.toList());
	}

	public List<Double> getPeriods(int componentIndex) {
		return components.get(componentIndex).getPeriods();
	}

	public double[] getSA(int componentIndex, int dampingIndex) {
		return components.get(componentIndex).getDampings().get(dampingIndex).getData();
	}


}
