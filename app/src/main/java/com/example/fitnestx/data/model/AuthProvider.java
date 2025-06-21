package com.example.fitnestx.data.model;

public interface AuthProvider {
    public int getProviderId();
    public int getUserId();
    public String getProviderType();
    public String getProviderUID();
}
