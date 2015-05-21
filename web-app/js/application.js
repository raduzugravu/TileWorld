if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}

/**
 * This method is called each time changes appear in tile world.
 * The interface is redrawn based on data object sent as JSON.
 * @param data - Environment object sent as JSON containing actual situation of the world.
 */
function drawTileWorld(data) {
    if (typeof jQuery !== 'undefined') {
        (function($) {

            $(document).ready(function() {

                console.log(data);

                var tileWorld = '';

                // compute box width based on body's max width and font-size as percent of box width
                var boxSize = (940-2*data.gridWidth)/data.gridWidth;
                var fontSize = (80*boxSize/100)+"px";

                // draw grid
                for(var i=0; i < data.gridHeight; i++) {
                    tileWorld += '<div class="row">';
                    for(var j=0; j < data.gridWidth; j++) {
                        tileWorld += '<div style="width:'+boxSize+'px; height:'+boxSize+'px;" id="'+i+j+'" class="box"></div>';
                    }
                    tileWorld += "</div>";
                }
                $('#tileWorld').html(tileWorld);

                // mark obstacles
                for(var i=0; i < data.obstacles.length; i++) {
                    var boxId = "#"+data.obstacles[i].xPosition+data.obstacles[i].yPosition;
                    $(""+boxId).addClass("obstacle");
                }

                // mark holes
                for(var i=0; i < data.holes.length; i++) {
                    var boxId = "#"+data.holes[i].xPosition+data.holes[i].yPosition;
                    $(""+boxId).html('<div class="hole" style="width:'+boxSize+'px; height:'+boxSize+'px;"></div>');
                    $(""+boxId+" > .hole").css("background-color",data.holes[i].color);
                    $(""+boxId+" > .hole").html(data.holes[i].depth);
                }
                $(".hole").css("font-size", fontSize);

                // mark tiles
                var tilesMaxSize = boxSize/2;
                for(var i=0; i < data.tiles.length; i++) {
                    var tileSize = tilesMaxSize/data.tiles[i].numberOfTiles;
                    var boxId = "#"+data.tiles[i].xPosition+data.tiles[i].yPosition;
                    var tiles = '';
                    for(var j = 0; j < data.tiles[i].numberOfTiles; j++) {
                        tiles += '<div class="tile" style="width:'+tileSize+'px; height:'+tileSize+'px; background-color:'+data.tiles[i].color+'"></div>'
                    }
                    $(""+boxId).html(tiles);
                }

                // mark agents
                for(var i=0; i < data.agents.length; i++) {
                    var boxId = "#"+data.agents[i].xPosition+data.agents[i].yPosition;
                    $(""+boxId).addClass("agent");
                    $(""+boxId).html("A");
                    $(""+boxId).css("color", data.agents[i].color);
                }
                $(".agent").css("font-size", fontSize);
            });

        })(jQuery);
    }
}

function updateConsole(data) {
    if (typeof jQuery !== 'undefined') {
        (function($) {

            $(document).ready(function() {
                var console = $("#console");
                console.html(console.html() + data.message + "<br/>");
            });

        })(jQuery);
    }
}
