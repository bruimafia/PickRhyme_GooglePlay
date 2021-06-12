package com.gukov.pickrhyme.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gukov.pickrhyme.object.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Model implements ModelInterface {


    private Context context;
    private SQLiteDatabase database;

    public Model(Context context) {
        this.context = context;
        initDatabase();
    }

    private void initDatabase() {
        DatabaseHelper mDBHelper = new DatabaseHelper(context);
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        database = mDBHelper.getWritableDatabase();
    }

    @Override
    public List<Word> getAllWordsFromDataBase() {
        List<Word> list = new ArrayList<>();
        Cursor c = database.rawQuery("SELECT * FROM levels", null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            for (int i = 0; i < c.getCount(); i++) {
                list.add(new Word(c.getInt(0), c.getString(1)));
                c.moveToNext();
            }
        }
        c.close();
        return list;
    }

    @Override
    public String getWordFromDatabase(int level) {
        String word = null;
        Cursor c = database.rawQuery("SELECT * FROM levels WHERE level_id = " + level, null);
        c.moveToFirst();
        if (c.getCount() != 0)
            word = c.getString(1);
        c.close();
        return word;
    }

    @Override
    public List<Word> getRhymesFromDatabase(int level) {
        List<Word> list = new ArrayList<>();
        Cursor c = database.rawQuery("SELECT * FROM levels WHERE level_id = " + level, null);
        c.moveToFirst();
        if (c.getCount() != 0)
            list = listStringToListWord(Arrays.asList((c.getString(2)).split(",")));
        c.close();
        return list;
    }

    // полчение случайной рифмы из базы данных
    @Override
    public String getRandomRhymeFromDatabase(int level, List<Word> userRhymes) {
        List<Word> list = getRhymesFromDatabase(level);
        Word randomWord;
        do {
            randomWord = list.get((int) (Math.random() * list.size() + 1));
        } while (userRhymes.contains(randomWord));
        return randomWord.getWord();
    }

    private List<Word> listStringToListWord(List<String> listString) {
        List<Word> listWord = new ArrayList<>();
        for (String s : listString)
            listWord.add(new Word(s));
        return listWord;
    }

}
