package com.hpmtutorial.hpmotochat.model;

import java.io.Serializable;
import java.util.List;

public class Group {

    private String id;
    private String title;
    private List<Chat> messages;
    private List<User> users;
    private List<String> seenBy;

    public Group() {
    }

    public Group(String title, List<Chat> messages, List<User> users, List<String> seenBy) {
        this.title = title;
        this.messages = messages;
        this.users = users;
        this.seenBy = seenBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Chat> getMessages() {
        return messages;
    }

    public void setMessages(List<Chat> messages) {
        this.messages = messages;
    }

    public void addMessage(Chat message){
        this.messages.add(message);
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void addUser(User newUser){
        this.users.add(newUser);
    }

    public List<String> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(List<String> seenBy) {
        this.seenBy = seenBy;
    }

    public void addSeen(String uid){
        this.seenBy.add(uid);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
