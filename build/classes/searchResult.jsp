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
		.summary {
			background-color: #f0f8ff;
			padding: 15px;
			border-radius: 5px;
			margin-bottom: 20px;
			font-weight: bold;
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
		Disease disease = (Disease) request.getAttribute("disease");
		ArrayList<Supply> supplyList = (ArrayList<Supply>) request.getAttribute("supplyList");
		Integer diseaseIndex = (Integer) request.getAttribute("diseaseIndex");
		Map<Integer, Integer> supplyToInterestMap = (Map<Integer, Integer>) request.getAttribute("supplyToInterestMap");
		Double drgPaymentStandard = (Double) request.getAttribute("drgPaymentStandard");
		Double totalAmount = (Double) request.getAttribute("totalAmount");
		Double amountRankPercentile = (Double) request.getAttribute("amountRankPercentile");
		
		// 计算总价
		if (totalAmount == null) {
			totalAmount = 0.0;
		}
		
		Map<Integer, Integer> supplyToAverageQuantityMap = (Map<Integer, Integer>) request.getAttribute("supplyToAverageQuantityMap");
	%>

	<% if (disease != null) { %>
	<div class="disease-info">
		<h3>病种信息</h3>
		<p><strong>drg编码:</strong> <%= disease.getNAME() %></p>
		<p><strong>drg名称:</strong> <%= disease.getDESCRIPTION() != null ? disease.getDESCRIPTION() : "无描述" %></p>
		<% if (drgPaymentStandard != null && drgPaymentStandard > 0) { %>
		<p><strong>DRG支付标准:</strong> ¥<%= String.format("%.2f", drgPaymentStandard) %></p>
		<% } %>
	</div>
<% } %>

	<h3>关联的耗材列表</h3>
	<% if (supplyList != null && !supplyList.isEmpty()) { %>
	<table>
		<tr>
			<th>ID</th>
			<th>耗材id</th>
			<th>产品名称</th>
			<th>规格</th>
			<th>单价</th>
			<th>数量</th>
			<th>小计</th>
			<th class="action-column">操作</th>
		</tr>
		<% for (Supply supply : supplyList) { %>
		<tr data-supply="<%= supply.getID() %>">
			<td><%= supply.getID() %></td>
			<td><%= supply.getNAME() %></td>
			<td><%= supply.getPRODUCT_NAME() != null ? supply.getPRODUCT_NAME() : "无产品名称" %></td>
			<td><%= supply.getURL() != null ? supply.getURL() : "无规格" %></td>
			<td>
				<% 
					double unitPrice = 0.0;
					if (supply.getPRICE() != null && !supply.getPRICE().isEmpty() && !supply.getPRICE().equals("0")) {
						try {
							unitPrice = Double.parseDouble(supply.getPRICE());
							out.print("¥" + String.format("%.2f", unitPrice));
						} catch (NumberFormatException e) {
							out.print("暂无价格");
						}
					} else {
						out.print("暂无价格");
					}
				%>
			</td>
			<td>
				<%
					int averageQuantity = 0;
					if (supplyToAverageQuantityMap != null) {
						Integer quantity = supplyToAverageQuantityMap.get(supply.getID());
						if (quantity != null) {
							averageQuantity = quantity;
							out.print(averageQuantity);
						} else {
							out.print("0");
						}
					} else {
						out.print("0");
					}
				%>
			</td>
			<td>
				<%
					double subtotal = unitPrice * averageQuantity;
					totalAmount += subtotal;
					if (unitPrice > 0 && averageQuantity > 0) {
						out.print("¥" + String.format("%.2f", subtotal));
					} else {
						out.print("¥0.00");
					}
				%>
			</td>
			<td class="action-column">
				<%
					// 获取当前行号（从0开始）
					int rowIndex = ((List<Supply>) supplyList).indexOf(supply);
					
					// 获取行对应的主题ID
					int interestId = -1;
					@SuppressWarnings("unchecked")
					List<Integer> rowToInterestList = (List<Integer>) request.getAttribute("rowToInterestList");
					if (rowToInterestList != null && rowIndex >= 0 && rowIndex < rowToInterestList.size()) {
						interestId = rowToInterestList.get(rowIndex);
					}
				%>
				<button class="continue-button" onclick="continueRecommendation(<%= supply.getID() %>, <%= diseaseIndex != null ? diseaseIndex : -1 %>, <%= interestId %>, <%= rowIndex %>)">继续推荐</button>
			</td>
		</tr>
		<% } %>
		<tr>
			<td colspan="6" style="text-align: right; font-weight: bold;">总价:</td>
			<td colspan="2" style="font-weight: bold;">¥<%= String.format("%.2f", totalAmount) %></td>
		</tr>
		<% if (amountRankPercentile != null && amountRankPercentile >= 0) { %>
		<tr>
			<td colspan="6" style="text-align: right; font-weight: bold;">总价排名百分比:</td>
			<td colspan="2" style="font-weight: bold;">
				<%= String.format("%.2f", amountRankPercentile) %>%
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