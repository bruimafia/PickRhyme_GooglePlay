package com.gukov.pickrhyme.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.adapter.WordAdapter;
import com.gukov.pickrhyme.model.DatabaseHelper;
import com.gukov.pickrhyme.databinding.FragmentAllRhymesBinding;
import com.gukov.pickrhyme.object.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DEPRECATEDallRhymesFragment extends DialogFragment {

    private FragmentAllRhymesBinding binding;
    private Context context;

    private String wordOfLevel = "..."; // слово текущего уровня
    private List<Word> aList = new ArrayList<>(); // массив для адаптера
    private List<String> words = new ArrayList<>(); // массив слов
    private SQLiteDatabase mDb;
    private int level;
    private String requestFrom;
    private Boolean fullVersion;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        view = inflater.inflate(R.layout.fragment_all_rhymes, container, false);
        binding = FragmentAllRhymesBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();
        getDialog().setCanceledOnTouchOutside(false);


        level = getArguments().getInt("level", 1);
        requestFrom = getArguments().getString("requestFrom", "");
        fullVersion = getArguments().getBoolean("fullVersion");

        initDB();
        loadingLevel();

        for (String s : words) {
            if (!s.equals(""))
                aList.add(new Word(s));
        }

        showEnteredWords();
        setInfo();
        binding.btnContinue.setOnClickListener(v -> {
            dismiss();
//            continueGame();
        });

        return binding.getRoot();
    }


    // загрузка информации на экран
    private void setInfo() {
        binding.tvFoundCountRhymes.setText(String.format(getString(R.string.found_count_rhymes), String.valueOf(words.size())));

        if (requestFrom.equals("GameActivity")) {
            binding.tvThereAreSuchRhymes.setText(String.format(getString(R.string.good_job), wordOfLevel));
            binding.btnContinue.setText(R.string.continue_title);
        } else {
            binding.tvThereAreSuchRhymes.setText(String.format(getString(R.string.quotation_marks), wordOfLevel));
            binding.tvThereAreSuchRhymes.setTextSize(30);
            binding.btnContinue.setText(R.string.close_title);
        }
    }


    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }


    // инициализация базы данных
    private void initDB() {
        DatabaseHelper mDBHelper = new DatabaseHelper(getContext());
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        mDb = mDBHelper.getWritableDatabase();
    }


    // загрузка текущего уровня из БД
    private void loadingLevel() {
        Cursor c = mDb.rawQuery("SELECT * FROM levels WHERE level_id = " + level, null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            wordOfLevel = c.getString(1);
            words = Arrays.asList((c.getString(2)).split(","));
        }
        c.close();
    }


    // отображение введенных слов
    private void showEnteredWords() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        binding.rvRhymes.setLayoutManager(layoutManager);
        Collections.shuffle(aList); // перемешать слова
        WordAdapter adapter = new WordAdapter(aList);
        binding.rvRhymes.setAdapter(adapter); // устанавливаем адаптер
    }

}