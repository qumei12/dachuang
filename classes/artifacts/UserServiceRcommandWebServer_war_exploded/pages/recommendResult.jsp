<%@page import="javabean.API"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>推荐结果</title>
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
	caption{
		font-size:		120%;
		font-weight:	bold;
	}
</style>
</head>
<body>
	<header class='header'>
		<div id='title'>
			<p>
				推荐结果
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
				<%
					Object obj = request.getAttribute("result");
					List<List<API>> result = (List<List<API>>)obj;
					
					for(int i = 0;i < result.size();i++){
				%>
				<table>
					<caption>推荐组合<%= (i+1) %></caption>
					<tr>
						<th>API名称</th>
						<th>API详情</th>
						<th>API URL</th>
					</tr>
					
					<%
						for(int j = 0;j < result.get(i).size();j++){
							API api = result.get(i).get(j);
					%>
						
					<tr>
						<td><%= api.getC_NAME()%></td>
						<td><%= api.getC_DESCRIPTION()%></td>
						<td><a href='<%= api.getC_URL()%>'><%= api.getC_URL()%></a></td>
					</tr>
					<%
						}
					%> 
			
				
				</table>
				<br>
				<br>
				<%
					}
				%>
			
			</div>
			<div class='buttons'>
				<input type='button' value='后退' onclick='javascript:history.go(-1)'/>
			</div>
		</div>
		
	</div>
	<footer class='footer'>Footer</footer>
</body>
</html>