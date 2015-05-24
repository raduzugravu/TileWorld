package com.tileworld.representation

import com.tileworld.communication.Message
import com.tileworld.communication.MessageBox
import com.tileworld.communication.Operation
import com.tileworld.exceptions.UnknownOperationException

/**
 * Created by radu on 16/05/15.
 */
public class Environment {

    Integer numberOfAgents;
    Integer tickTime;
    Integer totalTime;
    Integer gridWidth;
    Integer gridHeight;

    List<Agent> agents = new ArrayList<Agent>();
    List<Tile> tiles = new ArrayList<Tile>();
    List<Obstacle> obstacles = new ArrayList<Obstacle>();
    List<Hole> holes = new ArrayList<Hole>();

    Generator generator;

    def map = [[]]; // this is a way to represent what a cell contains: E=empty; A=agent; T=tile; H=hole; O=obstacle

    MessageBox messageBox;

    public processMessageList() {
        System.out.println ("environment: processMessageList: size=${this.messageBox.messageList.size()}.");
        for(int i = 0; i < messageBox.messageList.size(); i++) {
            processMessage(messageBox.messageList.get(i));
        }

        messageBox.emptyMessageList();
    }

    private void processMessage(Message message) {

        try {
            if(executeOperation(message.sender, message.operation)) {
                // send success message
                Message successMessage = new Message(sender: "environment", inReplayTo: message.operation.code, successCode: "SUCCESS");
                for(int i = 0; i < numberOfAgents; i++) {
                    if(message.sender.equalsIgnoreCase(agents.get(i).name)) {
                        agents.get(i).messageBox.addMessage(successMessage);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // send error message
        Message errorMessage = new Message(sender: "environment", inReplayTo: message.operation.code, successCode: "ERROR");
        for(int i = 0; i < numberOfAgents; i++) {
            if(message.sender.equalsIgnoreCase(agents.get(i).name)) {
                agents.get(i).messageBox.addMessage(errorMessage);
                return;
            }
        }
    }

    private Boolean executeOperation(String author, Operation operation) {
        switch(operation.code) {
            case "PICK":
                break;
            case "DROP":
                break;
            case "USE":
                break;
            case "MOVE":
                return move(author, operation);
            case "TRANSFER":
                break;
            default:
                throw new UnknownOperationException("${author} requested an operation with an unknown code. Operation: ${operation}");
        }

        return false;
    }

    private Boolean move(String author, Operation operation) {

        if(!isEmpty(operation.position)) return false;

        for(int i = 0; i < numberOfAgents; i++) {

            if(author.equalsIgnoreCase(agents.get(i).name)) {

                agents.get(i).xPosition = operation.position.x;
                agents.get(i).yPosition = operation.position.y;

                if(map[operation.position.x][operation.position.y] != 'E') return false;

                map[operation.position.x][operation.position.y] = 'A';

                break;
            }
        }

        return true;
    }

    public Boolean isEmpty(def position) {
        if(map[position.x][position.y] == 'E' &&
                position.x >= 0 && position.x < gridWidth &&
                position.y >= 0 && position.y < gridHeight) {
            return true;
        }

        return false;
    }
}
