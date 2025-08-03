<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>服务推荐原型系统</title>
<script type="text/javascript" src='jquery-1.12.2.js'></script>
<style type="text/css">
.pic {
	text-align: center;
	margin-bottom: 30px;
}

.logo {
	height: 130px;
	width: 270px;
}

.search {
	text-align: center;
}

.input {
	width: 540px;
	height: 36px;
}

.btn {
	border: 0;
	width: 100px;
	height: 40px;
	background: #36F;
	font-size: 15px;
	color: #fff;
}

.foot {
	width: 100%;
	position: absolute;
	bottom: 100px;
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
</head>
<body>
	<div class="body">
    	<div class="pic">
        <h1>服务推荐系统</h1>
    	</div>
   		<div class="search">
        <form action="./Search" method="post">
            <label for="search"></label>
            <input class="input" type="text" name="search" id="search" value="" /><input class="btn" type="submit" value="搜索" name="submit" />
        </form>
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