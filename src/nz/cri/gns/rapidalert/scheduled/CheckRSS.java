package nz.cri.gns.rapidalert.scheduled;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletContext;

import nz.cri.gns.rapidalert.da.DataAccess;
import nz.cri.gns.rapidalert.model.Earthquake;
import nz.cri.gns.rapidalert.model.Notify;
import nz.cri.gns.rapidalert.model.Station;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jumbletree.mail.DeliveryAddress;
import com.jumbletree.mail.Mailer;
import com.jumbletree.utils.CSVReader;
import com.jumbletree.utils.dataaccess.DataAccessException;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

//****************************************************************************\
//  ==============                                                            *
//  class CheckRSS                                                            *
//  ==============                                                            *
//                                                                            *
//  Checks for new RSS items in the felt earthquakes RSS feed                 *
//  http://www.geonet.org.nz/quakes/services/felt.rss                         *
//                                                                            *
//  Ported from Python code checkRSS.py                                       *
//  Dec 2014, Aarno Korpela                                                   *
//                                                                            *
//****************************************************************************/

@Service
public class CheckRSS
{
	private static final String FROM_ADDRESS = "info@rapidalert.org.nz";
	//private static DeliveryAddress[] TO_ADDRESS, DEV_ADDRESS;
	
//	static {
//		try {
//			DEV_ADDRESS = new DeliveryAddress[] {
//				new DeliveryAddress(InternetAddress.parse("iain@jumbletree.com")[0], RecipientType.TO),
//				new DeliveryAddress("a.king@gns.cri.nz", RecipientType.TO),
//				new DeliveryAddress("m.nayyerloo@gns.cri.nz", RecipientType.TO),
//				new DeliveryAddress("n.horspool@gns.cri.nz", RecipientType.TO)
//			};
//			
//			TO_ADDRESS = DEV_ADDRESS;
//		} catch (Exception e) {
//			//Can't happen
//		}
//	};
	
	//Two hour send window on the 'major' alert
	private static final long SEND_WINDOW = 1000 * 60 * 60 * 2;
	
	@Autowired
	private URL url;

	@Autowired
	private DataAccess da;
	
	@Autowired
	ServletContext context;

	//****************************************************************************\
	//  main                                                                      *
	//                                                                            *
	//****************************************************************************/

//	public static void main(String[] args) throws ClassNotFoundException, MalformedURLException, DataAccessException, SQLException, IOException, FeedException, ParseException, IllegalArgumentException, JSONException, MessagingException
//	{
//
//		System.setProperty("smtp.server", "localhost");
//		
//		DataAccess da = new RapidAlertHibernate("localhost", "rapidalert", "rapidalert", "I'maLert").getDAOFactory().getDataAccess();
//
//		URL url = new URL("http://www.geonet.org.nz/quakes/services/felt.rss");
//		
//		new CheckRSS(da, url).execute();
//	}
	
	public CheckRSS(DataAccess da, URL url) {
		this.da = da;
		this.url = url;
	}
	
	public CheckRSS() {
		System.out.println("CSJM: Initialising a check rss");
	}

