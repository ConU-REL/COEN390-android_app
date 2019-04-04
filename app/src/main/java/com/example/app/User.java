package com.example.app;

public class User
{
    private String userName;
    private Integer userID;
    private long sessionID;


    public User(String userName, Integer userID,long sessionID)
    {
        this.userName=userName;
        this.userID=userID;
        this.sessionID=sessionID;
    }

    public User(String userName,Integer userID)
    {
        this.userName=userName;
        this.userID=userID;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    
    public Integer getUserID()
    {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public long getSessionID() {
        return sessionID;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }
    
}
