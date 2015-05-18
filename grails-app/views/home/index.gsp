<%--
  Created by IntelliJ IDEA.
  User: radu
  Date: 16/05/15
  Time: 00:19
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>TileWorld - Start your own world!</title>
    <meta name="layout" content="main"/>
</head>

<body>

<g:if test="${flash.message}">
    <p class="message error">${flash.message}</p>
</g:if>

<g:uploadForm action="start" name="tileWorldConfiguration">
    <input type="file" name="tileWorldConfigFile" />
    <g:submitButton name="submit" value="Start your World"/>
</g:uploadForm>

</body>
</html>