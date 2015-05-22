package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.representation.Environment

/**
 * Created by radu on 22/05/15.
 */
class Ticker {

    Environment environment;
    TileWorldService tileWorldService;

    private Boolean ticked = false;
    private Boolean running = true;
    private int agentsCounter = 0;

    public Ticker(Environment environment, TileWorldService tileWorldService) {
        this.environment = environment;
        this.tileWorldService = tileWorldService;
    }

    public void end() {
        running = false;
        tileWorldService.updateConsole("Game ended!")
    }

    public Boolean running() {
        return running;
    }

    public synchronized void tick(String threadName) {
        ticked = true;
        agentsCounter = 0;
        notifyAll();

        tileWorldService.updateConsole(threadName + ": Tick.")
    }

    public synchronized action(String threadName) {

        if(!ticked || (agentsCounter < environment.agents.size())) {
            wait()
        }

        agentsCounter++;
        if(agentsCounter == environment.agents.size()) {
            ticked = false;
        }

        tileWorldService.updateConsole(threadName + ": Action.");
    }

}
