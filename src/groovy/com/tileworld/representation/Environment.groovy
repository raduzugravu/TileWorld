package com.tileworld.representation

import com.tileworld.TileWorldService
import com.tileworld.communication.Message
import com.tileworld.communication.MessageBox
import com.tileworld.communication.Operation
import com.tileworld.exceptions.UnknownOperationException
import sun.management.resources.agent

/**
 * Created by radu on 16/05/15.
 */
public class Environment {

    Integer numberOfAgents;
    long tickTime;
    long totalTime;
    long remainingTime;
    Integer gridWidth;
    Integer gridHeight;

    List<Agent> agents = new ArrayList<Agent>();
    List<Tile> tiles = new ArrayList<Tile>();
    List<Obstacle> obstacles = new ArrayList<Obstacle>();
    List<Hole> holes = new ArrayList<Hole>();

    Generator generator = null;

    def map = [[]]; // this is a way to represent what a cell contains: E=empty; A=agent; T=tile; H=hole; O=obstacle

    /**
     *
     * @param author
     * @param operation
     * @return
     */
    public Boolean executeOperation(String author, Operation operation) {
        switch(operation.code) {
            case "PICK":
                return pick(author, operation);
            case "DROP":
                return drop(author, operation);
            case "USE":
                return use(author, operation);
            case "MOVE":
                return move(author, operation);
            case "TRANSFER":
                return transfer(author, operation);
            default:
                throw new UnknownOperationException("${author} requested an operation with an unknown code. Operation: ${operation}");
        }
    }

    /**
     * Move agent LEFT/RIGHT/UP/Down if a cell is not a hole or an obstacle.
     * @param author
     * @param operation
     * @return
     */
    private Boolean move(String author, Operation operation) {

        if(!canMoveTo(operation.position)) return false;

        for(int i = 0; i < numberOfAgents; i++) {

            if(author.equalsIgnoreCase(agents.get(i).name)) {

                // update agent old position and new position
                agents.get(i).xPosition = operation.position.x;
                agents.get(i).yPosition = operation.position.y;
                initialiseMap();
                return true;
            }
        }

        return false;
    }

    /**
     * Pick a tile from the current cell.
     * @param author
     * @param operation
     * @return
     */
    private Boolean pick(String author, Operation operation) {

        if(!map[operation.position.x][operation.position.y].contains("T")) return false;

        agents.each { Agent agent ->
            if(author.equalsIgnoreCase(agent.name)) {
                def iterator = tiles.iterator();
                while(iterator.hasNext()) {
                    Tile tile = iterator.next();
                    if((tile.xPosition == operation.position.x) && (tile.yPosition == operation.position.y)) {
                        agent.tile = tile;
                        iterator.remove();
                        initialiseMap();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Drop the tile the author agent is carrying.
     * @param author
     * @param operation
     * @return
     */
    private Boolean drop(String author, Operation operation) {

        if( map[operation.position.x][operation.position.y] == "H" ||
            map[operation.position.x][operation.position.y] == "O")
            return false;

        agents.each { Agent agent ->
            if(author.equalsIgnoreCase(agent.name)) {

                Tile tile = agent.tile;
                tile.xPosition = agent.xPosition;
                tile.yPosition = agent.yPosition;
                tiles.add(tile);

                agent.tile = null;

                initialiseMap();

                return true;
            }
        }

        return false;
    }

    /**
     * Use the tile the author agent is carrying to cover an adjacent hole.
     * @param author - The agent carrying the tile.
     * @param operation - Identifies the hole to be covered.
     * @return
     */
    private Boolean use(String author, Operation operation) {

        agents.each { Agent agent ->
            if (author.equalsIgnoreCase(agent.name)) {

                // based on agent's position trying to cover a hole find hole's position
                def holePosition = [x: agent.xPosition, y: agent.yPosition];
                switch (operation.direction) {
                    case "LEFT":
                        holePosition.y -= 1;
                        break;
                    case "RIGHT":
                        holePosition.y += 1;
                        break;
                    case "UP":
                        holePosition.x -= 1;
                        break;
                    case "DOWN":
                        holePosition.x += 1;
                        break;
                    default:
                        return false;
                }

                // identify hole and lower its depth
                if(map[holePosition.x][holePosition.y] != "H") return false;
                def iterator = holes.iterator();
                while(iterator.hasNext()) {
                    Hole hole = iterator.next();
                    if((hole.xPosition == holePosition.x) && (hole.yPosition == holePosition.y)) {
                        iterator.remove();
                        hole.depth -= 1;
                        if(hole.depth > 0)
                            holes.add(hole);
                        initialiseMap();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Transfer points from one agent to another.
     * @param author - The agent transferring the points.
     * @param operation - Transfer points operation (contains agent to transfer to and number of points).
     * @return
     */
    private Boolean transfer(String author, Operation operation) {

        // remove points from agent transferring
        for(int i = 0; i < numberOfAgents; i++) {
            if(author.equalsIgnoreCase(agents.get(i).name)) {
                if(agents.get(i).points - operation.transferPoints < 0)
                    return false;
                agents.get(i).points -= operation.transferPoints;
                break;
            }
        }

        // add points to agent transferring to
        for(int i = 0; i < numberOfAgents; i++) {
            if(operation.toAgent.equalsIgnoreCase(agents.get(i).name)) {
                agents.get(i).points += operation.transferPoints;
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param position
     * @return
     * TODO: This method is public for testing only - convert this method to private after removing logic from AgentThread
     */
    public Boolean canMoveTo(def position) {

        if( position.x >= 0 && position.x < gridHeight &&
            position.y >= 0 && position.y < gridWidth &&
            map[position.x][position.y] != 'H' &&
            map[position.x][position.y] != 'O' &&
            map[position.x][position.y] != 'A') {
            return true;
        }

        return false;
    }

    /**
     * Mark each cell with a character corresponding to the entity currently in that cell (agents, tiles, holes, obstacles).
     * This will ease the process of finding out if an adjacent cell is free or not.
     * @param environment
     * @return map - a matrix marking all cells that are not empty in this tile world game.
     */
    public synchronized void initialiseMap() {

        map = [[]];

        for(int i = 0; i < gridHeight; i++) {
            def row = [];
            for(int j = 0; j < gridWidth; j++) {
                row.add("E");
            }
            map.add(row);
        }

        // mark agents
        for(int i = 0; i < agents.size(); i++) {
            map[agents.get(i).xPosition][agents.get(i).yPosition] = "A";
        }

        // mark tiles
        for(int i = 0; i < tiles.size(); i++) {
            map[tiles.get(i).xPosition][tiles.get(i).yPosition] += "T";
        }

        // mark holes
        for(int i = 0; i < holes.size(); i++) {
            map[holes.get(i).xPosition][holes.get(i).yPosition] = "H";
        }

        // mark obstacles
        for(int i = 0; i < obstacles.size(); i++) {
            map[obstacles.get(i).xPosition][obstacles.get(i).yPosition] = "O";
        }
    }

    public String getScore() {
        String score = "";
        agents.each { Agent agent ->
            score += "${agent.name}: ${agent.points}\n"
        }

        return score;
    }
}
