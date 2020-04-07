<%--
  Created by IntelliJ IDEA.
  User: stud
  Date: 2019/7/12
  Time: 10:21
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<html>
<head>
    <title>Title</title>
    <link rel="stylesheet" href="static/layui/css/layui.css">
    <script src="static/layui/layui.js"></script>
</head>
<body>
<div class="layui-container">
    <table class="layui-table">
        <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>性别</th>
            <th>电话</th>
            <th>邮箱</th>
            <th>操作</th>
        </tr>
        <c:forEach items="${list}" var="t">
            <tr>
                <td>${t.id}</td>
                <td>${t.username}</td>
                <td>${t.sex}</td>
                <td>${t.phone}</td>
                <td>${t.email}</td>
                <td>
                    <a href="userDelete${t.id}.do"
                       class="layui-btn layui-btn-danger">删除</a>

                    <a class="layui-btn layui-btn-warm" href="userEdit.do?id=${t.id}">修改</a>

                </td>
            </tr>
        </c:forEach>
    </table>
</div>
</body>
</html>
