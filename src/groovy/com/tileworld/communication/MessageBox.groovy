package com.tileworld.communication

import com.tileworld.TileWorldService

/**
 * Created by radu on 23/05/15.
 */
class MessageBox {

    TileWorldService tileWorldService;
    private String owner;
    private List<Message> messageList = new ArrayList<Message>();
    private Integer expectedMessages;

    public MessageBox(String owner, Integer expectedMessages, TileWorldService tileWorldService) {
        this.owner = owner;
        this.expectedMessages = expectedMessages;
        this.tileWorldService = tileWorldService;
    }

    public getOwner() {
        return owner;
    }

    public getMessageList() {
        return messageList;
    }

    public synchronized void checkMessageList(String threadName) {
        if(messageList.size() < expectedMessages) {
            System.out.println("${threadName}: checkMessageList(): Wait: messageList.size()=${messageList.size()}; expectedMessages=${expectedMessages}");
            wait();
        }
    }

    public synchronized void isMessageListProcessed() {
        System.out.println("isMessageListProcessed(): Wait: messageList.size()=${messageList.size()};");
        if(messageList.size() > 0) {
            wait();
        }
    }

    public synchronized void emptyMessageList(String threadName) {
        this.messageList.clear();
        System.out.println("${threadName}: emptyMessageList(): messageList.size()=${messageList.size()}");
        notifyAll();
    }

    public synchronized void addStatusMessage(Message message) {
        messageList.add(message);
        System.out.println("addStatusMessage(): messageList.size()=${messageList.size()};");
        notifyAll();
    }

    public synchronized void addMessage(Message message) {
        messageList.add(message);
        System.out.println("addMessage(): messageList.size()=${messageList.size()}; expectedMessages=${expectedMessages}");
        if(messageList.size() == expectedMessages) {
            notifyAll();
        }
    }
}