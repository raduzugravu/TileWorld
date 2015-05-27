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
class NegotiationThread extends Thread {

    Environment environment;
    MessageBox[] agentsMessageBox;
    MessageBox environmentMessageBox;
    Ticker ticker;
    TileWorldService tileWorldService;
    Boolean principal = false;

    public NegotiationThread(MessageBox[] agentsMessageBox, MessageBox environmentMessageBox, Environment environment,
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
            def distances = computeDistances(getPosition());

            // principal agent intermediates negotiation between agents and decides agents actions
            if(this.principal) {

                waitNegotiationMessages(); // wait for all agents to send distances
                computeNegotiationResult(); // based on distances decide action for agents

                // decide action for principal thread
                for(int i = 0; i < environment.agents.size(); i++) {
                    if(environment.agents.get(i).name.equalsIgnoreCase(this.name)) {
                        decideAgentAction(environment.agents.get(i), environment.getMap(), [[owner: this.name, distances: distances]]);
                        break;
                    }
                }
            } else {
                Message negotiationMessage = negotiate(distances);
                notifyPrincipal(negotiationMessage);
                waitNegotiationStatusMessage();
            }

            processMessageList();

            environmentMessageBox.checkMessageList(this.name);
            environmentMessageBox.isMessageListProcessed();

            processMessageList();
        }

