<%@page import="java.time.LocalDate"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Collections"%>
<%@page import="nz.cri.gns.rapidalert.da.DataAccess.Types"%>
<%@page import="nz.cri.gns.rapidalert.model.Earthquake"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page import="nz.cri.gns.rapidalert.model.DamageThreshold"%>
<%@page import="java.util.List"%>
<%@page import="java.time.LocalDateTime"%>
<%@page import="java.time.Instant"%>
<%@page import="java.time.ZoneId"%>
<%@page import="java.time.LocalDateTime"%>
<%@page import="nz.cri.gns.rapidalert.da.DataAccess"%>
<%@page import="nz.cri.gns.rapidalert.model.Region"%>
<%@page extends="nz.cri.gns.rapidalert.web.RapidAlertPageBase"%>
<%
LocalDate start = LocalDateTime.now().minusDays(30).toLocalDate();
LocalDate end = LocalDateTime.now().toLocalDate();
%>
<h3>Show only earthquakes between dates</h3>
<p>From: <input type="text" id="datepickerStart" value="<%=start%>"></p>
<p>To:  &nbsp;&nbsp; <input type="text" id="datepickerFin" value="<%=end%>"></p>
<input type="button" id="Update" value="Update"> 
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

<%

	DataAccess da = getDataAccess(request);
%>
		<div class="wrapper">

			<div class="quake-list">

				<h2>Latest earthquakes</h2>

				<% 
				List<Earthquake> quakes = da.listEarthquakes(start, end); 
				
				System.out.println(quakes);
				
				Collections.sort(quakes, Collections.reverseOrder());

				
				if (!quakes.isEmpty()) {
					/*
					Collections.sort(quakes);
					ArrayList<Earthquake> lastQuakes = new ArrayList<Earthquake>();
					int index = 0;
					LocalDateTime ldt = LocalDateTime.now().minusDays(10);
					Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
					Date cutOff = Date.from(instant);
					
					
					while(index < quakes.size() && quakes.get(index).getEqTime().after(cutOff)){
						lastQuakes.add(quakes.get(index++));
					}
					for(;index < quakes.size() && index < 10; index++){
						lastQuakes.add(quakes.get(index));
					}
					quakes = lastQuakes;
					Collections.sort(quakes, Collections.reverseOrder());
					*/
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

			</div>

		</div>

	<div class="footer">

		<div class="wrapper">

			<h3>
				<a href="#top">back to the top</a>
			</h3>

		</div>

	</div>

	<script
		src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
	<script>
		window.jQuery
				|| document
						.write('<script src="skin/scripts/vendor/jquery-1.10.2.min.js"><\/script>')
	</script>
	 <script>
  	$(function() {
    	$( "#datepickerStart" ).datepicker({
    		dateFormat: "yy-mm-dd"
    	});
  	});
  	$(function() {
		$( "#datepickerFin" ).datepicker({
    		dateFormat: "yy-mm-dd"
    	});
	});
  </script>
  <script>
  
$(document).ready(function(){
	
	//alert(datepickerStart.value);
	//alert(datepickerFin.value);
	$('#Update').click(function(){
		$.ajax({
			method: "POST",
			url: "getUpdate.jsp",
			data: {startTime: datepickerStart.value, finTime: datepickerFin.value, "${_csrf.parameterName}": "${_csrf.token}"},
			dataType: "html",
			success: function(html){
				$("div.quake-list").html( html );
			}
		});
	});
});
</script>
	
	<script src="skin/scripts/plugins.js"></script>
	<script src="skin/scripts/main.js"></script>
	<script src="//code.jquery.com/jquery-1.10.2.js"></script>
  	<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
 	<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
  
  

</body>
