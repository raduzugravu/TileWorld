package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.communication.Operation
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

            environment.executeOperation("generator", new Operation(code: "ADD"))
            tileWorldService.updateTileWorld(environment);

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
}
