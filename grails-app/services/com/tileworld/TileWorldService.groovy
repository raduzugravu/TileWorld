package com.tileworld

import com.tileworld.communication.MessageBox
import com.tileworld.exceptions.ConfigurationException
import com.tileworld.representation.Agent
import com.tileworld.representation.Environment
import com.tileworld.representation.Generator
import com.tileworld.representation.Hole
import com.tileworld.representation.Obstacle
import com.tileworld.representation.Tile
import com.tileworld.thread.AgentThread

import com.tileworld.thread.EnvironmentThread

import com.tileworld.thread.Ticker
import grails.transaction.Transactional

@Transactional
class TileWorldService {

    /**
     * Transform user configuration input into internal configuration.
     * @param configuration - user configuration input
     * @return environment - internal environment configuration
     */
    Environment getConfiguration(String configuration) throws ConfigurationException {

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
                MessageBox messageBox = new MessageBox();
                Agent agent = new Agent(color: configurationVariables[k], name: "agent${i}", messageBox: messageBox);
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

            environment.map = initialiseMap(environment);

            // messageBox used to communicate between agents and environment
            environment.messageBox = new MessageBox();

            return environment;

        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigurationException(e.getMessage());
        }

    }

    /**
     * Mark each cell with a character corresponding to the entity currently in that cell (agents, tiles, holes, obstacles).
     * This will ease the process of finding out if an adjacent cell is free or not.
     * @param environment
     * @return map - a matrix marking all cells that are not empty in this tile world game.
     */
    private def initialiseMap(Environment environment) {

        def map = [[]];

        for(int i = 0; i < environment.gridWidth; i++) {
            def row = [];
            for(int j = 0; j < environment.gridHeight; j++) {
                row.add("E");
            }
            map.add(row);
        }

        // mark agents
        for(int i = 0; i < environment.agents.size(); i++) {
            map[environment.agents.get(i).xPosition][environment.agents.get(i).yPosition] = "A";
        }

        // mark tiles
        for(int i = 0; i < environment.tiles.size(); i++) {
            map[environment.tiles.get(i).xPosition][environment.tiles.get(i).yPosition] = "T";
        }

        // mark holes
        for(int i = 0; i < environment.holes.size(); i++) {
            map[environment.holes.get(i).xPosition][environment.holes.get(i).yPosition] = "H";
        }

        // mark obstacles
        for(int i = 0; i < environment.obstacles.size(); i++) {
            map[environment.obstacles.get(i).xPosition][environment.obstacles.get(i).yPosition] = "O";
        }

        return map;
    }

    /**
     * Start agent threads and environment thread.
     * @param environment
     */
    def initialise(Environment environment) {

        Thread.sleep(1000);

        Ticker ticker = new Ticker(environment, this);

        // create agents and start them
        List<AgentThread> agentThreads = new ArrayList<AgentThread>();
        for(int i = 0; i < environment.agents.size(); i++) {
            AgentThread agent = new AgentThread(environment, ticker, this, environment.agents.get(i).name);
            agentThreads.add(agent);
            agent.start();
        }

        Thread.sleep(5000);

        EnvironmentThread environmentThread = new EnvironmentThread(environment, ticker, this);
        environmentThread.start()

        updateTileWorld(environment);
        updateConsole("TileWorld game started.");

    }

    // send event to update tile world environment
    def updateTileWorld(def environment) {
        event(key: "drawTileWorld", for: 'browser', data: environment);
    }

    def updateConsole(String message) {
        System.out.println(System.currentTimeMillis() + " -> " + message);
        def data = [message: message]
        event(key: "updateConsole", for: 'browser', data: data)
    }

}
