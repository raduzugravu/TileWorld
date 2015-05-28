package com.tileworld.thread

import com.tileworld.TileWorldService
import com.tileworld.communication.Message
import com.tileworld.communication.MessageBox
import com.tileworld.communication.Operation
import com.tileworld.helper.Distance
import com.tileworld.representation.Agent
import com.tileworld.representation.Environment

/**
 * Created by radu on 21/05/15.
 */
class AgentThread extends Thread {

    Environment environment;
    MessageBox[] agentsMessageBox;
    MessageBox environmentMessageBox;
    MessageBox negotiationMessageBox;
    Ticker ticker;
    TileWorldService tileWorldService;

    public AgentThread(MessageBox[] agentsMessageBox, MessageBox environmentMessageBox, MessageBox negotiationMessageBox,
                       Environment environment, Ticker ticker, TileWorldService tileWorldService, String agentName) {
        this.agentsMessageBox = agentsMessageBox;
        this.environmentMessageBox = environmentMessageBox;
        this.negotiationMessageBox = negotiationMessageBox;
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
            def distances = computeDistances(getPosition());

            Message negotiationMessage = negotiate(distances);
            notifyPrincipal(negotiationMessage);
            negotiationMessageBox.checkNegotiationMessageList(this.name); // wait for all agents to send distances
            negotiationMessageBox.isMessageListProcessed();

            processMessageList();

            environmentMessageBox.checkMessageList(this.name);
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

    private synchronized void processMessageList() {

        for(int i = 0; i < environment.numberOfAgents; i++) {
            if (this.name.equalsIgnoreCase(agentsMessageBox[i].getOwner())) {
                agentsMessageBox[i].messageList.each {

                    tileWorldService.updateConsole("${this.name}: ${it}");

                    switch(it?.operation?.code) {
                        case "MOVE":
                            def message = move(it.operation.position);
                            notifyEnvironment(message);
                            break;
                        case "PICK":
                            def message = pickTile(it.operation.position);
                            notifyEnvironment(message);
                            break;
                        case "DROP":
                            def message = dropTile(it.operation.position);
                            notifyEnvironment(message);
                            break;
                        case "USE":
                            def message = useTile(it.operation.direction);
                            notifyEnvironment(message);
                            break;
                        case "TRANSFER":
                            def message = transferPoints(it.operation.toAgent, it.operation.transferPoints);
                            notifyEnvironment(message);
                            break;
                    }
                }
                agentsMessageBox[i].messageList.clear();
                break;
            }
        }
    }

    /* --- OPERATIONS --- */

    /**
     * Message to move to another adjacent cell.
     * @param newPosition - position the agent wants to get to
     */
    private Message move(def newPosition) {
        System.out.println("move(): ${this.name}: position=${newPosition}.")
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "MOVE", position: newPosition);
        message.operation = operation;
        return message;
    }

    /**
     * Message to pick tile from current position.
     * @param newPosition
     */
    private Message pickTile(def position) {
        System.out.println("pickTile(): ${this.name}: position=${position}.")
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "PICK", position: position);
        message.operation = operation;
        return message;
    }

    /**
     * Message to drop tile in a given position.
     * @param newPosition
     */
    private Message dropTile(def position) {
        System.out.println("dropTile(): ${this.name}: position=${position}.");
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "DROP", position: position);
        message.operation = operation;
        return message;
    }

    /**
     * Message to use tile to cover an adjacent hole.
     * @param direction - LEFT, RIGHT, UP, DOWN
     */
    private Message useTile(String direction) {
        System.out.println("useTile(): ${this.name}: direction=${direction}.");
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "USE", direction: direction);
        message.operation = operation;
        return message;
    }

    /**
     * Message to transfer points.
     * @param toAgent - agent you are transferring points to.
     * @param transferPoints - number of points to transfer
     */
    private Message transferPoints(String toAgent, Integer transferPoints) {
        System.out.println("transferPoints(): ${this.name}: toAgent=${toAgent}; transferPoints=${transferPoints}.")
        Message message = new Message(sender: this.name, replayWith: "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation(code: "TRANSFER", toAgent: toAgent, transferPoints: transferPoints);
        message.operation = operation;
        return message;
    }

    /**
     * Message containing distances from current thread to other elements.
     *
     */
    private Message negotiate(def distances) {
        System.out.println("negotiate(): ${this.name}: distances=${distances}.")
        Message message = new Message(sender: this.name, replayWith: "NEGOTIATION_RESULT");
        Operation operation = new Operation(code: "NEGOTIATION", distances: distances);
        message.operation = operation;
        return message;
    }

    private def computeDistances(def position) {

        def distanceToHoles = [];
        environment.holes.each {
            def distance = Distance.findShortestPath([position.x,position.y], [it.xPosition, it.yPosition], environment.map);
            distanceToHoles.add([x:it.xPosition, y:it.yPosition, distance: distance]);
        }

        def distanceToTiles = [];
        environment.tiles.each {
            def distance = Distance.findShortestPath([position.x,position.y], [it.xPosition, it.yPosition], environment.map);
            distanceToTiles.add([x:it.xPosition, y:it.yPosition, distance: distance]);
        }

        return [holes: distanceToHoles, tiles: distanceToTiles];
    }

    /**
     * Send message to the agent handling the negotiation process (principal agent).
     * @param message - message to send.
     */
    private void notifyPrincipal(Message message) {
        negotiationMessageBox.addMessage(message);
    }

    /**
     * Send message to the environment.
     * @param message - message to send.
     */
    private void notifyEnvironment(Message message) {
        environmentMessageBox.addMessage(message);
        return;
    }
}
