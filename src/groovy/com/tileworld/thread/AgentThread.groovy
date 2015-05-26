package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.communication.Message
import com.tileworld.communication.MessageBox
import com.tileworld.communication.Operation
import com.tileworld.representation.Agent
import com.tileworld.representation.Environment

/**
 * Created by radu on 21/05/15.
 */
class AgentThread extends Thread {

    Environment environment;
    MessageBox[] agentsMessageBox;
    MessageBox environmentMessageBox;
    Ticker ticker;
    TileWorldService tileWorldService;

    public AgentThread(MessageBox[] agentsMessageBox, MessageBox environmentMessageBox, Environment environment,
                       Ticker ticker, TileWorldService tileWorldService, String agentName) {
        this.agentsMessageBox = agentsMessageBox;
        this.environmentMessageBox = environmentMessageBox;
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
            tileWorldService.updateConsole(this.name + ": Action.")

            // got action tick - make your move
            makeRandomMove();
            environmentMessageBox.isMessageListProcessed();
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
        tileWorldService.updateConsole("${this.name}: makeRandomMove(): position=${position}");

        // check left
        if(environment.canMoveTo([x: position.x, y: position.y-1])) {
            move([x: position.x, y: position.y-1]);
            return;
        }

        // check right
        if(environment.canMoveTo([x: position.x, y: position.y+1])) {
            move([x: position.x, y: position.y+1]);
            return;
        }

        // check up
        if(environment.canMoveTo([x: position.x-1, y: position.y])) {
            move([x: position.x-1, y: position.y]);
            return;
        }

        // check down
        if(environment.canMoveTo([x: position.x+1, y: position.y])) {
            move([x: position.x+1, y: position.y]);
            return;
        }
    }

    private synchronized void processMessageList() {
        for(int i = 0; i < environment.numberOfAgents; i++) {
            if (this.name.equalsIgnoreCase(agentsMessageBox[i].getOwner())) {
                agentsMessageBox[i].messageList.each {
                    tileWorldService.updateConsole("${this.name}: ${it.toString()}");
                }
                agentsMessageBox[i].messageList.clear();
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
        environmentMessageBox.addMessage(message);
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
        environmentMessageBox.addMessage(message);
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
        environmentMessageBox.addMessage(message);
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
        environmentMessageBox.addMessage(message);
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
        environmentMessageBox.addMessage(message);
    }

    /* --- END OPERATIONS --- */

}
