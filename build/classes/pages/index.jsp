<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>服务推荐首页</title>
<script type="text/javascript" src='jquery-1.12.2.js'></script>
<script type="text/javascript" src='jquery-ui.js'></script>
<script type="text/javascript">
/* 	$(function(){
		$('input').on('click',function(){
			alert('HelloJQuery');
		});
	}); */
	
	
	var currentPage = -1;
	var totalPage = -1;
	
	$(document).ready(function(){
		//changePage(1);
		//$('#clear_btn').click(function(){
			//alert(123);
			//$('#dialog').dialog();
		//});
		if(currentPage == -1){
			currentPage = 1;
		}
		
		if(totalPage == -1){
			getTotalPageAmount();
		}
		
		changePage(1);
		fillPageBar();
	});
	
/* 	if(currentPage == -1){
		currentPage = 1;
	}
	
	if(totalPage == -1){
		getTotalPageAmount();
	}
	
	changePage(1); */
	
	function getTotalPageAmount(){
		$.ajax({
			type:	'post',
			url:	'../TotalPageAmount',
			async:   false,
			data:	{
				
			},
			success:	function(data){
				var jsondata = eval("("+data+")");
				totalPage = jsondata.resultValue.pageAmount;
				//alert("ajax" + totalPage);
			}
		});
	}
	
	function changePage(page){
		currentPage = page;
		getMashupTable(currentPage);
		fillPageBar();
	}
	
	function getMashupTable(page){
		//alert(123);
		$.ajax({
			type:	'post',
			url:	'../MashupTable',
			data:	{
				'page': page
			},
			success:	function(data){
				var jsondata = eval("("+data+")");
				//alert(123);
				//alert(jsondata.resultValue);
				fillMashupTable('resultTable', jsondata.resultValue.mashupList)
				//totalPage = jsondata.resultValue.pageAmount;
				//alert("ajax" + totalPage);
			}
		});
	}
	
	var choosenMashupId = [];
	
	function checkboxOnChangeEvent(id){
		if($('#cb' + id).is(':checked')){
			choosenMashupId.push(id);
			//alert('Mashup' + id + '已被选中');
		} else {
			for(var i = 0;i < choosenMashupId.length;i++){
				if(choosenMashupId[i] == id){
					choosenMashupId.splice(i,1);
					break;
				}
			}
			//alert('Mashup' + id + '被取消选中');
		}
		drawCheckedBox('CheckedTable');
		//alert('当前数组中的值：' + choosenMashupId);
	}
	
	function fillMashupTable(position, list){
		var str = "";
		str +=  "<table><tr>";                       
		str +=  "<th></th>";
		str +=	"<th>Id</th>";
		str +=	"<th>Mashup名称</th>";
		str +=	"<th>Mashup详细介绍</th>";
		str +=	"<th class='url'>MashupURL</th>";
		str +=	"<th>Mashup最后更新日期</th>";
		str +=	"<tr>";
			
		$.each(list, function(index, obj){
			str += "<tr>";
			str += "<td><input type='checkbox' value='" + obj.n_ID + "' class='checkbox' id='cb" + obj.n_ID + "' onchange='checkboxOnChangeEvent(" + obj.n_ID + ")'/></td>";
			str += "<td>" + obj.n_ID + "</td>";
			str += "<td><a href='../MashupApiRelation?mashupId=" + obj.n_ID + "&mashupName=" + obj.c_NAME + "'>" + obj.c_NAME + "</a></td>";
			str += "<td>" + obj.c_DESCRIPTION + "</td>";
			str += "<td><a href='" + obj.c_URL + "'>" + obj.c_URL + "</a></td>"
			str += "<td>" + obj.c_DATE + "</td>"
			str += "</tr>";
		});
		str += "</table>";
		
		$('#' + position).html(str);
		//alert(123);
		
		$('.checkbox').each(function(index){
			//alert(this.id);
			for(var i = 0;i < choosenMashupId.length;i++){
				if('cb' + choosenMashupId[i] == this.id){
					$(this).attr('checked', 'checked');
					//break;
				}
			}
		});
	}
	
	function recommand(){
		var sendlist =  [];
		//alert(choosenMashupId);
		$.ajax({
			type:	'post',
			url:	'../Recommand',
			data:	{
				'idList':	choosenMashupId
			},
			success:	function(data){
				location.href="./recommendResult.jsp";
				//var jsondata = eval("("+data+")");
				//alert(123);
				//alert(jsondata.resultValue);
				//fillMashupTable('resultTable', jsondata.resultValue.mashupList)
				//totalPage = jsondata.resultValue.pageAmount;
				//alert("ajax" + totalPage);
			}
		});
	}
	
	function fillPageBar(){
		//alert(currentPage);
		//totalPage = totalPage;
		//alert(totalPage);
		var	str = "";
		str += "<span>第" + currentPage + "页/共" + totalPage + "页</span>";
		//alert(str);
		str += "<input type='button' value='1' onclick='changePage(1)'>";
		str += "......";
		if(currentPage == 1 || currentPage == 2){
			for(var i = 2; i <= 2 + 7; i++){
				str += "<input type='button' value='" + i + "' onclick='changePage(" + i + ")'>";
			}
		} else if(currentPage >= totalPage - 8){
			for(var i = totalPage - 8; i <= totalPage - 1; i++){
				str += "<input type='button' value='" + i + "' onclick='changePage(" + i + ")'>";
			}
		} else {
			for(var i = currentPage - 1;i <= currentPage + 6; i++){
				str += "<input type='button' value='" + i + "' onclick='changePage(" + i + ")'>";
			}
		}
		str += "......";
		str += "<input type='button' value='" + totalPage + "' onclick='changePage(" + totalPage + ")'>";
		str += "&nbsp&nbsp&nbsp";
		str += "转到第";
		str += "<input type='text' id='goto' size='3'>";
		str += "页";
		str += "&nbsp&nbsp";
		str += "<input type='button' value='跳转' onclick='gotoPage();'>"
		$(".pageBar").html(str);
	}
	
	function gotoPage(){
		var val_s = $('#goto').val();
		var val_i = parseInt(val_s);
		if(val_i >= 1 && val_i <= totalPage){
			changePage(val_i);
		} else {
			$('#goto').val('');
			alert('请输入合法页码');
		}
	}
	
	function checkedBoxTableChange(index, id){
		choosenMashupId.splice(index,1);
		checkboxOnChangeEvent(index);
		$('#cb' + id).attr('checked',false);
	}
	
	function drawCheckedBox(position){
		var str = "";
		str +=  "<table><caption>已选中Mashup列表</caption><tr>";
		str += 	"<th></th>"
		str +=	"<th>序号</th>";
		str +=	"<th>MashupID</th>";
		str +=	"</tr>";
			
//		$.each(choosenMashupId, function(index, obj){
//			str += "<tr>";
//			str += "<td><input type='checkbox' value='" + obj.n_ID + "' class='checkbox' id='cb" + obj.n_ID + "' onchange='checkboxOnChangeEvent(" + obj.n_ID + ")'/></td>";
//			str += "<td>" + obj.n_ID + "</td>";
//			str += "<td><a href='../MashupApiRelation?mashupId=" + obj.n_ID + "&mashupName=" + obj.c_NAME + "'>" + obj.c_NAME + "</a></td>";
//			str += "<td>" + obj.c_DESCRIPTION + "</td>";
//			str += "<td><a href='" + obj.c_URL + "'>" + obj.c_URL + "</a></td>"
//			str += "<td>" + obj.c_DATE + "</td>"
//			str += "</tr>";
//		});

		for(var i = 0;i < choosenMashupId.length; i++){
			str += "<tr>";
			str += "<td><input type='checkbox' class='CheckedBoxTable' id='cbt" + choosenMashupId[i] + "' checked='true' onchange='checkedBoxTableChange(" + i + ", " + choosenMashupId[i] + ")'></td>";
			str += "<td>" + (i + 1) + "</td>";
			str += "<td>" + choosenMashupId[i] + "</td>";
			str += "</tr>";
		}
		str += "</table>";
		//str += "<input type='button' value='提交查询' onclick=''>"
		
		$('#' + position).html(str);
		//alert(123);
		
//		$('.checkbox').each(function(index){
//			//alert(this.id);
//			for(var i = 0;i < choosenMashupId.length;i++){
//				if('cb' + choosenMashupId[i] == this.id){
//					$(this).attr('checked', 'checked');
//					//break;
//				}
//			}
//		});
	}
	
	function clearChoosenArray(){
		//alert(123);
		$('.checkbox').each(function(index){
			$(this).attr('checked', false);
		});
		choosenMashupId = [];
		//alert(choosenMashupId);
		changePage(1);
	}
	
	

</script>

<link rel="stylesheet" href="jquery-ui.css"  type="text/css" />
<link rel="stylesheet" href="style.css"  type="text/css" />

</head>
<body> 
	<header class='header'>
		<div id='title'>
			<p>服务推荐原型系统</p>
		</div>
	</header>
	<div class='content'>
		<div class='left'>
			<div id='CheckedTable' style='position:fixed'>
				
			</div>
		</div>
		<div class='right'>
			
		</div>
		

		<div class='main'>
			<div id='resultTable'>
			
			</div>
			<div class='buttons'>
				<input type='button' id='' value='提交查询' onclick="window.open('../Recommand?idList=' + choosenMashupId.toString())"/>
				&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
				<input id="clear_btn" type='button' value='清空选择' onclick='clearChoosenArray();'/>
			</div>
			
			<div class='pageBar'>
			
			</div>
			<script type="text/javascript">
				/* fillPageBar(); */
			</script>
		</div>
		
	</div>
	
	<footer class='footer'>Footer</footer>
</body>
</html>