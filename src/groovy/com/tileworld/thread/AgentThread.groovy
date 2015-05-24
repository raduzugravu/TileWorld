package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.communication.Message
import com.tileworld.communication.Operation
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

            // got action tick - make your move
            makeRandomMove();
            waitForAnswer();
            processMessageList();
        }

        tileWorldService.updateConsole(this.name + ": ended.")
    }

    private def getPosition() {
        for(int i = 0; i < environment.agents.size(); i++) {
            if(this.name.equalsIgnoreCase(environment.agents.get(i).name)) {
                return [x: environment.agents.get(i).xPosition, y: environment.agents.get(i).yPosition]
            }
        }
    }

    private void makeRandomMove() {

        System.out.println("makeRandomMove(): ${this.name}.")

        def position = getPosition();

        // check left
        if(environment.isEmpty([x: position.x, y: position.y-1])) {
            move([x: position.x, y: position.y-1]);
        }

        // check right
        if(environment.isEmpty([x: position.x, y: position.y+1])) {
            move([x: position.x, y: position.y+1]);
        }

        // check up
        if(environment.isEmpty([x: position.x-1, y: position.y])) {
            move([x: position.x-1, y: position.y]);
        }

        // check down
        if(environment.isEmpty([x: position.x+1, y: position.y])) {
            move([x: position.x+1, y: position.y]);
        }
    }

    private Boolean waitForAnswer() {

        for(int i = 0; i < environment.numberOfAgents; i++) {
            if(this.name.equalsIgnoreCase(environment.agents.get(i).name)) {
                environment.agents.get(i).messageBox.checkMessageList(1);
            }
        }

        return true;
    }

    private void processMessageList() {
        for(int i = 0; i < environment.numberOfAgents; i++) {
            if (this.name.equalsIgnoreCase(environment.agents.get(i).name)) {
                environment.agents.get(i).messageBox.messageList.get(0).each {
                    tileWorldService.updateConsole(it.toString());
                }
                environment.agents.get(i).messageBox.messageList.clear();
                break;
            }
        }
    }

    /**
     * Notify environment thread that your intention is to move to another adjacent cell
     * @param newPosition - position the agent wants to get to
     */
    private void move(def newPosition) {
        System.out.println("${this.name}: move=${newPosition}.")
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "MOVE", position: newPosition);
        message.operation = operation;
        environment.messageBox.addMessage(message);
    }

}
