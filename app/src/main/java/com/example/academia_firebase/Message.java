package com.example.academia_firebase;

public class Message {
    private String content;
    private String userId;
    private String userName;
    private long insertedAt;

    public Message() { }

    public Message(String content, String userId, String userName, long insertedAt) {
        this.content = content;
        this.userId = userId;
        this.userName = userName;
        this.insertedAt = insertedAt;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public long getInsertedAt() { return insertedAt; }
    public void setInsertedAt(long insertedAt) { this.insertedAt = insertedAt; }
}
