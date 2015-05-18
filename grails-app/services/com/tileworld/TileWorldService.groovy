package com.tileworld

import com.tileworld.exceptions.ConfigurationException
import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class TileWorldService {

    Environment initialise(String configuration) throws ConfigurationException {

        try {

            Environment environment = new Environment();

            List<String> configurationVariables = new ArrayList<String>();
            configuration.readLines().each { String line ->
                line.replaceAll("\\s", " ");
                configurationVariables.addAll(line.split(" "));
            }

            // environment general variables
            environment.numberOfAgents = Integer.parseInt(configurationVariables[0]);
            environment.tickTime = Integer.parseInt(configurationVariables[1]);
            environment.totalTime = Integer.parseInt(configurationVariables[2]);
            environment.gridWidth = Integer.parseInt(configurationVariables[3]);
            environment.gridHeight = Integer.parseInt(configurationVariables[4]);

            int k = 5; // iterate through all variables

            // agents color
            for(int i = 0; i < environment.numberOfAgents; i++) {
                Agent agent = new Agent(color: configurationVariables[k]);
                environment.agents.add(agent);
                k++;
            }

            // agents position
            int agent = 0;
            for(int i = 0; i < environment.numberOfAgents; i++) {
                environment.agents.get(agent).xPosition = Integer.parseInt(configurationVariables[k]);
                environment.agents.get(agent).yPosition = Integer.parseInt(configurationVariables[k+1]);
                k += 2;
                agent++;
            }

            // obstacles
            if(!"obstacles".equalsIgnoreCase(configurationVariables[k])) {
                throw new ConfigurationException("");
            } else {
                k++;
                while(!("tiles".equalsIgnoreCase(configurationVariables[k]) || "generator".equalsIgnoreCase(configurationVariables[k]))) {
                    Obstacle obstacle = new Obstacle(xPosition: Integer.parseInt(configurationVariables[k]),
                            yPosition: Integer.parseInt(configurationVariables[k+1]));
                    environment.obstacles.add(obstacle);
                    k += 2;
                }
            }

            // generator - bonus
            if("generator".equalsIgnoreCase(configurationVariables[k])) {

                Generator generator = new Generator();
                generator.generatorStartTime = Integer.parseInt(configurationVariables[k+1]);
                generator.generatorEndTime = Integer.parseInt(configurationVariables[k+2]);
                generator.generatorMinLifetime = Integer.parseInt(configurationVariables[k+3]);
                generator.generatorMaxLifetime = Integer.parseInt(configurationVariables[k+4]);
                environment.generator = generator;
                k += 4;

            // tiles
            } else if("tiles".equalsIgnoreCase(configurationVariables[k])) {

                k++;
                while(!"holes".equalsIgnoreCase(configurationVariables[k])) {
                    Tile tile = new Tile();
                    tile.numberOfTiles = Integer.parseInt(configurationVariables[k]);
                    tile.color = configurationVariables[k+1];
                    tile.xPosition = Integer.parseInt(configurationVariables[k+2]);
                    tile.yPosition = Integer.parseInt(configurationVariables[k+3]);
                    environment.tiles.add(tile);
                    k += 4;
                }

            } else {
                throw new ConfigurationException("");
            }

            // holes
            if(!"holes".equalsIgnoreCase(configurationVariables[k])) {
                throw new ConfigurationException("");
            } else {
                k++;
                for(int i = k; i < configurationVariables.size(); i++) {
                    Hole hole = new Hole();
                    hole.depth = Integer.parseInt(configurationVariables[i]);
                    hole.color = configurationVariables[i+1];
                    hole.xPosition = Integer.parseInt(configurationVariables[i+2]);
                    hole.yPosition = Integer.parseInt(configurationVariables[i+3]);
                    environment.holes.add(hole);
                    i += 3;
                }
            }

            return environment;

        } catch (Exception e) {
            System.out.println(e);
            throw new ConfigurationException(e.getMessage());
        }

    }

    def updateTileWorld() {

        def data = ['message': 'Hello from server!'];

        def jsonData = data as JSON
        System.out.println(jsonData);

        event(key: "drawTileWorld", for: 'browser', data: data);
        //event(topic: "drawTileWorld");

    }

}
