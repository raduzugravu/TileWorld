package com.tileworld.communication

/**
 * Created by radu on 23/05/15.
 */
class Message {

    String sender;
    String inReplayTo;
    String replayWith;

    Operation operation;

    String successCode; // SUCCESS - operation succeeded; ERROR - operation failed

    public String toString() {
        return "Message: sender=${sender}; inReplayTo=${inReplayTo}; " +
                "replayWith=${replayWith}; operation=${operation}; " +
                "successCode=${successCode};"
    }
}
