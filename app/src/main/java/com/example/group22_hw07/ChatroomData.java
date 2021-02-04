package com.example.group22_hw07;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatroomData implements Serializable {
    String ChatroomID;
    ArrayList<Message> Messages;

    public ChatroomData() {
    }

    public ChatroomData(Map<String, Object> ChatroomDataMap) {
        this.ChatroomID = (String) ChatroomDataMap.get("ChatroomID");
        this.Messages = (ArrayList<Message>) ChatroomDataMap.get("Messages");
    }

    public Map ChatroomData() {
        Map<String, Object> ChatroomDataMap = new HashMap<>();

        ChatroomDataMap.put("ChatroomID", this.ChatroomID);
        ChatroomDataMap.put("Messages", this.Messages);

        return ChatroomDataMap;
    }


    @Override
    public String toString() {
        return "ChatroomData{" +
                "ChatroomID='" + ChatroomID + '\'' +
                ", Messages=" + Messages +
                '}';
    }

    public String getChatroomID() {
        return ChatroomID;
    }

    public void setChatroomID(String chatroomID) {
        ChatroomID = chatroomID;
    }

    public ArrayList<Message> getMessages() {
        return Messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        Messages = messages;
    }
}
