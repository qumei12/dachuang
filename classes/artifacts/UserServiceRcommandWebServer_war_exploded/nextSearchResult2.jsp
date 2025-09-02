<%@page import="java.util.List"%>
<%@page import="javabean.Mashup"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="javabean.API"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Api推荐结果</title>
<style type="text/css">
body {
	font-family: Georgia, "Times New Roman", Times, serif;
	font-size: large;
	margin: 5px;
}

.header {
	margin: 10px;
	height: 100px;
	text-align: center;
}

.left {
	float: left;
	width: 200px;
	height: 100%;
}

.right {
	float: right;
	width: 400px;
	height: 100%;
}

.main {
	margin-left: 200px;
	margin-right: 200px;
}

#nav {
	height: 30px;
	width: 100%;
	background-color: gray;
}

#nav ul {
	margin: 0 0 0 30px;
	padding: 0px;
	font-size: 12px;
	color: #FFF;
	line-height: 30px;
	whitewhite-space: nowrap;
}

#nav li {
	display: inline;
	float: left;
	user-select:none;
}

#nav li a {
	text-decoration: none;
	font-family: Arial, Helvetica, sans-serif;
	padding: 7px 10px;
	font-size: large;
	color: #FFF;
	
}

#nav li a:hover {
	color: black;
	background-color: white;
}

.footer {
	margin: 10px;
	padding: 15px;
	text-align: center;
}

#title {
	padding-top: 1px;
	color: black;
	font-size: 180%;
}

#resultTable{
	text-align: center;
}

.apiTable{
	width: 100%;
	text-align: center;
}

table {
	text-align: center;
	border: thin solid black;
	border-collapse: collapse;
	background-color: white;
}

td, th {
	border: thin dotted black;
	padding: 5px;
}

th.url, td.url {
	width: 80px;
}

.foot {
	width: 100%;
	position: relative;
	bottom: 100px;
	margin-top:150px;
}

.foot>.link {
	text-align: center;
	margin-bottom: 10px;
}

.foot>.link>a {
	font-size: 12px;
	font-family: "宋体";
	text-decoration: underline;
}

.copyright {
	text-align: center;
}

p, p>a {
	font-size: 12px;
	font-family: "宋体";
	color: #666;
}
</style>
<script type="text/javascript" src='jquery-1.12.2.js'></script>
<script type="text/javascript">
	function showTable(type) {
		if (type == 'Mashup') {
			$('#apiTable').css('display', 'none');
			$('#mashupTable').css('display', 'block');
		} else if (type == 'API') {
			$('#apiTable').css('display', 'block');
			$('#mashupTable').css('display', 'none');
		}
	}
	var chain = [];
	
	function nextRecommand(id){
		chain.push(id);
		var url = './nextSearch?id=' + id;
		//alert(url);
		window.open(url);
		//$.post(url);
	}
</script>
</head>
<body>
	<header class='header'>
		<div id='title'>
			<h1>Api推荐结果</h1>
		</div>
	</header>
	<div class='content'>
		<div class='left'>
			<p></p>
		</div>
		<div class='right'>
			<p></p>
		</div>

		
		<div class='main'>
			<div id='resultTable'>
				<div><p>&nbsp</p></div>
				
				<%
					Object obj_r = request.getAttribute("result");
					ArrayList<API> result = (ArrayList<API>) obj_r;

					Object obj_c = request.getAttribute("chain");
					ArrayList<API> chain = (ArrayList<API>) obj_c;
				%>
				<table class='tb'>
							<tr>
							
				<%
					for(int i = 0;i < chain.size();i++){
				%>
								<th>已有API</th>
				<%
					}	
				%>
							</tr>
							<tr>
								
				<%
					
					for(int i = 0; i < chain.size(); i++){
						API api = chain.get(i);
				%>
						<script type="text/javascript">
							chain.push(<%=api.getN_ID() %>);
						</script>
						<td><%=api.getC_NAME() %></td>
				<%
					}
				%>
						
								
								
							</tr>
				</table>
				
				<%
					for(int i = 0; i < result.size(); i++){
						API resApi = result.get(i);
				%>
				<div style="display:block">
				<div><h2>推荐结果<%=(i + 1) %></h2></div>
					<table class='apiTable'>
						<tr>
							
							
							<th>推荐Api名称</th>
							<th>推荐</th>
						</tr>
						<tr>
							
							<td><a href='##'><%=resApi.getC_NAME() %></a></td>
							<td><input type='button' value='继续推荐' onclick="nextRecommand(<%=resApi.getN_ID() %>);"/></td>
						</tr>
					</table>
				</div>

				<% 
				
					}
				%>
				
			</div>
		</div>

	</div>
	<div class="foot">
		<div class="copyright">
        	<p>
            ©2016 Designed by Wang.Yan <a href="#">使用说明</a> <a href="#">意见反馈</a> 京ICP证000001号 
        	</p>
    	</div>
	</div>
</body>
</html>