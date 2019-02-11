package com.boostcamp.dreampicker.utils;

import com.boostcamp.dreampicker.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FirebaseManager {
    @NonNull
    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Nullable
    public static User getCurrentUser() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null) {
            return null;
        } else {
            return new User(
                    firebaseUser.getUid(),
                    firebaseUser.getDisplayName(),
                    firebaseUser.getPhotoUrl() == null ? null : firebaseUser.getPhotoUrl().toString());
        }
    }
}
