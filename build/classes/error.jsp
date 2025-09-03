<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>错误页面</title>
    <style type="text/css">
        body {
            font-family: Arial, sans-serif;
            margin: 40px;
            background-color: #f8f8f8;
        }
        .error-container {
            background-color: white;
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 20px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        .error-title {
            color: #d9534f;
            margin-top: 0;
        }
        .error-message {
            color: #333;
            font-size: 16px;
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
    <div class="error-container">
        <h1 class="error-title">系统错误</h1>
        <p class="error-message">
            <% 
                String errorMessage = (String) request.getAttribute("error");
                if (errorMessage != null) {
                    out.println(errorMessage);
                } else {
                    out.println("系统发生未知错误，请稍后重试。");
                }
            %>
        </p>
        <div class="back-link">
            <a href="index.jsp">返回首页</a>
        </div>
    </div>
</body>
</html>