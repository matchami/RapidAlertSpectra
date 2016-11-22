package nz.cri.gns.rapidalert.spectra;

public class Damping {

	private double damping;
	private double[] data;
	
	public Damping(double damping) {
		this.damping = damping;
	}

	public double getDamping() {
		return damping;
	}

	public void setDamping(double damping) {
		this.damping = damping;
	}

	public double[] getData() {
		return data;
	}

	public void setData(double[] data) {
		this.data = data;
	}

}
