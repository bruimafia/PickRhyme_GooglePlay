package com.gukov.pickrhyme.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.SharedPreferencesManager;
import com.gukov.pickrhyme.databinding.FragmentHelpByPushkinBinding;
import com.gukov.pickrhyme.model.Model;
import com.gukov.pickrhyme.model.ModelInterface;

public class HelpByPushkinFragment extends DialogFragment {

    private FragmentHelpByPushkinBinding binding;
    private Context context;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private int hints, level;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHelpByPushkinBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();
//        getDialog().setCanceledOnTouchOutside(false);
//        setCancelable(false);

        model = new Model(context);
        sPrefManager = SharedPreferencesManager.getInstance(context);
        level = getArguments().getInt("level", 1);
        hints = sPrefManager.getUserHints();

        if (hints == 0) {
            binding.tvEmptyHints.setVisibility(View.VISIBLE);
            binding.tvHelpWord.setVisibility(View.GONE);
            binding.btnThanks.setText(R.string.oh_sasha);
        }
        binding.tvKeepCountHints.setText(String.format(getString(R.string.keep_count_hints), Math.max(hints - 1, 0)));
        binding.tvHelpWord.setText(String.format(getString(R.string.quotation_marks), model.getRandomRhymeFromDatabase(level, sPrefManager.getUserRhymes(level))));

        if (hints > 0)
            downUserHints(hints);

        if (sPrefManager.getAppIsFullVersion())
            binding.tvAdvantageFullApp.setVisibility(View.GONE);

        binding.btnThanks.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

    private void downUserHints(int hints) {
        sPrefManager.setUserHints(hints - 1);
    }

}