package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.representation.Environment

/**
 * Created by radu on 21/05/15.
 */
class GeneratorThread extends Thread {

    Environment environment;
    TileWorldService tileWorldService;
    long totalTime = 0;

    public GeneratorThread(Environment environment, TileWorldService tileWorldService) {
        this.name = "generator";
        this.environment = environment;
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

        while(true) {
            long nextRandomTime = getNextRandomTime();
            sleep(nextRandomTime)
            totalTime += nextRandomTime;
            if(totalTime > environment.totalTime) {
                break;
            }

            long lifetime = getRandomLifetime();

            // TODO: generate random tile/holes
        }

        System.out.println(this.name + ": ended.")
    }

    private getNextRandomTime() {
        long generatorStartTime = environment.generator.generatorStartTime;
        long generatorEndTime = environment.generator.generatorEndTime;
        Random r = new Random()
        long nextRandomTime = generatorStartTime + ((long)(r.nextDouble() * (generatorEndTime - generatorStartTime)));

        return nextRandomTime;
    }

    private getRandomLifetime() {
        long generatorStartTime = environment.generator.generatorMinLifetime;
        long generatorEndTime = environment.generator.generatorMaxLifetime;
        Random r = new Random()
        long nextRandomTime = generatorStartTime + ((long)(r.nextDouble() * (generatorEndTime - generatorStartTime)));

        return nextRandomTime;
    }
}
