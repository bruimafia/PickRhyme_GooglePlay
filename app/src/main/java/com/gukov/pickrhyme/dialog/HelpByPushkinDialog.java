package com.gukov.pickrhyme.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.databinding.DialogHelpByPushkinBinding;
import com.gukov.pickrhyme.model.Model;
import com.gukov.pickrhyme.model.ModelInterface;
import com.gukov.pickrhyme.util.SharedPreferencesManager;

public class HelpByPushkinDialog extends BottomSheetDialogFragment {

    private DialogHelpByPushkinBinding binding;
    private Context context;
    private ModelInterface model;
    private SharedPreferencesManager sPrefManager;
    private int hints, level;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogHelpByPushkinBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();

        model = new Model(context);
        sPrefManager = SharedPreferencesManager.getInstance(context);
        if (getArguments() != null)
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

        if (sPrefManager.getIsFullVersion())
            binding.tvAdvantageFullApp.setVisibility(View.GONE);

        binding.btnThanks.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

    private void downUserHints(int hints) {
        sPrefManager.setUserHints(hints - 1);
    }

}
