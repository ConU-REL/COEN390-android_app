package com.example.app;

public class User
{
    private String userName;
    private long userID;


    public User(String userName, long userID)
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

    public long getUserID()
    {
        return userID;
    }

    public void setUserID(long userID)
    {
        this.userID=userID;
    }

}
