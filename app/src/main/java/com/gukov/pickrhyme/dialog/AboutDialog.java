package com.gukov.pickrhyme.dialog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.gukov.pickrhyme.BuildConfig;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.util.SharedPreferencesManager;
import com.gukov.pickrhyme.databinding.DialogAboutBinding;

public class AboutDialog extends DialogFragment {

    private static final String VK_ID = "31223368";

    private DialogAboutBinding binding;
    private Context context;
    private SharedPreferencesManager sPrefManager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogAboutBinding.inflate(inflater, container, false);
        context = getActivity().getApplicationContext();
        sPrefManager = SharedPreferencesManager.getInstance(context);

        String version = sPrefManager.getIsFullVersion() ? "pro" : "";
        binding.tvAppVersion.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME, version, String.valueOf(BuildConfig.VERSION_CODE)));

        // перейти по ссылке в вк
        binding.imgVkLink.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("linkedin://profile/%s", VK_ID))));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/id" + VK_ID)));
            }
        });

        // перейти по ссылке в магазин приложений
        binding.imgGoogleplayLink.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
            }
        });

        // перейти на политику конфиденциальности
        binding.tvPrivacyPolicyLink.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link)))));

        return binding.getRoot();
    }

}