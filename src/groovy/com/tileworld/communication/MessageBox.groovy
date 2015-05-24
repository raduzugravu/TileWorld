package com.tileworld.communication

import com.tileworld.exceptions.UnknownOperationException

/**
 * Created by radu on 23/05/15.
 */
class MessageBox {

    private List<Message> messageList = new ArrayList<Message>();

    public getMessageList() {
        return messageList;
    }

    public synchronized void checkMessageList(numberOfMessages) {
        if(messageList.size() < numberOfMessages) {
            wait();
        }
    }

    public synchronized void emptyMessageList() {
        this.messageList.clear();
    }

    public synchronized void addMessage(Message message) {
        messageList.add(message)
    }
}