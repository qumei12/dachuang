<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="javabean.*" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>病种耗材推荐结果</title>
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
		.disease-info {
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
		.continue-button {
			background-color: #4CAF50;
			color: white;
			padding: 5px 10px;
			border: none;
			border-radius: 4px;
			cursor: pointer;
			font-size: 14px;
		}
		.continue-button:hover {
			background-color: #45a049;
		}
		.action-column {
			width: 120px;
			text-align: center;
		}
	</style>
	<script type="text/javascript">
		function continueRecommendation(supplyId, diseaseIndex, interestId, rowIndex) {
			// 跳转到继续推荐页面，传递选中的耗材ID、原始病种索引和兴趣主题ID
			var url = "nextSearch?id=" + supplyId + "&rowIndex=" + rowIndex;
			if (diseaseIndex !== undefined && diseaseIndex >= 0) {
				url += "&diseaseIndex=" + diseaseIndex;
			}
			
			// 如果有明确的兴趣主题ID，则传递
			if (interestId >= 0) {
				url += "&interestId=" + interestId;
			}
			
			window.location.href = url;
		}
	</script>
</head>
<body>
<div class="container">
	<h2>病种耗材推荐结果</h2>

	<%
		Mashup disease = (Mashup) request.getAttribute("disease");
		ArrayList<API> supplyList = (ArrayList<API>) request.getAttribute("supplyList");
		Integer diseaseIndex = (Integer) request.getAttribute("diseaseIndex");
		Map<Integer, Integer> supplyToInterestMap = (Map<Integer, Integer>) request.getAttribute("supplyToInterestMap");
	%>

	<% if (disease != null) { %>
	<div class="disease-info">
		<h3>病种信息</h3>
		<p><strong>名称:</strong> <%= disease.getC_NAME() %></p>
		<p><strong>描述:</strong> <%= disease.getC_DESCRIPTION() != null ? disease.getC_DESCRIPTION() : "无描述" %></p>
		<% if (disease.getC_URL() != null && !disease.getC_URL().isEmpty()) { %>
		<p><strong>网址:</strong> <a href="<%= disease.getC_URL() %>" target="_blank"><%= disease.getC_URL() %></a></p>
		<% } %>
	</div>
	<% } %>

	<h3>关联的耗材列表</h3>
	<% if (supplyList != null && !supplyList.isEmpty()) { %>
	<table>
		<tr>
			<th>ID</th>
			<th>耗材名称</th>
			<th>描述</th>
			<th>网址</th>
			<th class="action-column">操作</th>
		</tr>
		<% for (API supply : supplyList) { %>
		<tr data-supply="<%= supply.getN_ID() %>">
			<td><%= supply.getN_ID() %></td>
			<td><%= supply.getC_NAME() %></td>
			<td><%= supply.getC_DESCRIPTION() != null ? supply.getC_DESCRIPTION() : "无描述" %></td>
			<td>
				<% if (supply.getC_URL() != null && !supply.getC_URL().isEmpty()) { %>
					<a href="<%= supply.getC_URL() %>" target="_blank"><%= supply.getC_URL() %></a>
				<% } else { %>
					无网址
				<% } %>
			</td>
			<td class="action-column">
				<%
					// 获取当前行号（从0开始）
					int rowIndex = ((List<API>) supplyList).indexOf(supply);
					
					// 获取行对应的主题ID
					int interestId = -1;
					@SuppressWarnings("unchecked")
					List<Integer> rowToInterestList = (List<Integer>) request.getAttribute("rowToInterestList");
					if (rowToInterestList != null && rowIndex >= 0 && rowIndex < rowToInterestList.size()) {
						interestId = rowToInterestList.get(rowIndex);
					}
				%>
				<button class="continue-button" onclick="continueRecommendation(<%= supply.getN_ID() %>, <%= diseaseIndex != null ? diseaseIndex : -1 %>, <%= interestId %>, <%= rowIndex %>)">继续推荐</button>
			</td>
		</tr>
		<% } %>
	</table>
	<% } else { %>
	<p>该病种没有关联的耗材。</p>
	<% } %>

	<div class="back-link">
		<a href="index.jsp">返回首页</a>
	</div>
</div>
</body>
</html>