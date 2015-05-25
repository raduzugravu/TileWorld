package com.tileworld.communication

/**
 * Created by radu on 23/05/15.
 */
class MessageBox {

    private List<Message> messageList = new ArrayList<Message>();
    private Integer expectedMessages;


    public getMessageList() {
        return messageList;
    }

    public synchronized void checkMessageList(Integer numberOfMessages) {
        expectedMessages = numberOfMessages;
        if(messageList.size() < expectedMessages) {
            System.out.println("checkMessageList(): Wait: messageList.size()=${messageList.size()}; expectedMessages=${expectedMessages}");
            wait();
        }
    }

    public synchronized isMessageListProcessed() {
        System.out.println("isMessageListProcessed(): Wait: messageList.size()=${messageList.size()};");
        if(messageList.size() > 0) {
            wait();
        }
    }

    public synchronized void emptyMessageList() {
        this.messageList.clear();
        notifyAll();
    }

    public synchronized void addMessage(Message message) {
        messageList.add(message);
        if(messageList.size() == expectedMessages) {
            notifyAll();
        }
    }
}