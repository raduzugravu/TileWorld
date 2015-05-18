package com.tileworld

import com.tileworld.exceptions.ConfigurationException
import grails.web.JSONBuilder

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

            environment = tileWorldService.initialise(configuration);

            flash.message = "TileWorld configuration file uploaded successfully. Your game will start soon. Please wait.."

        } catch (ConfigurationException e) {
            flash.message = "Error initialising TileWorld. Please make sure your configuration file is correct.";
            redirect([action: "index"]);
        }

        [environment: environment];

    }
}
