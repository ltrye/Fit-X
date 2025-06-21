package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.AuthProviderDAO;
import com.example.fitnestx.data.entity.AuthProviderEntity;

public class AuthProviderRepository {

    private final AuthProviderDAO authProviderDAO;

    public AuthProviderRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        authProviderDAO = appDatabase.authProviderDAO();
    }

    public void insertAuthProvider(AuthProviderEntity provider) {
        new Thread(() -> authProviderDAO.insertAuthProvider(provider)).start();
    }

    public LiveData<List<AuthProviderEntity>> getAllAuthProviders() {
        return authProviderDAO.getAllAuthProviders();
    }

    public AuthProviderEntity getAuthProviderById(int providerId) {
        return authProviderDAO.getAuthProviderById(providerId);
    }

    public LiveData<List<AuthProviderEntity>> getAuthProvidersByUserId(int userId) {
        return authProviderDAO.getAuthProvidersByUserId(userId);
    }

    public void updateAuthProvider(AuthProviderEntity provider) {
        new Thread(() -> authProviderDAO.updateAuthProvider(provider)).start();
    }

    public void deleteAuthProvider(AuthProviderEntity provider) {
        new Thread(() -> authProviderDAO.deleteAuthProvider(provider)).start();
    }

    public void deleteAllAuthProviders() {
        new Thread(() -> authProviderDAO.deleteAllAuthProviders()).start();
    }
}
