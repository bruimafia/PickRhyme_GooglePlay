package com.gukov.pickrhyme.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.gukov.pickrhyme.FirebaseManager;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.SharedPreferencesManager;
import com.gukov.pickrhyme.databinding.FragmentSuggestRhymeBinding;
import com.gukov.pickrhyme.model.Model;
import com.gukov.pickrhyme.model.ModelInterface;
import com.gukov.pickrhyme.object.Word;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class SuggestRhymeFragment extends DialogFragment {

    private FragmentSuggestRhymeBinding binding;
    private Context context;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private FirebaseManager firebaseManager;
    private String wordLevel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSuggestRhymeBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();

        model = new Model(context);
        sPrefManager = SharedPreferencesManager.getInstance(context);
        firebaseManager = new FirebaseManager(context);

        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(context, R.layout.word_in_spinner, getOpenLevelsListString(model.getAllWordsFromDataBase()));
        binding.spinner.setAdapter(userAdapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wordLevel = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.btnSend.setOnClickListener(v -> {
            if (!binding.etSuggestRhyme.getText().toString().equals("")) {
                firebaseManager.sendSuggestRhymeToCloudFirestore(wordLevel, binding.etSuggestRhyme.getText().toString());
                Toasty.info(context, getString(R.string.suggest_success), Toast.LENGTH_SHORT, false).show();
                dismiss();
            }
        });

        return binding.getRoot();
    }

    private List<String> getOpenLevelsListString(List<Word> listAllWords) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < sPrefManager.getUserLevel(); i++)
            list.add(listAllWords.get(i).getWord());
        return list;
    }
}
