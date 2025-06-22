package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.AuthProvider;

@Entity(tableName = "AUTH_PROVIDER",
        foreignKeys = @ForeignKey(entity = UserEntity.class, parentColumns = "userId", childColumns = "userId"))
public class AuthProviderEntity implements AuthProvider {

    @PrimaryKey
    @NonNull
    private int providerId;
    private int userId;
    private String providerType;
    private String providerUID;

    public AuthProviderEntity(int providerId, int userId, String providerType, String providerUID) {
        this.providerId = providerId;
        this.userId = userId;
        this.providerType = providerType;
        this.providerUID = providerUID;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public void setProviderUID(String providerUID) {
        this.providerUID = providerUID;
    }

    @Override
    public int getProviderId() {
        return providerId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getProviderType() {
        return providerType;
    }

    @Override
    public String getProviderUID() {
        return providerUID;
    }
}
