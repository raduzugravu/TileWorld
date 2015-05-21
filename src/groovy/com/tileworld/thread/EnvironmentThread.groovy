package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.representation.Environment

/**
 * Created by radu on 21/05/15.
 */
class EnvironmentThread implements Runnable{

    Environment environment;
    TileWorldService tileWorldService;

    public EnvironmentThread(Environment environment, TileWorldService tileWorldService) {
        this.environment = environment;
        this.tileWorldService = tileWorldService;
        new Thread(this, "environment").start();
        tileWorldService.updateConsole("environment: started.")
    }

    @Override
    void run() {

        // create agents and start them
        List<AgentThread> agents = new ArrayList<AgentThread>();
        for(int i = 0; i < this.environment.agents.size(); i++) {
            AgentThread agent = new AgentThread(this.environment, this.tileWorldService, this.environment.agents.get(i).name);
            agents.add(agent);
        }

    }
}
