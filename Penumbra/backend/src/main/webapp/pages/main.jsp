<%@ page import="com.google.appengine.api.datastore.Entity" %>
<html>
<body>
	<h1>Main Menu</h1>

	Function : <a href="addCustomerPage">Add Customer</a>
	<hr />

	<h2>All Customers</h2>
	<table border="1">
		<thead>
			<tr>
				<td>Email</td>
				<td>Password</td>
				<td>Action</td>
			</tr>
		</thead>
		<%
		  Entity user = (Entity) request.getAttribute("user");
		%>
		<tr>
		  <td><%=user.getProperty("email") %></td>
		</tr>
	</table>

</body>
</html>