<%@page import="java.io.InputStream"%>
<%@page import="nz.org.riskscape.wrappers.hazard.quake.shakemap.ShakeMapParser"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="nz.cri.gns.rapidalert.model.Station"%>
<%@page import="java.util.Collections"%>
<%@page import="nz.cri.gns.rapidalert.da.DataAccess.Types"%>
<%@page import="nz.cri.gns.rapidalert.model.Earthquake"%>
<%@page import="java.util.Arrays"%>
<%@page import="nz.cri.gns.rapidalert.model.DamageThreshold"%>
<%@page import="java.util.List"%>
<%@page import="nz.cri.gns.rapidalert.da.DataAccess"%>
<%@page import="nz.cri.gns.rapidalert.model.Region"%>
<%@page extends="nz.cri.gns.rapidalert.web.RapidAlertPageBase"%>

<%
	DataAccess da = getDataAccess(request);
	String slug = request.getParameter("quake");
	Earthquake eq = null;
	if (slug == null) {
		int id = Integer.parseInt(request.getParameter("id"));
		eq = da.get(DataAccess.EARTHQUAKE, id);
	} else {
		eq = da.getEarthquake(slug);
	}
	
%>

<div class="overview">

	<h2>Overview</h2>

	<table>
		
		<tr>
			<th>ID:</th>
			<td><%=eq.getSlug() %></td>
		</tr>
		
		<tr>
			<th>Magnitude:</th>
			<td><%=String.format("%.1f", eq.getEqMagnitude())%></td>
		</tr>
		
		<tr>
			<th>Depth:</th>
			<td><%=String.format("%.0f km", eq.getEqDepth())%></td>
		</tr>
		
		<tr>
			<th>Date:</th>
			<td><%=String.format("%tb. %<td, %<tY, %<tl:%<tM %<tp", eq.getEqTime())%></td>
		</tr>
		
		<tr>
			<th>Status:</th>
			<td><%=eq.getStatus() %></td>
		</tr>
		
		<tr>
			<th><a href="<%=eq.getGeonetUrl() %>" target="_blank">GeoNet link &raquo;</a></th>
			<td></td>
		</tr>
		
	</table>
	
</div>

<div class="leaflet-map">

	<h2>Map</h2>

	<div id="map"></div>
	
	<script type="text/javascript">
		
		// initialize layers for pga bands
		
		var green 	= L.layerGroup([]);
		var yellow 	= L.layerGroup([]);
		var orange 	= L.layerGroup([]);
		var red 	= L.layerGroup([]);
		
		// initialize map
		
		var map = L.map('map', {
		    center: [<%=eq.getLatitude()%>, <%=eq.getLongitude()%>],
		    zoom: 7,
		    scrollWheelZoom: false,
		    layers: [green, yellow, orange, red],
		    minZoom: 3,
		    worldCopyJump: true,
		});
		
		// load OSM tiles
		
	 	L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
	 		attribution: '',
	 	}).addTo(map);
		
		// plot earthquake epicenter
		
		var epicenter = L.marker([<%=eq.getLatitude()%>, <%=eq.getLongitude()%>], {}).addTo(map);
		
		epicenter.bindPopup("<table class='popup'><tr><th>ID:</th><td><%=eq.getSlug()%></td></tr><tr><th>Magnitude:</th><td><%=String.format("%.1f", eq.getEqMagnitude())%></td></tr><tr><th>Depth:</th><td><%= String.format("%.0f km", eq.getEqDepth())%></td></tr></table>").openPopup();
	
		// plot rail lines
		
		function onEachFeature(feature, layer) {
			if (feature.properties && feature.properties.LINESECTIO) {
				layer.bindPopup(feature.properties.LINESECTIO)
			}
		}
		
		var trackStyle = {
			"color": "#757575",
			"weight": 3,
			"opacity": 0.75,
		};
		
		var trackLayer = L.geoJson(rail, {
			style: trackStyle,
			onEachFeature: onEachFeature,
		}).addTo(map);
		
	<%
		InputStream in = ShakeMapParser.getXMLFor(eq.getSlug());
		boolean shakeMapExists = in != null;
		if (shakeMapExists) {
			ShakeMapParser parser = new ShakeMapParser(in);
			parser.processData();
			double minLat = parser.getMininumLatitude() - parser.getLatitudeSpacing()/2;
			double maxLat = parser.getMaxLatitude() + parser.getLatitudeSpacing()/2;
			double minLon = parser.getMininumLongitude() - parser.getLongitudeSpacing()/2;
			double maxLon = parser.getMaxLongitude() + parser.getLongitudeSpacing()/2;
			%>
			var imageUrl = 'shakemap.png?event=<%=eq.getSlug()%>',
			    imageBounds = [[<%=minLat%>, <%=minLon%>], [<%=maxLat%>, <%=maxLon%>]];

			L.imageOverlay(imageUrl, imageBounds, {"opacity": 0.4}).addTo(map);
			<%
		}
	%>
		// add station data

	<%
	for (Station station : eq.getStations()) {
		double thepga = station.getPgaHUpper() / 100;
	%>
	
		var <%=station.getSiteCode()%> = L.circleMarker([<%=station.getLatitude()%>, <%=station.getLongitude()%>], {
		    fillOpacity: 0.5,
		<% if (thepga >= 0.2) { %>
		    color: '#ff0000',
		}).addTo(red);
		<% }else if (thepga < 0.2 && thepga >= 0.15) {%>
		    color: '#ff9900',
		}).addTo(orange);
		<%} else if (thepga < 0.15 && thepga >= 0.1) {%>
		    color: '#ffd700',
		}).addTo(yellow);
		<% } else { %>
			color: '#008000',
		}).addTo(green);
		<%}%>
		
		<%=station.getSiteCode()%>.bindPopup("<strong><%=station.getSiteName()%></strong><br>PGA (upper bound): <%=String.format("%.3fg", station.getPgaHUpper()/100)%><br>PGA (lower bound): <%=String.format("%.3fg", station.getPgaHLower()/100)%>");
		
	<%
	}
	%>
		
		// create layer control for filtering different pga bands
		
		var baseMaps = {};
		
		var overlayMaps = {
			"Green": green,
			"Yellow": yellow,
			"Orange": orange,
			"Red": red,
		};
	
		L.control.layers(baseMaps, overlayMaps).addTo(map);
		
		// add legend
		
		function getColor(d) {
		    return d > 0.20 ? '#ff0000' :
		           d > 0.15 ? '#ff9900' :
		           d > 0.10 ? '#ffd700' :
		                      '#008000';
		}
		
		var legend = L.control({position: 'bottomright'});
		
		legend.onAdd = function (map) {
		
		    var div = L.DomUtil.create('div', 'info legend'),
		        grades = [0, 0.10, 0.15, 0.20],
		        labels = [];
			
			div.innerHTML += '<h4>PGA thresholds</h4>';
			
		    // loop through our density intervals and generate a label with a colored square for each interval
		    for (var i = 0; i < grades.length; i++) {
		        div.innerHTML +=
		            '<i style="background:' + getColor(grades[i] + 0.01) + ';"></i> ' +
		            grades[i] + (grades[i + 1] ? '&nbsp;&ndash;&nbsp;' + grades[i + 1] + '&nbsp;g<br>' : '&nbsp;g&nbsp;+');
		    }
		
		    return div;
		};
		
		legend.addTo(map);
		
	</script>
	<% if (!eq.getStations().isEmpty()) {%>
	
	<p><strong>Last update:</strong> <%=String.format("%tb. %<td, %<tY, %<tl:%<tM %<tp", eq.getLastModified()) %></p>
	
	<%} %>	
