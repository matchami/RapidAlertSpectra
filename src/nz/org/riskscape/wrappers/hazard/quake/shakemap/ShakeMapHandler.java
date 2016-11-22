package nz.org.riskscape.wrappers.hazard.quake.shakemap;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ShakeMapHandler extends DefaultHandler {

  boolean data = false;
  StringBuilder sb = new StringBuilder();
  int numFields = 0;
  ArrayList<ArrayList<Double>> fieldData = new ArrayList<ArrayList<Double>>();
  ArrayList<String> fields = new ArrayList<String>();
  private double lon_min, lon_max;
  private double lat_min, lat_max;
  private double nominal_lon_spacing;
  private double nominal_lat_spacing;
  private int nlat;
  private int nlon;

  @Override
  public void startDocument() throws SAXException {
  }

  @Override
  public void endDocument() throws SAXException {
    //This deals with the last line of grid data
    String[] nums = sb.toString().replaceFirst("\\s+", "").split("\\s+");
    for (int i = 0; i < nums.length; i++) {
      fieldData.get(i % numFields).add(Double.parseDouble(nums[i]));
    }
  }

  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
        
    switch (qName) {

    case "grid_specification":
      lon_min = Double.parseDouble(attributes.getValue("lon_min"));
      lat_min = Double.parseDouble(attributes.getValue("lat_min"));
      lon_max = Double.parseDouble(attributes.getValue("lon_max"));
      lat_max = Double.parseDouble(attributes.getValue("lat_max"));
      nominal_lon_spacing = Double.parseDouble(attributes.getValue("nominal_lon_spacing"));
      nominal_lat_spacing = Double.parseDouble(attributes.getValue("nominal_lat_spacing"));
      nlat = Integer.parseInt(attributes.getValue("nlat"));
      nlon = Integer.parseInt(attributes.getValue("nlon"));
      break;
    case "grid_field":
      fields.add(attributes.getValue(1));
      numFields++;
      fieldData.add(new ArrayList<Double>());
      break;
    case "grid_data":
      data = true;
    default :
      break;

    }

  }

  @Override
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    data = false;
  }

  // To take specific actions for each chunk of character data (such as
  // adding the data to a node or buffer, or printing it to a file).
  @Override
  public void characters(char ch[], int start, int length)
      throws SAXException {
    if (!data)
      return;
    sb.append(new String(ch, start, length));
  }

  public ArrayList<ArrayList<Double>> getFieldData() {
    return fieldData;
  }

  public ArrayList<String> getFields() {
    return fields;
  }


  public Double getLon_min() {
    return lon_min;
  }

  public double getLat_min() {
    return lat_min;
  }
  
  public Double getLon_max() {
    return lon_max;
  }

  public double getLat_max() {
    return lat_max;
  }

  public double getNominal_lon_spacing() {
    return nominal_lon_spacing;
  }

  public double getNominal_lat_spacing() {
    return nominal_lat_spacing;
  }

  public int getNlat() {
    return nlat;
  }


  public int getNlon() {
    return nlon;
  }
}

