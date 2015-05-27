package com.tileworld.communication

/**
 * Created by radu on 23/05/15.
 */
class Operation {

    String code; // operation code - PICK, DROP, USE, MOVE, TRANSFER, NEGOTIATE
    String toAgent; // used for TRANSFER
    def position = [:]; // used for MOVE/PICK/DROP
    Integer transferPoints = 0; // used for TRANSFER
    String direction // LEFT/RIGHT/UP/DOWN - used for USE
    def distances; // used for the negotiation process

    public String toString() {
        return "Operation: code=${code}; toAgent=${toAgent}; position=${position}; " +
                "transferPoints=${transferPoints}; direction=${direction}; distances=${distances};"
    }
}
