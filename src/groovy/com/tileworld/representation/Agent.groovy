package com.tileworld.representation

import com.tileworld.communication.MessageBox

/**
 * Created by radu on 16/05/15.
 */
class Agent {

    String name; // AgentThread identifier
    Integer points = 0; // number of points accumulated by this agent

    String color;
    Integer xPosition;
    Integer yPosition;

    Tile tile; // the tile this agent is carrying
}
