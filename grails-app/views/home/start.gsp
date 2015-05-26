<%--
  Created by IntelliJ IDEA.
  User: radu
  Date: 16/05/15
  Time: 00:47
--%>

<html>
<head>

    <title>TileWorld - Watch your world!</title>
    <meta name="layout" content="main"/>

    <script src="<g:resource plugin="events-push" dir="js/atmosphere" file="atmosphere.js"/>"></script>
    <script src="<g:resource plugin="events-push" dir="js/atmosphere" file="jquery.atmosphere.js"/>"></script>
    <script src="<g:resource plugin="events-push" dir="js/grails" file="grailsEvents.js"/>"></script>

    <script>
        drawTileWorld(${raw(environment)});
        var tileWorldEvents = new grails.Events("http://localhost:8080/");
        tileWorldEvents.on('drawTileWorld', function(data) {
            drawTileWorld(data);
        });
        tileWorldEvents.on('updateConsole', function(data) {
            updateConsole(data);
        });
        tileWorldEvents.on('endGame', function(data) {
            alert(data.message);
        });
    </script>

</head>

<body>

<div id="tileWorld">
    <g:if test="${flash.message}">
        <p class="message error">${flash.message}</p>
    </g:if>
</div>

<div id="console">
    <ul></ul>
</div>

</body>

</html>