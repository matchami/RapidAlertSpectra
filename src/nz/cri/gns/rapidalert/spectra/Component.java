package nz.cri.gns.rapidalert.spectra;

import java.util.List;

public class Component {

	private String stationCode;
	private String stationName;
	private String stationLatLng;
	private String accelerogram;
	private List<Damping> dampings;
	private List<Double> periods;
	private String name;
	public String getStationCode() {
		return stationCode;
	}
	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public String getStationLatLng() {
		return stationLatLng;
	}
	public void setStationLatLng(String stationLatLng) {
		this.stationLatLng = stationLatLng;
	}
	public String getAccelerogram() {
		return accelerogram;
	}
	public void setAccelerogram(String accelerogram) {
		this.accelerogram = accelerogram;
	}
	public void setDampings(List<Damping> dampings) {
		this.dampings = dampings;
	}
	public List<Damping> getDampings() {
		return dampings;
	}
	public void setPeriods(List<Double> periods) {
		this.periods = periods;
	}
	public List<Double> getPeriods() {
		return periods;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	
}