	@Scheduled(fixedRate=120000)
	@Transactional
	public void execute() throws Exception {

		downloadFeltFromRSS();

		FTPClient ftp = new FTPClient();
		
		List<Earthquake> earthquakes = da.getRecentEarthquakes(3);
		for (Earthquake earthquake : earthquakes) {
			try {
				loadDetails(earthquake, ftp);
			} catch (IOException e) {
				e.printStackTrace();
				sendErrorReport("FTP error for earthquake "+earthquake.getSlug()+" while loading details", e);
			}
		}

		//da.closeConnection();
		if (ftp.isConnected()) {
			try {
				ftp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadDetails(Earthquake earthquake, FTPClient ftp) throws DataAccessException, SocketException, IOException {

		
		//Check if details have been updated since we saved it
		Earthquake newCopy;
		try {
			newCopy = parseEqJson(earthquake.getSlug());
		} catch (JSONException | IOException | ParseException e) {
			sendErrorReport("Error parsing JSON for "+earthquake.getSlug()+" while loading details", e);
			return;
		}
		if (newCopy.getLastModified().after(earthquake.getLastModified())) {
			earthquake.setEqTime(newCopy.getEqTime());
			earthquake.setLongitude(newCopy.getLongitude());
			earthquake.setLatitude(newCopy.getLatitude());
			earthquake.setEqDepth(newCopy.getEqDepth());
			earthquake.setEqMagnitude(newCopy.getEqMagnitude());
			earthquake.setLastModified(newCopy.getLastModified());
			earthquake.setStatus(newCopy.getStatus());
			da.save(earthquake);
		}

		if (earthquake.getStatus().equals("deleted"))
			return;
		
		if (earthquake.getEqMagnitude().doubleValue() > 4) {
			URL csvURL;
			try {
				csvURL = new URL(earthquake.getCsvUrl());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				sendErrorReport("Earthquake "+earthquake.getSlug()+" has an invalid CVS URL of "+earthquake.getCsvUrl(), e);
				return;
			}
			if (!ftp.isConnected()) {
				ftp.connect(csvURL.getHost());
				ftp.user("ftp");
				ftp.pass("rapidalert");
			}

			ftp.changeWorkingDirectory("/");

			String path = csvURL.getPath();
			if (path.startsWith("/"))
				path = path.substring(1);

			String[] pathElements = path.split("\\/");
			for (int i=0; i<pathElements.length-1; i++) {
				ftp.changeWorkingDirectory(pathElements[i]);
			}
			String filename = pathElements[pathElements.length-1];

			InputStream in = ftp.retrieveFileStream(filename);
			if (in == null) {
				System.out.println("No file found, aborting");
				return;
			}

			CSVReader csvReader = new CSVReader(new InputStreamReader(in));
			String[] headers = csvReader.readCSVLine();

			String[] wanted = new String[]{
					"Epic. Dist.(km)", 					
					"PGA Vertical (%g)", 
					"PGA Horiz_1 (%g)", 
					"PGA Horiz_2 (%g)", 
					"Site Code", 
					"Name", 
					"Site Latitude", 
					"Site Longitude", 
					"Accelerogram ID"
			};
			final int EPIC_DIST = 0, PGA_V = 1, PGA_H_1 = 2, PGA_H_2 = 3, SITE_CODE = 4, NAME = 5, LAT = 6, LON = 7, ACC_ID = 8;
			int[] colIndexes = new int[wanted.length];
			Arrays.fill(colIndexes, -1);
			__outer: for (int i=0; i<headers.length; i++) {
				for (int j=0; j<wanted.length; j++) {
					if (headers[i].equals(wanted[j])) {
						colIndexes[j] = i;
						continue __outer;
					}
				}
			}

			boolean ok = true;
			for (int i=0; i<wanted.length; i++) {
				if (colIndexes[i] == -1) {
					ok = false;
					System.out.println("Failing because can't find column: " + wanted[i]);
					break;
				}
			}

			List<Station> stations = new ArrayList<Station>();
			if (ok) {
				String[] line = null;
				while ((line = csvReader.readCSVLine()) != null) {
					Station station = new Station();
					station.setLatitude(new BigDecimal(line[colIndexes[LAT]]));
					station.setLongitude(new BigDecimal(line[colIndexes[LON]]));
					station.setPgaH1(Double.parseDouble(line[colIndexes[PGA_H_1]]));
					station.setPgaH2(Double.parseDouble(line[colIndexes[PGA_H_2]]));
					station.setPgaV(Double.parseDouble(line[colIndexes[PGA_V]]));
					station.setDistance(new Integer(line[colIndexes[EPIC_DIST]]));
					//calculates lower bound horizontal pga by taking max of two horizontal orthogonal pgas
					station.setPgaHLower(Math.max(station.getPgaH1(), station.getPgaH2()));
					//calculates upper bound horizontal pga by taking vector sum of two horizontal orthogonal pgas
					station.setPgaHUpper(Math.sqrt(Math.pow(station.getPgaH1(),  2) + Math.pow(station.getPgaH2(), 2)));

					//Now set all the string vars
					station.setAccelerogramId(line[colIndexes[ACC_ID]]);
					station.setDateAdded(new Date());
					station.setEarthquake(earthquake);
					station.setSiteCode(line[colIndexes[SITE_CODE]]);
					station.setSiteName(line[colIndexes[NAME]]);
					stations.add(station);
					System.out.println("Added station " + station.getSiteCode());
				}
			}
			if (csvReader != null) {
				csvReader.close();
			}

			//Reset the ftp to root dir
			in.close();
			ftp.completePendingCommand();
			ftp.changeWorkingDirectory("/");
			List<Station> existing = new ArrayList<>(earthquake.getStations());

			for (Iterator<Station> it = stations.iterator(); it.hasNext(); ) {
				Station station = it.next();
				String uniqueId = station.getAccelerogramId();
				uniqueId = uniqueId.substring(uniqueId.length()-7);
				Station found = null;
				for (Station existingStation : existing) {

					String existingUnique = existingStation.getAccelerogramId();
					existingUnique = existingUnique.substring(existingUnique.length()-7);
					if (existingUnique.equals(uniqueId)) {
						found = existingStation;
						break;
					}
				}
				if (found == null) {
					da.save(station);
				} else {
					found.setDistance(station.getDistance());
					found.setAccelerogramId(station.getAccelerogramId());
					da.save(found);
				}
			}
		}
	
	}

	
	/**
	 * Looks up the RSS feed, and for each entry analyses the magnitude.
	 * Based on earthquake magnitudes, sends alerts and potentially commits the earthquake to the db.
	 * 
	 * @throws IllegalArgumentException
	 * @throws FeedException
	 * @throws JSONException
	 * @throws DataAccessException
	 * @throws ParseException
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void downloadFeltFromRSS() throws DataAccessException  {

		SyndEntry        entry;
		String           link,id;
		String[]         words;
		String           title,where; 
		String[]         elems; 
		XmlReader        reader = null;
		SyndFeed         feed = null;

		try {
			// Download a list of latest felt earthquakes from RSS
			reader = new XmlReader(url);
			feed = new SyndFeedInput().build(reader);
//			System.out.println("");
//			System.out.println("Feed Title: "+ feed.getAuthor());
//			System.out.println("-------------------------------------------------------------------------------------");
			for (@SuppressWarnings("rawtypes")Iterator it = feed.getEntries().iterator(); it.hasNext();)
			{
				entry = (SyndEntry) it.next();
				link  = entry.getLink();
				words = link.split("/");
				id = words[words.length-1];

				title = entry.getTitle();  // E.g. "Magnitude 2.6, Fri, Dec 19 2014 at 2:27:34 pm (NZDT), 10 km north-west of Taupo"
				elems = title.split(", ");
				where = elems[3];
				//				System.out.println(title);

				// Process new earthquake entries
				if (eqIsNew(id))
				{
					Earthquake eq = parseEqJson(id);
					System.out.println("Got new eq: " + eq.getSlug() + " with magnitude " + eq.getEqMagnitude().floatValue());

					if (eq.getEqMagnitude().floatValue() >= 3.5) // Save earthquake record to database if magnitude >= 3.5
					{
						eq.setLocation(where);
						if (eq.getEqMagnitude().floatValue() >= 4) // Send email alert if magnitude >= 4
						{
							System.out.println("Sending alert for "+eq.getSlug());
							sendAlert(eq, true);       // ### TBD ###
							eq.setAlert(true);

							//Real?
							if (eq.getEqMagnitude().floatValue() >= 5 && System.currentTimeMillis() - eq.getEqTime().getTime() < SEND_WINDOW) //Send to real people
							{
								sendAlert(eq, false);
							}
						}
						da.save(eq);
//						System.out.println("Saved with key "+eq.getEqId());
//						System.out.println(eq.getSlug());
//						System.out.println(eq.getEqTime());
//						System.out.println(eq.getLatitude().floatValue());
//						System.out.println(eq.getLongitude().floatValue());
//						System.out.println(eq.getEqDepth().floatValue());
//						System.out.println(eq.getEqMagnitude().floatValue());
//						System.out.println(eq.getGeonetUrl());
//						System.out.println(eq.getCsvUrl());
//						System.out.println(eq.getLastModified());
//						System.out.println(eq.getStatus());
//						System.out.println(eq.getLocation());
//						System.out.println(eq.isAlert());
					}
				}
				//				System.out.println("-------------------------------------------------------------------------------------");
			}
		} catch (IllegalArgumentException | FeedException | IOException e) {
			sendErrorReport("Error reading from RSS feed", e);
			e.printStackTrace();
		} catch (JSONException | ParseException e) {
			sendErrorReport("Error parsing the JSON", e);
			e.printStackTrace();
		} catch (MessagingException e) {
			sendErrorReport("Error parsing the JSON", e);
			e.printStackTrace();
		} finally { 
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}

		
	}

	//****************************************************************************\
	//  eqIsNew                                                                   *
	//                                                                            *
	//****************************************************************************/
	// Check if earthquake record already exists within database

	

	public boolean eqIsNew(String id) throws DataAccessException
	{
		return !da.earthquakeExists(id);
	}



	//****************************************************************************\
	//  sendAlert                                                                 *
	//                                                                            *
	//****************************************************************************/
	// Send email alert
	// dev_list = ['mikemieler@gmail.com']
	

	public void sendAlert(Earthquake eq, boolean devOnly) throws MessagingException

	{
		
		String subject = String.format("[rapidAlert] Magnitude %.1f earthquake %s", eq.getEqMagnitude(), eq.getLocation());
		String message = String.format("This is a preliminary earthquake alert. More detailed ground shaking data for this event "
				+ "can be found at:\n\nhttp://www.rapidalert.org.nz/quakes/%s\n\nGround shaking data will "
				+ "generally be available within 10-15 minutes, though it may take longer for some stations to "
				+ "report.\n\nGNS Science accepts no liability for any loss or damage, direct or indirect, resulting "
				+ "from the use of the information provided. GNS Science does not make any representation in respect "
				+ "of the information's accuracy, completeness or fitness for any particular purpose.\n\nPlease do "
				+ "not respond to this email.", eq.getSlug());

		String html = String.format("<html><p>This is a preliminary earthquake alert. More detailed ground shaking data for this event "
				+ "can be found at:</p><p><a href=\"http://www.rapidalert.org.nz/quakes/%s\">http://www.rapidalert.org.nz/quakes/%<s</a></p>"
				+ "<p>Ground shaking data will "
				+ "generally be available within 10-15 minutes, though it may take longer for some stations to "
				+ "report.</p><p>GNS Science accepts no liability for any loss or damage, direct or indirect, resulting "
				+ "from the use of the information provided. GNS Science does not make any representation in respect "
				+ "of the information's accuracy, completeness or fitness for any particular purpose.</p><p>Please do "
				+ "not respond to this email.</p></html>", eq.getSlug());
		System.out.println("Sending...");
		
		String smtp = context.getInitParameter("smtp.server");
		DeliveryAddress[] addresses = devOnly ? getDevAddress() : getToAddress();
		if (addresses.length > 0) {
			Mailer.get(smtp, null, null).sendMessage(FROM_ADDRESS, addresses, subject, message, html, null, null);
		}
	}


	private DeliveryAddress[] getAddresses(List<Notify> notifications) throws AddressException, UnsupportedEncodingException {
		List<DeliveryAddress> addresses = new ArrayList<DeliveryAddress>(notifications.size());

		for (Notify notify : notifications) {
			addresses.add(new DeliveryAddress(notify.getEmail(), RecipientType.TO));
		}
		return addresses.toArray(new DeliveryAddress[addresses.size()]);
	}

	private void sendErrorReport(String reason, Exception e) {
		System.out.println("Generating error report: "+reason);
		e.printStackTrace();
		// Building stack trace string
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));		

		String subject = "[RapidAlert] Exception caught";
		String text = 	"Reason: "+reason+"\n\n" +
						"Message: " + e.getMessage() + "\n\n" +
						"StackTrace:\n" + stackTrace.getBuffer().toString();
		// Sending e-mail

		try {
			List<Notify> error = da.getErrorEmailAddresses();
			if (error.size() > 0) {
				String smtp = context.getInitParameter("smtp.server");
				Mailer.get(smtp, null, null).sendMessage(FROM_ADDRESS, getAddresses(error),subject,text,null,null,null);
			}
		} catch (UnsupportedEncodingException| MessagingException | DataAccessException e1) {
			e1.printStackTrace();
		}
		
	}

	private DeliveryAddress[] getDevAddress() {
		try {
			List<Notify> devs = da.getDevEmailAddresses();
			return getAddresses(devs);
		} catch (AddressException | UnsupportedEncodingException | DataAccessException e) {
			sendErrorReport("Error loading dev email addresses", e);
			return null;
		}
	}
	
	private DeliveryAddress[] getToAddress() {
		try {
			List<Notify> real = da.getRealEmailAddresses();
			return getAddresses(real);
		} catch (AddressException | UnsupportedEncodingException | DataAccessException e) {
			sendErrorReport("Error loading dev real addresses", e);
			return null;
		}
	}
	
	

	//****************************************************************************\
	//  parseEqJson                                                               *
	//                                                                            *
	//****************************************************************************/


	
	public Earthquake parseEqJson(String id) throws IOException, JSONException, ParseException
	{
		Earthquake equ = null;
		String     utcNor,utcIso;
		Date       equTime,updTime;

		JSONObject jsA   = readJsonFromUrl("http://www.geonet.org.nz/quakes/services/quake/"+id+".json");
		JSONArray  jsB   = (JSONArray)jsA.get("features");
		JSONObject jsC   = (JSONObject)jsB.get(0);
		JSONObject jsPro = (JSONObject)jsC.get("properties");
		JSONObject jsGeo = (JSONObject)jsC.get("geometry");
		JSONArray  jsCoo = (JSONArray)jsGeo.get("coordinates");

		//System.out.println(jsA.toString());

		SimpleDateFormat fmtNormal  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat fmtIso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

		utcNor = jsPro.get("origintime").toString();
		utcNor = fmtNormal.format(fmtNormal.parse(utcNor));  // Round the seconds
		utcIso = utcNor.replace(" ","T")+"Z";
		equTime = fmtIso8601.parse(utcIso);
		utcNor = jsPro.get("updatetime").toString();
		utcNor = fmtNormal.format(fmtNormal.parse(utcNor));  // Round the seconds
		utcIso = utcNor.replace(" ","T")+"Z";
		updTime = fmtIso8601.parse(utcIso);

		equ = new Earthquake();
		equ.setSlug(id);
		equ.setEqTime(equTime);
		equ.setLatitude(new BigDecimal(jsCoo.get(1).toString()));
		equ.setLongitude(new BigDecimal(jsCoo.get(0).toString()));
		equ.setEqDepth(new BigDecimal(jsPro.get("depth").toString()));
		equ.setEqMagnitude(new BigDecimal(jsPro.get("magnitude").toString()));
		equ.setGeonetUrl("http://www.geonet.org.nz/quakes/" + id);
		equ.setCsvUrl("ftp://ftp.geonet.org.nz/strong/summary/" + id.substring(0,4) + "/" + id + "_pga.csv");
		equ.setLastModified(updTime);
		equ.setStatus(jsPro.get("status").toString());
		equ.setLocation("");
		equ.setAlert(false);
		//private int eqId;
		//private Set<Station> stations = new HashSet<Station>(0);

		return equ;
	}


	//****************************************************************************\
	//  readJsonFromUrl                                                           *
	//                                                                            *
	//****************************************************************************/

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException
	{
		int         cVal;
		JSONObject  json = null;
		InputStream urlStream = null;

		try
		{
			urlStream = new URL(url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(urlStream, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
			while ((cVal = rd.read()) != -1) { sb.append((char)cVal); }
			json = new JSONObject(sb.toString());
		}
		finally { if (urlStream != null) urlStream.close(); }

		return json;
	}

	
	
}
