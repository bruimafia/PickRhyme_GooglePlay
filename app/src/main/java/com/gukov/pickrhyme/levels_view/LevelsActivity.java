package com.gukov.pickrhyme.levels_view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.SharedPreferencesManager;
import com.gukov.pickrhyme.adapter.LevelAdapter;
import com.gukov.pickrhyme.databinding.ActivityLevelsBinding;
import com.gukov.pickrhyme.game_view.GameActivity;
import com.gukov.pickrhyme.model.Model;
import com.gukov.pickrhyme.model.ModelInterface;
import com.gukov.pickrhyme.object.Word;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class LevelsActivity extends AppCompatActivity {

    private ActivityLevelsBinding binding;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = ActivityLevelsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.training_mode));
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        model = new Model(this);
        sPrefManager = SharedPreferencesManager.getInstance(this);
        currentLevel = sPrefManager.getUserLevel();

        showLevels();
    }

    // отображение уровней
    private void showLevels() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.rvLevels.setLayoutManager(layoutManager);
        binding.rvLevels.addItemDecoration(new DividerItemDecoration(binding.rvLevels.getContext(), DividerItemDecoration.VERTICAL));
//        Collections.sort(aList, (w1, w2) -> Integer.compare(w1.getId(), w2.getId()));
        LevelAdapter adapter = new LevelAdapter(this, hideFailedLevels());
        binding.rvLevels.setAdapter(adapter); // устанавливаем адаптер

        adapter.setOnWordClickListener(position -> {
            Word word = adapter.getWords().get(position);
            if (word.getId() < currentLevel) {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("level", word.getId());
                intent.putExtra("mode", "training");
                startActivity(intent);
            }
            else if (word.getId() == currentLevel)
                Toasty.error(this, getString(R.string.complete_this_level_first), Toast.LENGTH_SHORT, false).show();
            else
                Toasty.error(this, getString(R.string.access_is_closed), Toast.LENGTH_SHORT, false).show();
        });
    }

    // скрытие непройденных уровней
    private List<Word> hideFailedLevels() {
        List<Word> list = new ArrayList<>(model.getAllWordsFromDataBase());
        for (int i = currentLevel; i < list.size(); i++)
            list.get(i).setWord("???");
        return list;
    }

}