</div>

<div class="station-data">
	<%
	int numStations = eq.getStations().size();
	if (numStations > 0) {
		%>
		
		<h2>Table</h2>
		
		<h3><%=numStations %>
		<%if (numStations > 1) { %>
			stations
		<%} else { %>
			station
		<%} %>
			reporting</h3>
		
		<table>
		
			<tr>
				<th class="mobile-hide">Station ID</th>
				<th>Location</th>
				<th>PGA<br>(upper bound)</th>
				<th class="mobile-hide">PGA<br>(lower bound)</th>
				<th>Epicentral<br>distance</th>
				<th class="mobile-hide">Date added</th>
			</tr>
		<% 
		List<Station> stations = new ArrayList<Station>(eq.getStations());
		Collections.sort(stations, new Comparator<Station>() {
			@Override
			public int compare(Station s1, Station s2) {
				double diff = s2.getPgaHUpper() - s1.getPgaHUpper();
				if (diff > 0)
					return 1;
				else if (diff == 0) {
					return 0;
				} else {
					return -1;
				}
			}
		});
		for (Station station : stations) { %>
			
		<% if (station.getPgaHUpper()/100 >= 0.2)  {%>
			<tr class="damage">
		<% } else { %>
			<tr>
		<% } %>
		
				<td class="mobile-hide"><%=station.getSiteCode() %></td>
				<td><%= station.getSiteName() %></td>
		    	<td><%= String.format("%.3f", station.getPgaHUpper()/100) %> g</td>
				<td class="mobile-hide"><%= String.format("%.3f", station.getPgaHLower()/100) %> g</td>
				<td><%= station.getDistance() %> km</td>
				<td class="mobile-hide"><%= String.format("%tb. %<td, %<tY, %<tl:%<tM %<tp", station.getDateAdded()) %></td>
			
			</tr>
		<% } %>	
		</table>
	
		<p><strong>Last update:</strong> <%=String.format("%tb. %<td, %<tY, %<tl:%<tM %<tp", eq.getLastModified()) %></p>
		
	<%} %>
	
</div>

<div class="disclaimer">

	<p><em>Ground motions were recorded by <a href="http://geonet.org.nz" target="_blank">GeoNet</a>. The GeoNet project is core funded by the <a href="http://www.eqc.govt.nz" target="_blank">Earthquake Commission</a> (EQC) and is being designed, installed, and operated by <a href="http://www.gns.cri.nz" target="_blank">GNS Science</a> on behalf of EQC and all New Zealanders. GNS Science accepts no liability for any loss or damage, direct or indirect, resulting from the use of the information provided. GNS Science does not make any representation with respect to the information's accuracy, completeness, or fitness for any particular purpose.</em></p>

</div>

