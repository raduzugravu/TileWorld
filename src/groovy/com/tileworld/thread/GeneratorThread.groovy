package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.representation.Agent
import com.tileworld.representation.Environment
import com.tileworld.representation.Hole
import com.tileworld.representation.Tile

/**
 * Created by radu on 21/05/15.
 */
class GeneratorThread extends Thread {

    Environment environment;
    Ticker ticker;
    TileWorldService tileWorldService;
    long totalTime = 0;

    public GeneratorThread(Environment environment, Ticker ticker, TileWorldService tileWorldService) {
        this.name = "generator";
        this.environment = environment;
        this.ticker = ticker;
        this.tileWorldService = tileWorldService;
        tileWorldService.updateConsole("generator: started.")
    }

    /**
     * EnvironmentThread announces all agents when they should do their move through a tick event.
     * After the tick event is issued EnvironmentThread waits for all Agents to send him a message with their move.
     * The tick event is repeated until total time ends.
     */
    @Override
    void run() {

        String[] colors = new String[environment.numberOfAgents];
        environment.agents.eachWithIndex { Agent agent, i -> colors[i] = agent.color; }

        while(ticker.running()) {

            long nextRandomTime = getNextRandomTime();
            tileWorldService.updateConsole("${this.name}: Next group random generation in ${nextRandomTime}.")
            sleep(nextRandomTime)

            addRandomGroup(colors);
            tileWorldService.updateConsole("${this.name}: Random group generated.")

            totalTime += nextRandomTime;
            System.out.println("generator: " + totalTime);
            if(totalTime > environment.totalTime) { break; }
        }

        System.out.println(this.name + ": ended.")
    }

    private long getNextRandomTime() {
        long generatorStartTime = environment.generator.generatorStartTime;
        long generatorEndTime = environment.generator.generatorEndTime;
        Random r = new Random()
        long nextRandomTime = generatorStartTime + ((long)(r.nextDouble() * (generatorEndTime - generatorStartTime)));

        return nextRandomTime;
    }

    private long getRandomLifetime() {
        long generatorStartTime = environment.generator.generatorMinLifetime;
        long generatorEndTime = environment.generator.generatorMaxLifetime;
        Random r = new Random()
        long nextRandomTime = generatorStartTime + ((long)(r.nextDouble() * (generatorEndTime - generatorStartTime)));

        return nextRandomTime;
    }

    private void addRandomGroup(String[] colors) {

        if(isEnoughSpace(2)) {

            long lifetime = getRandomLifetime();

            Random rand = new Random();
            int depth = rand.nextInt(5) + 1;
            String color = colors[rand.nextInt(colors.size())];

            def position = getRandomPosition();
            Hole hole = new Hole(color: color, depth: depth, xPosition: position.x, yPosition: position.y, lifetime: lifetime);
            environment.holes.add(hole);

            position = getRandomPosition();
            Tile tile = new Tile(numberOfTiles: depth, color: color, xPosition: position.x, yPosition: position.y, lifetime: lifetime);
            environment.tiles.add(tile);

            environment.initialiseMap();
            tileWorldService.updateTileWorld(environment);
        }

    }

    private def getRandomPosition() {
        while(true) {
            Random rand = new Random();
            int x = rand.nextInt(environment.gridHeight);
            int y = rand.nextInt(environment.gridWidth);
            if(environment.map[x][y] == 'E')
                return [x:x, y:y];
        }
    }

    private Boolean isEnoughSpace(space) {
        int availableSpace = 0;
        for(int i = 0; i < environment.map.size(); i++)
            for(int j = 0; j < environment.map[i].size(); j++)
                if(environment.map[i][j] == 'E')
                    availableSpace++;

        if(availableSpace >= space)
            return true;

        return false;
    }
}
