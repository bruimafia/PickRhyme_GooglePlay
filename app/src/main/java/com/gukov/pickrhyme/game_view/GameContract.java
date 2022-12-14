package com.gukov.pickrhyme.game_view;

import com.gukov.pickrhyme.object.Word;

import java.util.List;

public interface GameContract {

    interface View {
        void showCurrentEnteredRhymes(List<Word> list); // показ введенных слов, если они есть

        void showLevelValue(String value); // показ текущего уровня игры

        void showPointsValue(String value); // показ текущего количества баллов

        void showWord(String value); // показ текущего загаданого слова

        void showNumberWordsForNextLevel(String value); // показ количества слов, необходимых для перехода на следующий уровень

        void showToastError(String message); // всплывающее сообщение Ошибка

        void showToastInfo(String message); // всплывающее сообщение Информация

        void showToastNormal(String message); // всплывающее сообщение Обычное

        void showToastSuccess(String message); // всплывающее сообщение Успех

        void showToastWarning(String message); // всплывающее сообщение Внимание

        void showGameEndDialog(); // показ финального диалогового окна, если проходим все уровни игры

        void showHelpByPushkinDialog(int level); // показ диалогового окна, если просим помощи у Пушкина

        void showAdsInterstitial(); // показ межстраничной рекламы в приложении
    }

    interface Presenter {
        void loadSavedData(int level); // подгрузка сохраненных данных игры

        void onEnterWord(String inputText, String mode); // нажатие кнопки Ввести слово

        void onHelpByPushkin(); // нажатие кнопки Помощь Пушкина

        void updateUI(); // отсылка данных на view

        void saveResult(); // сохранение результатов игры

        void updateResult(); // обновление результатов игры
    }

}
