package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.communication.Message
import com.tileworld.communication.MessageBox
import com.tileworld.representation.Environment

/**
 * Created by radu on 21/05/15.
 */
class EnvironmentThread extends Thread {

    MessageBox[] agentsMessageBox;
    MessageBox environmentMessageBox;
    Environment environment;
    Ticker ticker;
    TileWorldService tileWorldService;

    public EnvironmentThread(MessageBox[] agentsMessageBox, MessageBox environmentMessageBox, Environment environment,
                             Ticker ticker, TileWorldService tileWorldService) {
        this.agentsMessageBox = agentsMessageBox;
        this.environmentMessageBox = environmentMessageBox;
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

        while(environment.remainingTime > 0) {

            println("----- environment: Before starting: map:\n${environment.getMap()}")

            if(!environment.generator && environment.isEmpty()) break;

            long startTime = System.currentTimeMillis() % 1000;
            ticker.tick(this.name);
            environmentMessageBox.checkMessageList(this.name);
            processMessageList();
            tileWorldService.updateTileWorld(environment);
            long endTime = System.currentTimeMillis() % 1000 - startTime;

            // if everything ended faster, wait until next tick time
            if(environment.tickTime - endTime > 0)
                sleep(environment.tickTime - endTime);
            environment.remainingTime -= environment.tickTime;

            System.out.println("EnvironmentThread.run(): Time to end: ${environment.totalTime}");
        }

        System.out.println(this.name + ": ended.")
        ticker.end();
    }

    /**
     *
     * @param tileWorldService
     * @return
     */
    private void processMessageList() {
        environmentMessageBox.messageList.each { Message message ->
            tileWorldService.updateConsole("environment: ${message.toString()}");
            processMessage(message);
        }

        environmentMessageBox.emptyMessageList(this.name);
    }

    /**
     *
     * @param message
     */
    private void processMessage(Message message) {

        try {

            Message confirmation = new Message(sender: "environment", inReplayTo: message.operation.code);
            if(environment.executeOperation(message.sender, message.operation)) {
                // send success message
                confirmation.successCode = "SUCCESS";
            } else {
                // send error message
                confirmation.successCode = "ERROR";
            }

            for(int i = 0; i < environment.numberOfAgents; i++) {
                if(message.sender.equalsIgnoreCase(agentsMessageBox[i].getOwner())) {
                    agentsMessageBox[i].addStatusMessage(confirmation);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
