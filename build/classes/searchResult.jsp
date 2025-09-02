<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="javabean.API"%>
<%@page import="javabean.Mashup"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Mashup API推荐结果</title>
	<style type="text/css">
		body {
			font-family: Arial, sans-serif;
			margin: 20px;
		}
		.container {
			max-width: 1000px;
			margin: 0 auto;
		}
		table {
			width: 100%;
			border-collapse: collapse;
			margin: 20px 0;
		}
		th, td {
			border: 1px solid #ddd;
			padding: 12px;
			text-align: left;
		}
		th {
			background-color: #f2f2f2;
		}
		tr:nth-child(even) {
			background-color: #f9f9f9;
		}
		.mashup-info {
			background-color: #e7f3ff;
			padding: 15px;
			border-radius: 5px;
			margin-bottom: 20px;
		}
		.back-link {
			margin-top: 20px;
		}
		a {
			color: #4CAF50;
			text-decoration: none;
		}
		a:hover {
			text-decoration: underline;
		}
	</style>
</head>
<body>
<div class="container">
	<h2>Mashup API推荐结果</h2>

	<%
		Mashup mashup = (Mashup) request.getAttribute("mashup");
		ArrayList<API> apiList = (ArrayList<API>) request.getAttribute("apiList");
	%>

	<% if (mashup != null) { %>
	<div class="mashup-info">
		<h3>Mashup信息</h3>
		<p><strong>名称:</strong> <%= mashup.getC_NAME() %></p>
		<p><strong>描述:</strong> <%= mashup.getC_DESCRIPTION() != null ? mashup.getC_DESCRIPTION() : "无描述" %></p>
		<% if (mashup.getC_URL() != null && !mashup.getC_URL().isEmpty()) { %>
		<p><strong>网址:</strong> <a href="<%= mashup.getC_URL() %>" target="_blank"><%= mashup.getC_URL() %></a></p>
		<% } %>
	</div>
	<% } %>

	<h3>关联的API列表</h3>
	<% if (apiList != null && !apiList.isEmpty()) { %>
	<table>
		<tr>
			<th>ID</th>
			<th>API名称</th>
			<th>描述</th>
			<th>网址</th>
		</tr>
		<% for (API api : apiList) { %>
		<tr>
			<td><%= api.getN_ID() %></td>
			<td><%= api.getC_NAME() %></td>
			<td><%= api.getC_DESCRIPTION() != null ? api.getC_DESCRIPTION() : "无描述" %></td>
			<td>
				<% if (api.getC_URL() != null && !api.getC_URL().isEmpty()) { %>
				<a href="<%= api.getC_URL() %>" target="_blank">访问</a>
				<% } else { %>
				无网址
				<% } %>
			</td>
		</tr>
		<% } %>
	</table>
	<% } else { %>
	<p>该mashup没有关联的API。</p>
	<% } %>

	<div class="back-link">
		<a href="index.jsp">返回首页</a>
	</div>
</div>
</body>
</html>
