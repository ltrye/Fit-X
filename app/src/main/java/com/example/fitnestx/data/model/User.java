package com.example.fitnestx.data.model;

public interface User {
    public int getId();
    public String getName();
    public int getAge();
    public boolean getGender();
    public String getEmail();
    public String passwordHash();
    public boolean getIsActive();
}
