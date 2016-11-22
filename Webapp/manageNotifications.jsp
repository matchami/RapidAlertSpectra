<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="nz.cri.gns.rapidalert.da.DataAccess.Types"%>
<%@page import="nz.cri.gns.rapidalert.model.Notify"%>
<%@page import="java.util.Arrays"%>
<%@page import="nz.cri.gns.rapidalert.model.NotifyGroup"%>
<%@page import="java.util.List"%>
<%@page import="nz.cri.gns.rapidalert.da.DataAccess"%>
<%@page extends="nz.cri.gns.rapidalert.web.RapidAlertPageBase"%>

<%
	DataAccess da = getDataAccess(request);
	
	String email = request.getParameter("email");
	String group = request.getParameter("group");
	String delete = request.getParameter("delete");
	if (email != null && group != null) {
		Notify notify = new Notify();
		System.out.println("Looking for NotifyGroup with id "+Integer.parseInt(group));
		
		NotifyGroup myGroup = da.get(DataAccess.NOTIFY_GROUP, Integer.parseInt(group));
		System.out.println(myGroup);
		notify.setGroup(myGroup);
		notify.setEmail(email);
		da.save(notify);
	} else if (delete != null) {

		Notify notify = da.get(DataAccess.NOTIFY, Integer.parseInt(delete));
		da.delete(notify);

	}

%>
		<div class="wrapper">
		
			<div class="quake-list">
				<fieldset><legend>Add notification</legend>
					<form method="post" action="manageNotifications.jsp">
					<p><label for="email">Email address</label><input type="text" name="email" id="email"/></p>
					<p><select name="group" id="group">
						<% 	List<NotifyGroup> groups = da.listAll(DataAccess.NOTIFY_GROUP);  
							for (NotifyGroup availableGroup : groups) {
								%><option value="<%=availableGroup.getId() %>"><%=availableGroup.getDescription() %></option><%
							}
						%>
					</select></p>
					<input type="submit" value="Add email to notification list"/>
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
					</form>
				</fieldset>
			</div>

			<div class="quake-list">

				<h2>Notification lists</h2>

				<% 
				System.out.println(da);
				List<Notify> notifications = da.listAll(DataAccess.NOTIFY); 
				
				if (!notifications.isEmpty()) {
					//Group notifications by level
					List<Notify> dev = new ArrayList<Notify>(notifications.size());
					List<Notify> real = new ArrayList<Notify>(notifications.size());
					List<Notify> error = new ArrayList<Notify>(notifications.size());
					
					for (Notify n : notifications) {
						switch (n.getGroup().getDescription()) {
							case "dev":
								dev.add(n);
								break;
							case "real":
								real.add(n);
								break;
							case "error":
								error.add(n);
								break;
						}
					}
				%>

				<table class="emailTable"><thead><tr><th>Dev</th><th>Real</th><th>Error</th></tr></thead>
				<tbody><tr>
				
				<%
				for (List<Notify> list : new List[] {dev, real, error}) {
					%><td><ul>
					<%
					for (Notify n : list) {
						%><li><%= n.getEmail()%> <a onclick="delEmail(<%=n.getId()%>)" style="cursor: pointer">delete</a></li>
					<%
					}
					%></ul></td>
				<%
				}
				%>
						
				</tbody>
				</table>
				<script>
				function delEmail(id) {
					$("#delId").val(id);
					$("#delForm").submit();
				}
				</script>
				<form method="post" action="manageNotifications.jsp" id="delForm">
				<input type="hidden" name="delete" id="delId" />
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
				</form>

				<%
				} else {
				%>
				<p>No notifications records are available.</p>
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
	<script src="skin/scripts/plugins.js"></script>
	<script src="skin/scripts/main.js"></script>

</body>
