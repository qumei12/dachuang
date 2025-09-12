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
	font-size: large;
	float: right;
	width: 200px; /* 调整宽度以匹配左侧面板 */
	height: 100%;
}

.main {
	margin-left: 210px; /* 稍微增加左侧边距以考虑滚动条 */
	margin-right: 210px; /* 确保左右边距对称 */
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
	width: 100%; /* 确保表格宽度适应容器 */
	max-width: 100%; /* 防止表格溢出 */
}

td, th {
	border: thin dotted black;
	padding: 5px;
}

th.url, td.url {
	width: 120px; /* 增加URL列宽度以改善显示效果 */
}

.foot {
	width: 100%;
	clear: both; /* 清除浮动影响 */
	margin-top: 30px;
	padding-bottom: 20px;
}

.foot>.link {
	text-align: center;
	margin-bottom: 15px;
}

.foot>.link>a {
	font-size: 14px;
	font-family: "Microsoft Yahei", sans-serif;
	text-decoration: none;
	color: #333;
	padding: 0 10px;
}

.copyright {
	text-align: center;
	margin-top: 10px;
	border-top: 1px solid #eee;
	padding-top: 10px;
}

p, p>a {
	font-size: 13px;
	font-family: "Microsoft Yahei", sans-serif;
	color: #555;
}

/* 新增推荐表格样式 */
.recommendation-table {
	margin: 0 auto;
	width: 100%; /* 使表格适应容器宽度 */
	max-width: 100%; /* 防止表格溢出 */
	box-shadow: 0 0 5px rgba(0,0,0,0.05);
	padding: 0 10px; /* 添加一些内边距 */
}

.recommendation-table th {
	background-color: #f5f5f5;
}

.recommendation-table td, 
.recommendation-table th {
	padding: 12px;
	vertical-align: middle;
}

.recommendation-table .api-name {
	font-weight: bold;
	color: #222;
}

.recommendation-table .btn-recommend {
	background-color: #4CAF50;
	color: white;
	border: none;
	padding: 8px 16px;
	cursor: pointer;
	border-radius: 4px;
}

.recommendation-table .btn-recommend:hover {
	background-color: #45a049;
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
	function nextRecommand(id){
		// 跳转到继续推荐页面
		window.location.href = './nextSearch?id=' + id;
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
					// 获取推荐结果
					ArrayList<API> recommandSupplyList = (ArrayList<API>) request.getAttribute("recommandSupplyList");
					API currentSupply = (API) request.getAttribute("currentSupply");
					
					// 显示当前耗材信息
					if(currentSupply != null) {
				%>
				<div style="display:block">
					<div><h2>当前耗材: <%= currentSupply.getC_NAME() %></h2></div>
				</div>
				<%
					}
				%>
		
	</div>
</div>

	</div>
	<div class="foot">
		<table class='apiTable'>
			<thead>
				<tr>
					<th>推荐耗材</th>
					<th>操作</th>
				</tr>
			</thead>
			<tbody>
				<%
					if(recommandSupplyList != null && !recommandSupplyList.isEmpty()) {
						for(API recommandApi : recommandSupplyList) {
				%>
				<tr>
					<td>
						<a href='<%= recommandApi.getC_URL() != null && !recommandApi.getC_URL().isEmpty() ? recommandApi.getC_URL() : "#" %>' target="_blank">
							<%= recommandApi.getC_NAME() %>
						</a>
					</td>
					<td>
						<input type='button' class="btn-recommend" value='继续推荐' 
							onclick="nextRecommand(<%=recommandApi.getN_ID() %>);"/>
					</td>
				</tr>
				<%
						}
					} else {
				%>
				<tr>
					<td colspan="2">没有推荐结果</td>
				</tr>
				<%
					}
				%>
			</tbody>
		</table>
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