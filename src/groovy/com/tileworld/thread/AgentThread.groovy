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
            environment.messageBox.isMessageListProcessed();
            processMessageList();
        }

        System.out.println(this.name + ": ended.")
    }

    private def getPosition() {
        for(int i = 0; i < environment.agents.size(); i++) {
            if(this.name.equalsIgnoreCase(environment.agents.get(i).name)) {
                return [x: environment.agents.get(i).xPosition, y: environment.agents.get(i).yPosition]
            }
        }
    }

    private void makeRandomMove() {

        def position = getPosition();

        // check left
        if(environment.isEmpty([x: position.x, y: position.y-1])) {
            move([x: position.x, y: position.y-1]);
            return;
        }

        // check right
        if(environment.isEmpty([x: position.x, y: position.y+1])) {
            move([x: position.x, y: position.y+1]);
            return;
        }

        // check up
        if(environment.isEmpty([x: position.x-1, y: position.y])) {
            move([x: position.x-1, y: position.y]);
            return;
        }

        // check down
        if(environment.isEmpty([x: position.x+1, y: position.y])) {
            move([x: position.x+1, y: position.y]);
            return;
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
                environment.agents.get(i).messageBox.messageList.each {
                    tileWorldService.updateConsole("${this.name}: ${it.toString()}");
                }
                environment.agents.get(i).messageBox.messageList.clear();
                break;
            }
        }
    }

    /* --- OPERATIONS --- */

    /**
     * Notify environment thread that your intention is to move to another adjacent cell
     * @param newPosition - position the agent wants to get to
     */
    private void move(def newPosition) {
        System.out.println("move(): ${this.name}: position=${newPosition}.")
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "MOVE", position: newPosition);
        message.operation = operation;
        environment.messageBox.addMessage(message);
    }

    /**
     * Notify environment thread that your intention is to pick the tile in the current cell.
     * @param newPosition - position the agent wants to get to
     */
    private void pickTile(def position) {
        System.out.println("pickTile(): ${this.name}: position=${position}.")
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "PICK", position: position);
        message.operation = operation;
        environment.messageBox.addMessage(message);
    }

    /**
     * Notify environment thread that your intention is to drop the tile in the current cell.
     * @param newPosition - position the agent wants to get to
     */
    private void dropTile(def position) {
        System.out.println("dropTile(): ${this.name}: position=${position}.");
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "DROP", position: position);
        message.operation = operation;
        environment.messageBox.addMessage(message);
    }

    /**
     * Notify environment thread that your intention is to use the tile to cover an adjacent hole.
     * @param direction - LEFT, RIGHT, UP, DOWN
     */
    private void useTile(String direction) {
        System.out.println("useTile(): ${this.name}: direction=${direction}.");
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "USE", direction: direction);
        message.operation = operation;
        environment.messageBox.addMessage(message);
    }

    /**
     * Notify environment thread that your intention is to transfer points to another thread
     * @param toAgent - agent you are transferring points to.
     * @param transferPoints - number of points to transfer
     */
    private void transferPoints(String toAgent, Integer transferPoints) {
        System.out.println("transferPoints(): ${this.name}: toAgent=${toAgent}; transferPoints=${transferPoints}.")
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "TRANSFER", toAgent: toAgent, transferPoints: transferPoints);
        message.operation = operation;
        environment.messageBox.addMessage(message);
    }

    /* --- END OPERATIONS --- */

}
