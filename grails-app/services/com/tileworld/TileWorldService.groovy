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
import com.tileworld.thread.GeneratorThread
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

            Boolean dynamic = false;
            Environment environment = new Environment();

            List<String> configurationVariables = new ArrayList<String>();
            configuration.readLines().each { String line ->
                line.replaceAll("\\s", " ");
                configurationVariables.addAll(line.split(" "));
            }

            // environment general variables
            environment.numberOfAgents = Integer.parseInt(configurationVariables[0]);
            environment.tickTime = Long.parseLong(configurationVariables[1]);
            environment.totalTime = Long.parseLong(configurationVariables[2]);
            environment.remainingTime = environment.totalTime;
            environment.gridWidth = Integer.parseInt(configurationVariables[3]);
            environment.gridHeight = Integer.parseInt(configurationVariables[4]);

            int k = 5; // iterate through all variables

            // agents color and name
            for(int i = 0; i < environment.numberOfAgents; i++) {
                Agent agent = new Agent(color: configurationVariables[k], name: configurationVariables[k]);
                environment.agents.add(agent);
                k++;
            }

            // decide main agent, used to intermediate the negotiation
            environment.agents.get(0).principal = true;

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
                dynamic = true;
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

            if(!dynamic) {

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
            }

            environment.initialiseMap();

            return environment;

        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigurationException(e.getMessage());
        }

    }

    /**
     * Start agent threads, environment thread and generator thread.
     * @param environment
     */
    def initialise(Environment environment) {

        Thread.sleep(5000);

        Ticker ticker = new Ticker(environment, this);

        // message boxes
        MessageBox[] agentsMessageBox = new MessageBox[environment.numberOfAgents];
        for(int i = 0; i < agentsMessageBox.size(); i++) {
            agentsMessageBox[i] = new MessageBox(environment.agents.get(i).name, environment.numberOfAgents, this);
        }
        MessageBox environmentMessageBox = new MessageBox("environment", environment.numberOfAgents, this);

        // create agents and start them
        List<AgentThread> agentThreads = new ArrayList<AgentThread>();
        for(int i = 0; i < environment.agents.size(); i++) {
            AgentThread agent = new AgentThread(agentsMessageBox, environmentMessageBox, environment, ticker, this, environment.agents.get(i).name);
            if(i == 0) agent.markAsPrincipal();
            agentThreads.add(agent);
            agent.start();
        }

        // random generation of tiles
        if(environment.generator) {
            GeneratorThread generatorThread = new GeneratorThread(environment, ticker, this);
            generatorThread.start()
        }

        // start environment thread - this thread deals with agent communication and makes changes to the environment
        EnvironmentThread environmentThread = new EnvironmentThread(agentsMessageBox, environmentMessageBox, environment, ticker, this);
        environmentThread.start()

        // events to update interface
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

    def endGame(String message) {
        System.out.println(System.currentTimeMillis() + " -> " + message);
        def data = [message: message]
        event(key: "endGame", for: 'browser', data: data)
    }
}
