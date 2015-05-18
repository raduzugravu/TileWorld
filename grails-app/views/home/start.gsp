<%--
  Created by IntelliJ IDEA.
  User: radu
  Date: 16/05/15
  Time: 00:47
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <title>TileWorld - Watch your world!</title>
    <meta name="layout" content="main"/>

    <script src="<g:resource plugin="events-push" dir="js/atmosphere" file="atmosphere.js"/>"></script>
    <script src="<g:resource plugin="events-push" dir="js/atmosphere" file="jquery.atmosphere.js"/>"></script>
    <script src="<g:resource plugin="events-push" dir="js/grails" file="grailsEvents.js"/>"></script>

    %{--<r:require module="grailsEvents"/>--}%
    <script>
        var tileWorldEvents = new grails.Events("http://localhost:8080/");
        tileWorldEvents.on('drawTileWorld', function(data) {
            console.log("Received test message: " + data);
        });
    </script>

</head>

<body>

</body>

</html>