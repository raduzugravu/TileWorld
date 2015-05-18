package com.tileworld
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

}
