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
    MessageBox negotiationMessageBox;
    Ticker ticker;
    TileWorldService tileWorldService;

    public NegotiationThread(MessageBox[] agentsMessageBox, MessageBox negotiationMessageBox,
                             Environment environment, Ticker ticker, TileWorldService tileWorldService, String agentName) {
        this.agentsMessageBox = agentsMessageBox;
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
            negotiationMessageBox.checkNegotiationMessageList(this.name); // wait for all agents to send distances
            computeNegotiationResult(); // based on distances decide action for agents
        }

        System.out.println(this.name + ": ended.")
    }

    /* --- OPERATIONS --- */

    /**
     * Message to move to another adjacent cell.
     * @param newPosition - position the agent wants to get to
     */
    private Message move(def newPosition) {
        System.out.println("move(): ${this.name}: position=${newPosition}.")
        Message message = new Message(sender: this.name, inReplayTo: "NEGOTIATION");
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
        Message message = new Message(sender: this.name, inReplayTo: "NEGOTIATION");
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
        Message message = new Message(sender: this.name, inReplayTo: "NEGOTIATION");
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
        Message message = new Message(sender: this.name, inReplayTo: "NEGOTIATION");
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
        Message message = new Message(sender: this.name, inReplayTo: "NEGOTIATION");
        Operation operation = new Operation(code: "TRANSFER", toAgent: toAgent, transferPoints: transferPoints);
        message.operation = operation;
        return message;
    }

    private void moveRandom(Agent agent) {
        def directions = ["UP", "DOWN", "LEFT", "RIGHT"];
        Random random = new Random();
        String randomDirection = directions[random.nextInt(directions.size())]
        Message message;
        switch(randomDirection) {
            case "UP":
                message = move([x:agent.xPosition-1, y:agent.yPosition]);
                break;
            case "DOWN":
                message = move([x:agent.xPosition+1, y:agent.yPosition]);
                break;
            case "LEFT":
                message = move([x:agent.xPosition, y:agent.yPosition-1]);
                break;
            case "RIGHT":
                message = move([x:agent.xPosition, y:agent.yPosition+1]);
                break;
            default:
                message = move([x:agent.xPosition, y:agent.yPosition]);
        }

        notifyAgent(agent.name, message);
    }

    /**
     * Read all other messages containing distances from other agents and make a decision for each agent.
     * @param distances
     */
    private void computeNegotiationResult() {

        println("computeNegotiationResult: ${this.name} got here.")

        def distances = [];
        negotiationMessageBox.messageList.each {
            tileWorldService.updateConsole("${this.name}: ${it}");
            if("NEGOTIATION".equalsIgnoreCase(it.operation.code)) {
                distances.add([owner: it.sender, distances: it.operation.distances]);
            }
        }

        println("computeNegotiationResult: ${this.name} got here: distances=${distances}.")

        def map = environment.getMap();
        environment.agents.eachWithIndex { Agent agent, i ->
            decideAgentAction(agent, map, distances)
        }

        // notify messages list processing ended
        negotiationMessageBox.emptyMessageList(this.name)

    }

    private void decideAgentAction(Agent agent, def map, def distances) {

        println("decideAgentAction(): agentName=${agent.name}; hasTile=${agent.tile}; map=${map}; distances=${distances}");

        // if agent is on a tile and has no tile pick it up
        if(!agent.tile && map[agent.xPosition][agent.yPosition].contains("T")) {
            if(map[agent.xPosition][agent.yPosition].contains("T")) {
                def message = pickTile([x:agent.xPosition, y:agent.yPosition]);
                notifyAgent(agent.name, message);
            } else {
                moveRandom(agent);
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
                        } else {
                            moveRandom(agent);
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
                        return;
                    } else {
                        moveRandom(agent);
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
