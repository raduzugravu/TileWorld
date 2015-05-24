package com.tileworld.communication

/**
 * Created by radu on 23/05/15.
 */
class Operation {

    String code; // operation code - PICK, DROP, USE, MOVE, TRANSFER
    String tileColor;
    def position = [:];
    Integer transferPoints = 0;

    public String toString() {
        return "Operation: code=${code}; tileColor=${tileColor}; position=${position}; transferPoints=${transferPoints};"
    }
}
