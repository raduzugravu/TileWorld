package com.tileworld

import com.tileworld.exceptions.ConfigurationException
import grails.converters.JSON

class HomeController {

    def tileWorldService

    def index() {

    }

    def start() {

        Environment environment;

        try {

            // upload configuration file
            def configFile = request.getFile("tileWorldConfigFile");
            if(!configFile) {
                flash.message = "Before starting TileWorld you have to upload world configuration file"
                redirect([action: "index"]);
            }

            // Initialise TileWorld game based on loaded configuration file
            String configuration = configFile?.inputStream?.text;

            String environmentJSON = tileWorldService.initialise(configuration) as JSON;
            return [environment: environmentJSON];

        } catch (ConfigurationException e) {
            flash.message = "Error initialising TileWorld. Please make sure your configuration file is correct.";
        }

        redirect([action: "index"]);
    }
}
