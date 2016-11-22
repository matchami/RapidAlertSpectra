<%@page import="java.time.LocalDate"%>
<%@page import="java.util.Date"%>
<%@page import="nz.cri.gns.rapidalert.model.Earthquake"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.time.LocalDateTime"%>
<%@page import="java.time.Instant"%>
<%@page import="java.time.ZoneId"%>
<%@page import="java.time.LocalDateTime"%>
<%@page import="nz.cri.gns.rapidalert.da.DataAccess"%>
<%@page import="nz.cri.gns.rapidalert.model.Region"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.List"%>
<%@page extends="nz.cri.gns.rapidalert.web.RapidAlertPageBase"%>

<%
//try{
	String startTime = request.getParameter("startTime");
	String finTime = request.getParameter("finTime");
	System.out.println("StartTime: " + startTime + "\nFinTime: " + finTime);
	DataAccess da = getDataAccess(request);
	%> 
	
	<h2>Latest earthquakes</h2>

		<% 
		
		LocalDate end = LocalDate.parse(finTime);
		//Instant instant = ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
		//Date endDate = Date.from(instant);
		
		LocalDate start = LocalDate.parse(startTime);
		//instant = ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
		//Date startDate = Date.from(instant);
		
		
		List<Earthquake> quakes = da.listEarthquakes(start, end); 
		
		System.out.println(quakes);
		
		//Collections.sort(quakes, Collections.reverseOrder());
		
		
		if (!quakes.isEmpty()) {
			Collections.sort(quakes);
			
			//ArrayList<Earthquake> lastQuakes = new ArrayList<Earthquake>();
			
			/*
			int index = 0;
			
			System.out.println("StartDate:" + startDate + "\nEnd Date" + endDate);
			while(index < quakes.size() && quakes.get(index).getEqTime().before(startDate)){
				index++;
			}
			System.out.println("After Before Index: "+index);
			while(index < quakes.size() && ! quakes.get(index).getEqTime().after(endDate)){
				lastQuakes.add(quakes.get(index++));
			}
			System.out.println("After Fin Index: "+index);
			System.out.println("Quakes original:" + quakes.size());
			*/
			//quakes = lastQuakes;
			//System.out.println("Quakes Sorted:" + quakes.size());
			Collections.sort(quakes, Collections.reverseOrder());
		%>

		<ul>
		<%
			for (Earthquake eq : quakes) {
		%>
			<li>

				<table>

					<tr>
						<th>ID:</th>
						<td><%= eq.getSlug() %></td>
					</tr>

					<tr>
						<th>Magnitude:</th>
						<td><%= String.format("%.1f", eq.getEqMagnitude()) %></td>
					</tr>

					<tr>
						<th>Depth:</th>
						<td><%= String.format("%.0f km", eq.getEqDepth()) %></td>
					</tr>

					<tr>
						<th>Date:</th>
						<td><%= String.format("%tb. %<td, %<tY, %<tl:%<tM %<tp", eq.getEqTime()) %></td>
					</tr>

					<!--  tr>
						<th>Modified:</th>
						<td><%= eq.getLastModified() %></td>
					</tr-->

					<tr>
						<th>Status:</th>
						<td><%= eq.getStatus() %></td>
					</tr>

					<tr>
						<th><a href="detail.jsp?id=<%=eq.getEqId()%>">Map &raquo;</a></th>
						<td></td>
					</tr>

				</table>

			</li>
		<%
			}
		%>
		</ul>

		<%
		} else {
		%>
		<p>No earthquake records are available.</p>
		<% } %>


	

