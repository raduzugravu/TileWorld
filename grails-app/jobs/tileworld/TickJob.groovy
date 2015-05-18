package tileworld

import grails.converters.JSON

class TickJob {

    def tileWorldService;

    static triggers = {
      simple repeatInterval: 30000l // execute job once in 30 seconds
    }

    def execute() {

        System.out.println("Updating TileWorld game interface - START");
        tileWorldService.updateTileWorld();
        System.out.println("Updating TileWorld game interface - END");

    }
}
