package com.tileworld.representation

import com.tileworld.TileWorldService
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

    public processMessageList(TileWorldService tileWorldService) {
        messageBox.messageList.each { Message message ->
            tileWorldService.updateConsole("environment: ${message.toString()}");
            processMessage(message);
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

                if(map[operation.position.x][operation.position.y] != 'E') return false;

                // update map old position and new position
                map[agents.get(i).xPosition][agents.get(i).yPosition] = 'E';
                map[operation.position.x][operation.position.y] = 'A';

                // update agent old position and new position
                agents.get(i).xPosition = operation.position.x;
                agents.get(i).yPosition = operation.position.y;

                break;
            }
        }

        return true;
    }

    public Boolean isEmpty(def position) {

//        System.out.println("isEmpty(): position=${position}");
//        for (int i = 0; i < map.size(); i++) {
//            for (int j = 0; j < map.get(i).size(); j++) {
//                System.out.print("${map[i][j]} ");
//            }
//            System.out.println("");
//        }

        if( position.x >= 0 && position.x < gridHeight &&
            position.y >= 0 && position.y < gridWidth &&
            map[position.x][position.y] == 'E') {
            return true;
        }

        return false;
    }
}
