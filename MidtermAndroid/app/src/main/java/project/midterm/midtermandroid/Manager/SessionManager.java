package project.midterm.midtermandroid.Manager;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(String email, String userType) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USER_TYPE, userType);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public String getUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, "");
    }
}