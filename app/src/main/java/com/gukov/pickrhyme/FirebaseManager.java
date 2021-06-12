package com.gukov.pickrhyme;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gukov.pickrhyme.object.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {

    private static final String COLLECTION_USER = "users"; // имя коллекции пользователей в облаке firebase
    private static final String COLLECTION_SUGGEST = "suggest_rhymes"; // имя коллекции  предложенных рифм в облаке firebase
    private static final String COLLECTION_UPGRADE = "upgrade"; // имя коллекции обновления в облаке firebase
    private static final String DOCUMENT_UPGRADE = "last_code_app"; // имя документа обновления в облаке firebase
    private static final int COUNT_LEVEL = 100; // количество уровней

    private FirebaseFirestore firestore;
    private SharedPreferencesManager sPrefManager;


    public FirebaseManager(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            firestore = FirebaseFirestore.getInstance();
        sPrefManager = SharedPreferencesManager.getInstance(context);
    }

    public void checkDataCloudFirestore() {
        if (firestore != null && sPrefManager.getUserID() != null) {
            firestore.collection(COLLECTION_USER)
                    .document(sPrefManager.getUserID())
                    .get().addOnSuccessListener(task -> {
                if (task.exists()) {
                    if ((long) (task.getLong("points")) > sPrefManager.getUserPoints() && task.getLong("level") != null) {
                        Log.d("FirebaseManager", "level | " + task.getLong("level"));
                        Log.d("FirebaseManager", "hints | " + task.getLong("hints"));
                        Log.d("FirebaseManager", "points | " + task.getLong("points"));

                        sPrefManager.setUserLevel((int) (long) task.getLong("level"));

                        Map<String, Object> map = task.getData();
                        for (int i = 1; i <= (int) (long) task.getLong("level"); i++) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                if (entry.getKey().equals("level" + i)) {
                                    List<String> list = (List<String>) task.get("level" + i);
                                    sPrefManager.updateUserLevelData(i, (int) (long) task.getLong("points"), (int) (long) task.getLong("hints"), listStringToListWord(list));
                                }

                            }
                        }

                    } else
                        setAllDataCloudFirestore();
                }
            });
        }
    }

    // получение данных о пользователе с cloud firestore
    private void setAllDataCloudFirestore() {
        for (int level = 1; level <= sPrefManager.getUserLevel(); level++)
            setDataCloudFirestore(level);
    }

    // отправка данных о пользователе в cloud firestore
    public void setDataCloudFirestore(int level) {
        Log.d("FirebaseManager", sPrefManager.getUserID() + " | " + sPrefManager.getUserName() + " | " + level);
        if (firestore != null && sPrefManager.getUserID() != null) {
            Map<String, Object> ex = new HashMap<>();
            ex.put("name", sPrefManager.getUserName());
            ex.put("level", sPrefManager.getUserLevel());
            ex.put("points", sPrefManager.getUserPoints());
            ex.put("hints", sPrefManager.getUserHints());
            ex.put("level" + level, listWordToListString(sPrefManager.getUserRhymes(level)));
            ex.put("isPurchased", sPrefManager.getAppIsFullVersion());

            firestore.collection(COLLECTION_USER).document(sPrefManager.getUserID()).set(ex, SetOptions.merge());
        }
    }

    public List<String> listWordToListString(List<Word> listWord) {
        List<String> list = new ArrayList<>();
        for (Word w : listWord)
            list.add(w.getWord());
        return list;
    }

    public List<Word> listStringToListWord(List<String> listString) {
        List<Word> list = new ArrayList<>();
        for (String s : listString)
            list.add(new Word(s));
        return list;
    }

    // отправка предложения рифмы
    public void sendSuggestRhymeToCloudFirestore(String level, String suggest) {
        if (firestore != null) {
            Map<String, Object> ex = new HashMap<>();
            ex.put("level", level);
            ex.put("suggest", suggest);

            firestore.collection(COLLECTION_SUGGEST).add(ex);
        }
    }

    // сброс результатов
    public void clearData() {
        if (firestore != null && sPrefManager.getUserID() != null)
            firestore.collection(COLLECTION_USER).document(sPrefManager.getUserID()).delete();
    }

    // получение FCM токена для cloud message
    public void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(s -> Log.d("FirebaseManager", "Fetching FCM registration token failed: " + s));
    }

}
