package com.gukov.pickrhyme.game_view;

import android.content.Context;
import android.util.Log;

import com.gukov.pickrhyme.util.FirebaseManager;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.util.SharedPreferencesManager;
import com.gukov.pickrhyme.model.ModelInterface;
import com.gukov.pickrhyme.object.Word;

import java.util.ArrayList;
import java.util.List;

public class GamePresenter implements GameContract.Presenter {

    public static final int WORDS_IN_LEVEL = 10; // количество слов на один уровень
    private int level = 1, points = 0, hints = 1, numberWordsForNextLevel = 10; // значения по умолчанию
    private List<Word> userWordsList = new ArrayList<>(); // массив введенных пользователем рифм
    private List<Word> allWordsList = new ArrayList<>(); // массив возможных рифм из базы данных

    private Context context;
    private GameContract.View view;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private FirebaseManager firebaseManager;

    public GamePresenter(Context context, GameContract.View view, ModelInterface model) {
        this.context = context;
        this.view = view;
        this.model = model;
        this.sPrefManager = SharedPreferencesManager.getInstance(context);
        firebaseManager = new FirebaseManager(context);
    }

    @Override
    public void loadSavedData(int level) {
        this.level = level;
        points = sPrefManager.getUserPoints();
        hints = sPrefManager.getUserHints();
        userWordsList = sPrefManager.getUserRhymes(level);
        numberWordsForNextLevel = WORDS_IN_LEVEL - userWordsList.size();
        allWordsList = model.getRhymesFromDatabase(level);
        updateUI();
        for (Word w : userWordsList)
            Log.d("KKK", w.getWord());
    }

    @Override
    public void onEnterWord(String inputText, String mode) {
        if (inputText.equals(""))
            view.showToastError(context.getString((R.string.enter_word)));
        else {
            if (!inputTextIsRhyme(inputText, mode))
                view.showToastError(context.getString(R.string.do_not_rhyme));
            updateUI();
        }
    }

    // проверяем, является ли введенный текст рифмой
    private boolean inputTextIsRhyme(String word, String mode) {
        for (Word w : allWordsList) {
            if (w.getWord().equals(word)) {
                if (!isAdded(word)) {
                    userWordsList.add(w);
                    points += 1;
                    view.showToastSuccess(context.getString(R.string.good_rhyme));
                    if (mode.equals("game")) {
                        numberWordsForNextLevel -= 1;
                        goToNextLevelIfNeed();
                    }
                    if (!sPrefManager.getIsFullVersion() && mode.equals("training") && userWordsList.size() % 7 == 0)
                        view.showAdsInterstitial();
                } else view.showToastWarning(context.getString(R.string.repeat_rhyme));
                return true;
            }
        }
        return false;
    }

    private boolean isAdded(String word) {
        for (Word w : userWordsList) {
            if (w.getWord().equals(word))
                return true;
        }
        return false;
    }

    // переходим на следующий уровень, если набрали необходимое количество слов
    private void goToNextLevelIfNeed() {
        if (numberWordsForNextLevel == 0) {
            sPrefManager.updateUserLevelData(level, points, hints, userWordsList);
            firebaseManager.setDataCloudFirestore(level);
            level += 1;
            numberWordsForNextLevel = WORDS_IN_LEVEL;
            if (sPrefManager.getIsFullVersion())
                hints += 2;
            userWordsList.clear();
            allWordsList = model.getRhymesFromDatabase(level);
            if (!sPrefManager.getIsFullVersion())
                view.showAdsInterstitial();
        }
    }

    @Override
    public void onHelpByPushkin() {
        if (hints > 0)
            hints--;
        view.showHelpByPushkinDialog(level);
    }

    @Override
    public void updateUI() {
        view.showLevelValue(String.valueOf(level));
        view.showPointsValue(String.valueOf(points));
        view.showWord(model.getWordFromDatabase(level));
        view.showNumberWordsForNextLevel(String.valueOf(numberWordsForNextLevel));
        view.showCurrentEnteredRhymes(userWordsList);
    }

    @Override
    public void saveResult() {
        sPrefManager.setUserData(level, points, hints, userWordsList);
    }

    @Override
    public void updateResult() {
        sPrefManager.updateUserLevelData(level, points, hints, userWordsList);
    }

}