        System.out.println(this.name + ": ended.")
    }

    public void markAsPrincipal() {
        this.principal = true;
    }

    private def getPosition() {
        for(int i = 0; i < environment.agents.size(); i++) {
            if(this.name.equalsIgnoreCase(environment.agents.get(i).name)) {
                return [x: environment.agents.get(i).xPosition, y: environment.agents.get(i).yPosition]
            }
        }
    }

    private void makeRandomMove(String agentName) {

        def position;
        for(int i = 0; i < environment.agents.size(); i++) {
            if(agentName.equalsIgnoreCase(environment.agents.get(i).name)) {
                position = [x: environment.agents.get(i).xPosition, y: environment.agents.get(i).yPosition];
            }
        }

        // check left/right/up/down
        if(environment.canMoveTo([x: position.x, y: position.y-1])) {
            def message = move([x: position.x, y: position.y-1]);
            notifyAgent(agentName, message);
        } else if(environment.canMoveTo([x: position.x, y: position.y+1])) {
            def message = move([x: position.x, y: position.y+1]);
            notifyAgent(agentName, message);
        } else if(environment.canMoveTo([x: position.x-1, y: position.y])) {
            def message = move([x: position.x-1, y: position.y]);
            notifyAgent(agentName, message);
        } else if(environment.canMoveTo([x: position.x+1, y: position.y])) {
            def message = move([x: position.x+1, y: position.y]);
            notifyAgent(agentName, message);
        } else {
            // move to the same position :)
            def message = move([x: position.x, y: position.y]);
            notifyAgent(agentName, message);
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
     * Read all other messages containing distances from other agents and make a decision for each agent.
     * @param distances
     */
    private void computeNegotiationResult() {

        println("computeNegotiationResult: ${this.name} got here.")

        def distances = [];
        for(int i = 0; i < agentsMessageBox.size(); i++) {
            if(this.name.equalsIgnoreCase(agentsMessageBox[i].owner)) {
                agentsMessageBox[i].messageList.each {
                    if("NEGOTIATION".equalsIgnoreCase(it.operation.code)) {
                        distances.add([owner: it.sender, distances: it.operation.distances]);
                    }
                }
                break;
            }
        }

        println("computeNegotiationResult: ${this.name} got here: distances=${distances}.")

        def map = environment.getMap();
        environment.agents.eachWithIndex { Agent agent, i ->
            if (agent.name.equalsIgnoreCase(this.name)) return;
            decideAgentAction(agent, map, distances)
        }

        // notify messages list processing ended
        for(int i = 0; i < agentsMessageBox.size(); i++) {
            if(this.name.equalsIgnoreCase(agentsMessageBox[i].owner)) {
                agentsMessageBox[i].emptyMessageList(this.name);
                break;
            }
        }

    }

    private void decideAgentAction(Agent agent, def map, def distances) {

        println("decideAgentAction(): agentName=${agent.name}; hasTile=${agent.tile}; map=${map}; distances=${distances}");

        // if agent is on a tile and has no tile pick it up
        if(!agent.tile && map[agent.xPosition][agent.yPosition].contains("T")) {
            if(map[agent.xPosition][agent.yPosition].contains("T")) {
                def message = pickTile([x:agent.xPosition, y:agent.yPosition]);
                notifyAgent(agent.name, message);
            }
        }
        // if agent has a tile cover a hole or go to the closest hole
        else if(agent.tile) {
            // cover a hole
            String direction = "";
            if(agent.xPosition - 1 >= 0 && map[agent.xPosition - 1][agent.yPosition].contains("H")) direction = "UP";
            if(agent.xPosition + 1 < environment.gridHeight && map[agent.xPosition + 1][agent.yPosition].contains("H")) direction = "DOWN";
            if(agent.yPosition - 1 >= 0 && map[agent.xPosition][agent.yPosition - 1].contains("H")) direction = "LEFT";
            if(agent.yPosition + 1 < environment.gridWidth && map[agent.xPosition][agent.yPosition+1].contains("H")) direction="RIGHT";
            if(direction.size() > 0) {
                def message = useTile(direction);
                notifyAgent(agent.name, message);
            } else {
                // get to the closest hole
                distances.each {
                    if(it.owner.equalsIgnoreCase(agent.name)) {
                        def position = getDirectionToClosestElement(agent, it.distances.holes);
                        if(position) {
                            def message = move(position);
                            notifyAgent(agent.name, message);
                        }
                    }
                }
            }
        } else {
            // get to the closest tile
            distances.each {
                if(it.owner.equalsIgnoreCase(agent.name)) {
                    def position = getDirectionToClosestElement(agent, it.distances.tiles);
                    if(position) {
                        def message = move(position);
                        notifyAgent(agent.name, message);
                    } else {
                        def message = move([x:agent.xPosition, y:agent.yPosition]);
                        notifyAgent(agent.name, message);
                    }
                }
            }
        }
    }

    private getDirectionToClosestElement(Agent agent, def elements) {

        int size = environment.gridHeight * environment.gridWidth;
        int k = 0;

        if(!elements || elements.size() == 0) return false;

        elements.eachWithIndex { element, i ->
            if(element.distance && element.distance.size() < size) {
                size = element.distance.size();
                k = i;
            }
        }

        if(elements[k] && elements[k].distance && elements[k].distance.size() > 0) {
            switch(elements[k]?.distance[0]) {
                case "north":
                    return [x: agent.xPosition - 1, y: agent.yPosition];
                case "south":
                    return [x: agent.xPosition + 1, y: agent.yPosition];
                case "east":
                    return [x: agent.xPosition, y: agent.yPosition + 1];
                case "west":
                    return [x: agent.xPosition, y: agent.yPosition - 1];
            }
        }

        return false;
    }

    private void waitNegotiationMessages() {
        for(int i = 0; i < environment.agents.size(); i++) {
            if(environment.agents.get(i).name.equalsIgnoreCase(this.name)) {
                agentsMessageBox[i].checkNegotiationMessageList(this.name);
                break;
            }
        }
    }

    private void waitNegotiationStatusMessage() {
        println("${this.name}: waitNegotiationStatusMessage();")
        for(int i = 0; i < environment.agents.size(); i++) {
            if(environment.agents.get(i).principal) {
                agentsMessageBox[i].isMessageListProcessed();
                break;
            }
        }
    }

    /**
     * Send message to the agent handling the negotiation process (principal agent).
     * @param message - message to send.
     */
    private void notifyPrincipal(Message message) {
        for(int i = 0; i < environment.agents.size(); i++) {
            if(environment.agents.get(i).principal) {
                agentsMessageBox[i].addNegotiationMessage(message);
                return;
            }
        }
    }

    /**
     * Send message to the environment.
     * @param message - message to send.
     */
    private void notifyEnvironment(Message message) {
        environmentMessageBox.addMessage(message);
        return;
    }

    /**
     * Send message to an agent.
     * @param agentName - agent name used to identify the agent message box.
     * @param message - message to send.
     */
    private void notifyAgent(String agentName, Message message) {
        println "notifyAgent: message=${message}";
        for(int i = 0; i < agentsMessageBox.size(); i++) {
            if(agentsMessageBox[i].owner.equalsIgnoreCase(agentName)) {
                agentsMessageBox[i].addMessage(message);
                return;
            }
        }
    }

    /* --- END OPERATIONS --- */

}
