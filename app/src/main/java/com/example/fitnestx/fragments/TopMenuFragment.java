package com.example.fitnestx.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnestx.R;
import com.example.fitnestx.ui.LoginActivity;
import com.example.fitnestx.ui.ProfileActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class TopMenuFragment extends Fragment {

    private ImageView menuButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_menu, container, false);

        initViews(view);
        setupMenuButton();

        return view;
    }

    private void initViews(View view) {
        menuButton = view.findViewById(R.id.iv_menu);
    }

    private void setupMenuButton() {
        menuButton.setOnClickListener(v -> showPopupMenu());
    }

    private void showPopupMenu() {
        PopupMenu popup = new PopupMenu(getContext(), menuButton);
        popup.getMenuInflater().inflate(R.menu.top_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_profile) {
                openProfile();
                return true;
            } else if (itemId == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void openProfile() {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        startActivity(intent);
    }

    private void logout() {
        if (getContext() == null) return;

        var gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        var mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            // Clear all relevant preferences
            SharedPreferences pref = getContext().getSharedPreferences("FitnestX", Context.MODE_PRIVATE);
            pref.edit().clear().apply();

            SharedPreferences authPrefs = getContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
            authPrefs.edit().clear().apply();

            SharedPreferences userProfilePrefs = getContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
            userProfilePrefs.edit().clear().apply();

            Toast.makeText(getContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}
