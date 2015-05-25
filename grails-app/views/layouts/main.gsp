<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="TileWorld"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'tileWorld.css')}" type="text/css">

        <script src="<g:resource dir="js" file="jquery-1.8.3.min.js"/>"></script>
        <script src="<g:resource dir="js" file="application.js"/>"></script>

		<g:layoutHead/>
		<r:layoutResources />
	</head>
	<body>

        <div id="grailsLogo" role="banner">
            <a href="${createLink(controller: 'home', action: 'index')}">TileWorld</a>
            <div id="menu">
                <a href="${createLink(controller: 'home', action: 'index')}">Start a new game</a> |
                <a href="${createLink(controller: 'home', action: 'help')}">Help</a>
            </div>
        </div>

        <div id="content">
            <g:layoutBody/>
        </div>

        <div class="footer" role="contentinfo">
            <p>Copyright &copy; 2015 - <span></span></p>
        </div>
        <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>

        <r:layoutResources />

    <script type="text/javascript" language="javascript">
        <!--
        // Email obfuscator script 2.1 by Tim Williams, University of Arizona
        // Random encryption key feature by Andrew Moulden, Site Engineering Ltd
        // This code is freeware provided these four comment lines remain intact
        // A wizard to generate this code is at http://www.jottings.com/obfuscator/
        { coded = "0ZLqaqH0Zwqsd@HAZJC.4nA"
            key = "9djqnhZTOvCE5HYc8rtFRKzsLkSM1fm7w2WXUi4e0puoIaVyDl6GgA3BQxbPJN"
            shift=coded.length
            link=""
            for (i=0; i<coded.length; i++) {
                if (key.indexOf(coded.charAt(i))==-1) {
                    ltr = coded.charAt(i)
                    link += (ltr)
                }
                else {
                    ltr = (key.indexOf(coded.charAt(i))-shift+key.length) % key.length
                    link += (key.charAt(ltr))
                }
            }
            $(".footer > p > span").html("<a href='mailto:"+link+"'>Radu-Stefan Zugravu</a>")
        }
        //-->
    </script><noscript>Sorry, you need Javascript on to email me.</noscript>


    </body>
</html>
