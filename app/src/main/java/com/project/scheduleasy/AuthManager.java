package com.project.scheduleasy;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREFS_NAME = "AppAuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_USERNAME = "username";
    private static final String FACULTY_TYPE = "FACULTY";
    private static final String STUDENT_TYPE = "STUDENT";

    private final SharedPreferences prefs;
    private static AuthManager instance;

    private AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    public void saveFacultyLogin(String username) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_TYPE, FACULTY_TYPE)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public boolean isFacultyLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) &&
                FACULTY_TYPE.equals(prefs.getString(KEY_USER_TYPE, ""));
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        prefs.edit()
                .clear()
                .apply();
    }

    public String getCurrentUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getCurrentUserType() {
        return prefs.getString(KEY_USER_TYPE, null);
    }
}
