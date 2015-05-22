package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.representation.Environment

/**
 * Created by radu on 21/05/15.
 */
class AgentThread extends Thread {

    Environment environment;
    Ticker ticker;
    TileWorldService tileWorldService;

    public AgentThread(Environment environment, Ticker ticker, TileWorldService tileWorldService, String agentName) {
        this.name = agentName;
        this.environment = environment;
        this.ticker = ticker;
        this.tileWorldService = tileWorldService;
        tileWorldService.updateConsole("${agentName}: started.")
    }

    @Override
    void run() {

        while(ticker.running()) {
            ticker.action(this.name);
        }

        tileWorldService.updateConsole(this.name + ": ended.")
    }
}
