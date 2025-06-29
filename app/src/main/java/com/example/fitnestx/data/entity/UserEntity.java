package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.User;

@Entity (tableName = "USER")
public class UserEntity implements User {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int userId;
    private String name;
    private int age;
    private boolean gender;
    private String email;
    private String password;
    private boolean isActive;

//    public UserEntity(int userId, String name, int age, boolean gender, String email, String password) {
//        this.userId = userId;
//        this.name = name;
//        this.age = age;
//        this.gender = gender;
//        this.email = email;
//        this.password = password;
//    }

    public UserEntity(int userId, String name, int age, boolean gender, String email, String password, boolean isActive) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
    }

    public void setId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean getGender() {
        return gender;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean getIsActive() {
        return isActive;
    }
}
