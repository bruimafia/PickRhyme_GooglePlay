package com.gukov.pickrhyme;

import android.content.Context;
import android.content.SharedPreferences;

import com.gukov.pickrhyme.object.Word;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesManager {

    private static final String APP_PREFERENCES = "com.gukov.pickrhyme";
    private static final String APP_IS_FULL_VERSION = "FULL_VERSION"; // версия приложения
    private static final String USER_LEVEL = "LEVEL"; // текущий уровень игры
    private static final String USER_POINTS = "POINTS"; // количество очков
    private static final String USER_HINTS = "HINTS"; // количество подсказок
    private static final String USER_ID = "USER_ID"; // id пользователя
    private static final String USER_NAME = "USER_NAME"; // имя пользователя
    private static final String USER_IS_PLAY_RATING = "PLAY_RATING"; // оценка игры (Google play)

    private static SharedPreferencesManager instance;
    private final SharedPreferences sPref;

    private SharedPreferencesManager(Context context) {
        sPref = context.getApplicationContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null)
            instance = new SharedPreferencesManager(context);
        return instance;
    }

    public boolean getAppIsFullVersion() {
        return sPref.getBoolean(APP_IS_FULL_VERSION, false);
    }

    public void setAppIsFullVersion(boolean isFullVersion) {
        sPref.edit().putBoolean(APP_IS_FULL_VERSION, isFullVersion).apply();
    }

    public int getUserLevel() {
        return sPref.getInt(USER_LEVEL, 1);
    }

    public void setUserLevel(int level) {
        sPref.edit().putInt(USER_LEVEL, level).apply();
    }

    public int getUserPoints() {
        return sPref.getInt(USER_POINTS, 0);
    }

    public void setUserPoints(int points) {
        sPref.edit().putInt(USER_POINTS, points).apply();
    }

    public int getUserHints() {
        return sPref.getInt(USER_HINTS, 2);
    }

    public void setUserHints(int hints) {
        sPref.edit().putInt(USER_HINTS, hints).apply();
    }

    public String getUserID() {
        return sPref.getString(USER_ID, null);
    }

    public void setUserId(String id) {
        sPref.edit().putString(USER_ID, id).apply();
    }

    public String getUserName() {
        return sPref.getString(USER_NAME, null);
    }

    public void setUserName(String name) {
        sPref.edit().putString(USER_NAME, name).apply();
    }

    public boolean getPlayRating() {
        return sPref.getBoolean(USER_IS_PLAY_RATING, false);
    }

    public void setPlayRating(boolean isPlayRating) {
        sPref.edit().putBoolean(USER_IS_PLAY_RATING, isPlayRating).apply();
    }

    public List<Word> getUserRhymes(int level) {
        String[] rhymes = sPref.getString("LEVEL" + level, "").split(";");
        List<Word> list = new ArrayList<>();
        for (String s : rhymes)
            if (!s.equals(""))
                list.add(new Word(s));
        return list;
    }

    public void setUserData(int level, int points, int hints, List<Word> rhymes) {
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt(USER_LEVEL, level);
        editor.putInt(USER_POINTS, points);
        editor.putInt(USER_HINTS, hints);
        editor.putString("LEVEL" + level, listWordToString(rhymes));
        editor.apply();
    }

    public void updateUserLevelData(int level, int points, int hints, List<Word> rhymes) {
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt(USER_POINTS, points);
        editor.putInt(USER_HINTS, hints);
        editor.putString("LEVEL" + level, listWordToString(rhymes));
        editor.apply();
    }

    public void clearAll() {
        sPref.edit().clear().apply();
    }

    private String listWordToString(List<Word> rhymes) {
        StringBuilder sb = new StringBuilder();
        for (Word w : rhymes)
            sb.append(w.getWord()).append(";");
        return sb.toString();
    }

}
