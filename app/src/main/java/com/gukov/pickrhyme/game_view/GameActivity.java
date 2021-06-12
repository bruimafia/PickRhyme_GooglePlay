package com.gukov.pickrhyme.game_view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.gukov.pickrhyme.FirebaseManager;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.SharedPreferencesManager;
import com.gukov.pickrhyme.adapter.WordAdapter;
import com.gukov.pickrhyme.databinding.ActivityGameBinding;
import com.gukov.pickrhyme.fragment.HelpByPushkinFragment;
import com.gukov.pickrhyme.model.Model;
import com.gukov.pickrhyme.object.Word;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class GameActivity extends AppCompatActivity implements GameContract.View {

    private ActivityGameBinding binding;
    private GameContract.Presenter presenter;
    private SharedPreferencesManager sPrefManager;
    private FirebaseManager firebaseManager;
    private InterstitialAd interstitialAd; // межстраничная реклама
    private int level = 1; // уровень игры
    private String mode = "game"; // режим игры

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        presenter = new GamePresenter(this, this, new Model(this));
        sPrefManager = SharedPreferencesManager.getInstance(this);
        firebaseManager = new FirebaseManager(this);

        try {
            level = getIntent().getExtras().getInt("level");
            mode = getIntent().getExtras().getString("mode");
        } catch (Exception ignored) {
            level = sPrefManager.getUserLevel();
            mode = "game";
        }
        Log.d("INTENT_CHECK", level + " | " + mode);

        if (mode.equals("training"))
            binding.tvPointsForNextLevel.setVisibility(View.GONE);

        presenter.loadSavedData(level);

        initAndShowAdsBanner(); // инициализация и показ баннерной рекламы
        initAdsInterstitial(); // инициализация межстраничной рекламы в приложении
        if (sPrefManager.getAppIsFullVersion())
            binding.adView.setVisibility(View.GONE);

        binding.imgHelp.setOnClickListener(v -> presenter.onHelpByPushkin());
        binding.imgAdd.setOnClickListener(v -> {
            presenter.onEnterWord(binding.etEnter.getText().toString().toLowerCase().replaceAll("\\s", "").replace("ё", "е"), mode);
            binding.etEnter.setText("");
        });
    }

    @Override
    public void showCurrentEnteredRhymes(List<Word> list) {
        if (list.size() == 0) {
            binding.llNoWords.setVisibility(View.VISIBLE);
            binding.rvRhymes.setVisibility(View.GONE);
        } else {
            binding.llNoWords.setVisibility(View.GONE);
            binding.rvRhymes.setVisibility(View.VISIBLE);
        }
        showEnteredRhymes(list);
    }

    // настройка recyclerView
    private void showEnteredRhymes(List<Word> list) {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        binding.rvRhymes.setLayoutManager(layoutManager);
        WordAdapter adapter = new WordAdapter(list);
        binding.rvRhymes.setAdapter(adapter);
    }

    @Override
    public void showLevelValue(String value) {
        binding.tvLevelValue.setText(value);
    }

    @Override
    public void showPointsValue(String value) {
        binding.tvPointsValue.setText(value);
    }

    @Override
    public void showWord(String value) {
        binding.tvWord.setText(value);
        if (value.equals("..."))
            showFinalGameAlertDialog();
    }

    // показываем финальное диалоговое окно, если проходим все уровни игры
    @Override
    public void showFinalGameAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.final_level))
                .setMessage(getString(R.string.final_level_detail))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.save_and_exit),
                        (dialog, id) -> {
                            dialog.cancel();
                            setResult(RESULT_OK, new Intent());
                            presenter.saveResult();
                            finish();
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // показываем диалоговое окно, если просим помощи у Пушкина
    @Override
    public void showHelpByPushkinAlertDialog(int level) {
        HelpByPushkinFragment fragment = new HelpByPushkinFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("level", level);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "dialogHelpByPushkin");
    }

    @Override
    public void showNumberWordsForNextLevel(String value) {
        binding.tvPointsForNextLevel.setText(String.format(getString(R.string.number_words_for_next_level), value));
    }

    @Override
    public void showToastError(String message) {
        Toasty.error(this, message, Toast.LENGTH_SHORT, false).show();
    }

    @Override
    public void showToastInfo(String message) {
        Toasty.info(this, message, Toast.LENGTH_SHORT, false).show();
    }

    @Override
    public void showToastNormal(String message) {
        Toasty.normal(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToastSuccess(String message) {
        Toasty.success(this, message, Toast.LENGTH_SHORT, false).show();
    }

    @Override
    public void showToastWarning(String message) {
        Toasty.warning(this, message, Toast.LENGTH_SHORT, false).show();
    }

    // показ баннерной рекламы
    void initAndShowAdsBanner() {
        AdRequest request = new AdRequest.Builder().build();
        binding.adView.loadAd(request);
    }

    // показ межстраничной рекламы в приложении
    @Override
    public void showAdsInterstitial() {
        if (interstitialAd != null)
            interstitialAd.show(this);
        else
            Log.d("InterstitialAd", "The interstitial ad wasn't ready yet.");
    }

    // инициализация межстраничной рекламы в приложении
    private void initAdsInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // ссылка mInterstitialAd будет равна нулю до тех пор, пока не будет загружено объявление
                Log.d("InterstitialAd", "onAdLoaded");
                GameActivity.this.interstitialAd = interstitialAd;
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // вызывается при отклонении полноэкранного содержимого. Установить значение null, чтобы не показывать ее во второй раз
                        Log.d("InterstitialAd", "The ad was dismissed.");
                        GameActivity.this.interstitialAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // вызывается, когда содержимое полноэкранного режима не отображается. Установить значение null, чтобы не показывать ее во второй раз
                        Log.d("InterstitialAd", "The ad failed to show.");
                        GameActivity.this.interstitialAd = null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d("InterstitialAd", "The ad was shown."); // вызывается при отображении полноэкранного содержимого
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("InterstitialAd", loadAdError.getMessage()); // ошибка инициализации
                interstitialAd = null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.adView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.adView.pause();
        if (mode.equals("game"))
            presenter.saveResult();
        if (mode.equals("training"))
            presenter.updateResult();
        firebaseManager.setDataCloudFirestore(level);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.adView.destroy();
    }
}