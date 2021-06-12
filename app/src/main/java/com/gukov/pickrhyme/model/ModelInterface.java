package com.gukov.pickrhyme.model;

import com.gukov.pickrhyme.object.Word;

import java.util.List;

public interface ModelInterface {
    List<Word> getAllWordsFromDataBase(); // получение всех слов (уровней)
    String getWordFromDatabase(int level); // получение слова из базы данных
    List<Word> getRhymesFromDatabase(int level); // получение всех возможных рифм из базы данных
    String getRandomRhymeFromDatabase(int level, List<Word> userRhymes); // полчение случайной рифмы из базы данных
}
