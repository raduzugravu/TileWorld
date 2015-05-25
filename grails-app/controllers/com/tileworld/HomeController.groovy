package com.tileworld

import com.tileworld.exceptions.ConfigurationException
import com.tileworld.representation.Environment
import grails.converters.JSON

class HomeController {

    def tileWorldService

    def index() {

    }

    def start() {

        try {

            System.out.println("PARAMS: " + params);

            // read uploaded configuration
            def configFile = request.getFile("tileWorldConfigFile");
            String configuration = configFile?.inputStream?.text;
            if(!configuration) {
                if (params.tileWorldConfigInput) {
                    configuration = params.tileWorldConfigInput;
                } else {
                    configuration = new File('system.txt').text;

                }
            }

            System.out.println("CONFIGURATION: " + configuration);

            // get internal representation of TileWorld game configuration based on user configuration input and initialise environment
            Environment environment = tileWorldService.getConfiguration(configuration);
            runAsync {
                tileWorldService.initialise(environment)
            }

            flash.message = "Please wait. We are initialising your TileWorld environment..";
            String environmentJSON = environment as JSON;
            return [environment: environmentJSON];

        } catch (ConfigurationException e) {
            flash.message = "Error initialising TileWorld. Please make sure your configuration file is correct.";
        } catch (FileNotFoundException e) {
            flash.message = "Your system.txt default configuration file was not found.";
        } catch (Exception e) {
            flash.message = e.getMessage();
            e.printStackTrace();
        }

        redirect([action: "index"]);
    }
}
