<%@page import="javabean.Mashup"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="javabean.API"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>服务推荐原型系统</title>
<style type="text/css">
body {
	font-family: Georgia, "Times New Roman", Times, serif;
	font-size: big;
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
	font-size: big;
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

table {
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

			//$('#mashup').css('color','black');
			//$('#mashup').css('background-color','white');

			//$('#api').css('color','none');
			//$('#api').css('background-color','gray');

		} else if (type == 'API') {
			$('#apiTable').css('display', 'block');
			$('#mashupTable').css('display', 'none');

			//$('#api').css('color','black');
			//$('#api').css('background-color','white');

			//$('#mashup').css('color','none');
			//$('#mashup').css('background-color','gray');
		}
	}
</script>
</head>
<body>
	<header class='header'>
		<div id='title'>
			<h1>服务推荐原型系统</h1>
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
			<div id='nav'>
			<ul>
				<li id='mashup' onclick='showTable("Mashup");'><a>Mashup</a></li>
				<li id='api' onclick='showTable("API");'><a>API</a></li>
			</ul>
			</div>
			<div id='resultTable'>
				<div><p>&nbsp</p></div>
				
				<div id='mashupTable' style="display:block">
				<div><h2>Mashup推荐结果</h2></div>
					<table>
						<tr>
							<th>Mashup名称</th>
							<th>Mashup详情</th>
							<th>Mashup URL</th>
						</tr>
						<%
							Object obj_m = request.getAttribute("mashupList");
							ArrayList<Mashup> list_m = (ArrayList<Mashup>) obj_m;
							for (int i = 0; i < list_m.size(); i++) {
						%>
						<tr>
							<td><%=list_m.get(i).getC_NAME()%></td>
							<td><%=list_m.get(i).getC_DESCRIPTION()%></td>
							<td><%=list_m.get(i).getC_URL()%></td>
						</tr>
						<%
							}
						%>
					</table>
				</div>
				
				<div id='apiTable' style="display:none">
				<div><h2>API推荐结果</h2></div>
					<table>
						<tr>
							<th>API名称</th>
							<th>API详情</th>
							<th>API URL</th>
							<th>推荐</th>
						</tr>
						<%
							Object obj_a = request.getAttribute("apiList");
							ArrayList<API> list_a = (ArrayList<API>) obj_a;
							for (int i = 0; i < list_a.size(); i++) {
						%>
						<tr>
							<td><%=list_a.get(i).getC_NAME()%></td>
							<td><%=list_a.get(i).getC_DESCRIPTION()%></td>
							<td><%=list_a.get(i).getC_URL()%></td>
							<td><input type='button' value='继续推荐' onclick="window.open('./nextSearch?idList=<%=list_a.get(i).getN_ID()%>')"/></td>
						</tr>
						<%
							}
						%>
					</table>
				</div>
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