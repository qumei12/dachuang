<%@page import="java.util.ArrayList"%>
<%@page import="javabean.API"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Mashup-Api关联关系</title>
<style type="text/css">
	body{
		background-color: 	#b5a789;
		font-family:		Georgia, "Times New Roman", Times, serif;
		font-size:			big;
		margin:				5px;
	}
	.header{
		background-color:	#675c47;
		margin:		10px;
		height:		100px;
		text-align:	center;
	}
	.left{
		float:		left; 
		width:		200px;
		height: 	100%;
	}
	.right{
		float:		right; 
		width:		200px;
		height: 	100%;
	}
	.main{
		margin-left: 200px;
		margin-right:200px;		
	}
	.footer{
		margin:		10px;
		padding:	15px;
		text-align:	center;
	}
	
	#title{
		padding-top: 1px;
		color: white;
		font-size:  180%;
	}
	
	table{
		margin-left:	20px;
		margin-right:	20px;
		border:			thin solid black;
		border-collapse: collapse;
		background-color:	white;
	}
	
	td, th{
		border:			thin dotted black;
		padding:		5px;
	}
	th.url, td.url{
		width:			80px;
	}
	.buttons{
		margin-left:	100px;
		margin-top:		20px;
		text-align:		center;
	}
	.pageBar{
		margin-left:	100px;
		margin-top:		20px;
	}
</style>
</head>
<body>
	<header class='header'>
		<div id='title'>
			<p>
				<%= request.getAttribute("mashupName") %>
				详情
			</p>
		</div>
	</header>
	<div class='content'>
		<div class='left'>
			
		</div>
		<div class='right'>
			
		</div>
		

		<div class='main'>
			<div id='resultTable'>
				<table>
					<tr>
						<th>API名称</th>
						<th>API详情</th>
						<th>API URL</th>
					</tr>
					<%
						Object obj = request.getAttribute("apilist");
						ArrayList<API> list = (ArrayList<API>)obj;
						for(int i = 0;i < list.size(); i++){
							%>
							<tr>
								<td><%= list.get(i).getC_NAME() %></td>
								<td><%= list.get(i).getC_DESCRIPTION() %></td>
								<td><%= list.get(i).getC_URL() %></td>
							</tr>
							<%
						}
					%>
				</table>
			</div>
			<div class='buttons'>
				<input type='button' value='后退' onclick='javascript:history.go(-1)'/>
			</div>
		</div>
		
	</div>
	<footer class='footer'>Footer</footer>
</body>
</html>