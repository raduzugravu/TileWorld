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

    @Override
    void run() {

        while(environment.totalTime > 0) {

            long startTime = System.currentTimeMillis() % 1000;
            ticker.tick(this.name);
            long endTime = System.currentTimeMillis() % 1000 - startTime;

            Thread.sleep(environment.tickTime - endTime);
            environment.totalTime -= environment.tickTime
        }

        tileWorldService.updateConsole(this.name + ": ended.")
        ticker.end();
    }
}
