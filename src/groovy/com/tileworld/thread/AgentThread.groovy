package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.representation.Environment

/**
 * Created by radu on 21/05/15.
 */
class AgentThread implements Runnable {

    Environment environment;
    TileWorldService tileWorldService;

    public AgentThread(Environment environment, TileWorldService tileWorldService, String agentName) {
        this.environment = environment;
        this.tileWorldService = tileWorldService;
        new Thread(this, agentName).start();
        tileWorldService.updateConsole("${agentName}: started.")
    }

    @Override
    void run() {

    }
}
