package nz.org.riskscape.wrappers.hazard.quake.shakemap;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;


public class ShakeMapParser {
	
	private Double[] latitude;
  private Double[] longitude;
	private List<Double[]> rawData;
	private List<String> atribs;
	private int xSize, ySize;
	private double latitudeMin, latitudeMax;
	private double latitudeSpacing;
	private double longitudeMin, longitudeMax;
	private double longitudeSpacing;
  private InputStream data; 

	public ShakeMapParser(InputStream data) {
	  this.data = data;
	}
	
	/**
	 * Returns a list of grids for the quantities requested
	 * @param file the file to parse
	 * @param requiredQuantities the fields (as defined in the XML header to extract)
	 */
	public List<double[][]> processData(String ... requiredQuantities){
		SAXParser parser = null;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ShakeMapHandler handler = new ShakeMapHandler();
		try {
			parser.parse(data, handler);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setFields(handler);
		return readProcessedData(requiredQuantities);
	}

	
	private void setFields(ShakeMapHandler handler) {
		atribs = handler.getFields();
		rawData = new ArrayList<Double[]>();
		for(ArrayList<Double> d :handler.fieldData){
			rawData.add(d.toArray(new Double[d.size()]));
		}
		setLatitude(rawData.get(atribs.indexOf("LAT")));
		setLongitude(rawData.get(atribs.indexOf("LON")));
		xSize = handler.getNlat();
		ySize = handler.getNlon();
		latitudeMin = handler.getLat_min();
		latitudeSpacing = handler.getNominal_lat_spacing();
		longitudeMin = handler.getLon_min();
		longitudeSpacing = handler.getNominal_lon_spacing();
    latitudeMax = handler.getLat_max();
    longitudeMax = handler.getLon_max();
		//System.out.println("Fields Set");
		

	}


	private List<double[][]> readProcessedData(String[] wantedVals){
		List<double[][]> output = new ArrayList<double[][]>();
		for(int j = 0; j < wantedVals.length; j++){
			double[][] curOutput = new double[xSize][ySize];
			int idx = atribs.indexOf(wantedVals[j]);
			//Set each value into it's relevant index
			for(int i=0; i < rawData.get(idx).length; i++){
//		     System.out.println("rawData.get(idx).length" + rawData.get(idx).length +" i " + i);

				curOutput[getX(i)][getY(i)] = rawData.get(idx)[i];		
			}
			output.add(curOutput);
//      for(int i=0; i < rawData.get(idx).length; i++){
//        System.out.print(wantedVals[j] + " idx ="+idx+" i ="+i  + " getX(i)="+getX(i)+" getY(i)="+getY(i));
//        System.out.println();
//      }			
//			for(int i = 0; i < curOutput.length; i++){
//			    
//				for(int k = 0; k < curOutput[i].length; k++){
//				  if (k == 0 && i == 0){
//				    System.out.print("i="+i+"k="+k + curOutput[i][k] + " ");
//	          System.out.println("getX(i)="+getX(i)+"getY(i)="+getY(i) );
//				  }
//				}
//			}
//			System.out.println();
		}
		return output;
		
	}


	public int getY(int i) {
		return (int) Math.round((getLongitude()[i] - longitudeMin)/longitudeSpacing);
	}


	public int getX(int i) {
		return (int) Math.round((getLatitude()[i] - latitudeMin)/latitudeSpacing);
	}

  public double getMininumLatitude() {
    return latitudeMin;
  }

  public double getMaxLatitude() {
    return latitudeMax;
  }

  public double getMininumLongitude() {
    return longitudeMin;
  }

  public double getMaxLongitude() {
    return longitudeMax;
  }
  
  public int getSizeLongitude() {
    return ySize;
  }
  public int getSizeLatitude() {
    return xSize;
  }

  public Double[] getLongitude() {
    return longitude;
  }

  public void setLongitude(Double[] longitude) {
    this.longitude = longitude;
  }

  public Double[] getLatitude() {
    return latitude;
  }

  public void setLatitude(Double[] latitude) {
    this.latitude = latitude;
  }

  public double getLongitudeSpacing() {
    return longitudeSpacing;
  }
  public double getLatitudeSpacing() {
    return latitudeSpacing;
  }
  
  public static InputStream getXMLFor(String slug) throws IOException {
    try {
	    URL url = new URL("http://shakemap.geonet.org.nz/data/" + slug + "/output/grid.xml");
	    return url.openStream();
	  } catch (MalformedURLException e) {
	    e.printStackTrace();
	  } catch (IOException e) {
		  //Doesn't exist
	  }
	  return null;
	}
}
