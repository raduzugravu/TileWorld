package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.representation.Environment

/**
 * Created by radu on 21/05/15.
 */
class EnvironmentThread extends Thread {

    Environment environment;
    Ticker ticker;
    TileWorldService tileWorldService;

    public EnvironmentThread(Environment environment, Ticker ticker, TileWorldService tileWorldService) {
        this.name = "environment";
        this.environment = environment;
        this.ticker = ticker;
        this.tileWorldService = tileWorldService;
        tileWorldService.updateConsole("environment: started.")
    }

    /**
     * EnvironmentThread announces all agents when they should do their move through a tick event.
     * After the tick event is issued EnvironmentThread waits for all Agents to send him a message with their move.
     * The tick event is repeated until total time ends.
     */
    @Override
    void run() {

        while(environment.totalTime > 0) {

            long startTime = System.currentTimeMillis() % 1000;
            ticker.tick(this.name);
            environment.messageBox.checkMessageList(environment.numberOfAgents);
            environment.processMessageList(tileWorldService);
            tileWorldService.updateTileWorld(environment);
            long endTime = System.currentTimeMillis() % 1000 - startTime;

            // if everything ended faster, wait until next tick time
            Thread.sleep(environment.tickTime - endTime);
            environment.totalTime -= environment.tickTime

            System.out.println("EnvironmentThread.run(): Time to end: ${environment.totalTime}");
        }

        System.out.println(this.name + ": ended.")
        ticker.end();
    }
}
