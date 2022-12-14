package com.gukov.pickrhyme.game_view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.adapter.WordAdapter;
import com.gukov.pickrhyme.databinding.ActivityGameBinding;
import com.gukov.pickrhyme.dialog.HelpByPushkinDialog;
import com.gukov.pickrhyme.model.Model;
import com.gukov.pickrhyme.object.Word;
import com.gukov.pickrhyme.util.FirebaseManager;
import com.gukov.pickrhyme.util.SharedPreferencesManager;
import com.yandex.mobile.ads.banner.AdSize;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class GameActivity extends AppCompatActivity implements GameContract.View {

    private final String TAG = "ADS";

    private ActivityGameBinding binding;
    private GameContract.Presenter presenter;
    private SharedPreferencesManager sPrefManager;
    private FirebaseManager firebaseManager;
    private InterstitialAd yandexInterstitialAd; // межстраничная реклама Yandex
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

        //initAndShowAdsBanner(); // инициализация и показ баннерной рекламы
        //initYandexAdsInterstitial(); // инициализация межстраничной рекламы Yandex в приложении
        if (!sPrefManager.getIsFullVersion()) {
            binding.bannerAdViewYandex.setAdUnitId(getString(R.string.ads_yandex_bannerAd_unitId));
            binding.bannerAdViewYandex.setAdSize(AdSize.stickySize(getResources().getDisplayMetrics().widthPixels));
            initAndShowAdsBanner();
        }

        binding.imgHelp.setOnClickListener(v -> presenter.onHelpByPushkin());
        binding.imgAdd.setOnClickListener(v -> {
            presenter.onEnterWord(binding.etEnter.getText().toString().toLowerCase().replaceAll("\\s", "").replace("ё", "е"), mode);
            binding.etEnter.setText("");
        });

        if (!sPrefManager.getIsFullVersion() && mode.equals("training"))
            showAdsInterstitial();

        requestReview();
    }

    // окно выставления оценки и отзыва
    private void requestReview() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(t -> {
                    //sPrefManager.setPlayRating(true);
                });
            }
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
            showGameEndDialog();
    }

    // показываем финальное диалоговое окно, если проходим все уровни игры
    @Override
    public void showGameEndDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(R.layout.dialog_game_end);
        dialog.setCancelable(false);

        Button ok = dialog.findViewById(R.id.btn_allRhymes);
        ok.setOnClickListener(view -> {
            presenter.saveResult();
            finish();
        });

        dialog.show();
    }

    // показываем диалоговое окно, если просим помощи у Пушкина
    @Override
    public void showHelpByPushkinDialog(int level) {
        HelpByPushkinDialog dialog = new HelpByPushkinDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("level", level);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "BottomSheetDialogHelpByPushkin");
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

    // показ баннерной рекламы Yandex
    void initAndShowAdsBanner() {
        final AdRequest adRequest = new AdRequest.Builder().build();
        binding.bannerAdViewYandex.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                initAndShowAdsBanner();
            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onLeftApplication() {

            }

            @Override
            public void onReturnedToApplication() {

            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {

            }
        });
        binding.bannerAdViewYandex.loadAd(adRequest);
    }

    // показ межстраничной рекламы в приложении
    @Override
    public void showAdsInterstitial() {
        initYandexAdsInterstitial();
//        if (yandexInterstitialAd != null && yandexInterstitialAd.isLoaded())
//            yandexInterstitialAd.show();
//        else
//            Log.d(TAG, "Yandex: The interstitial ad wasn't ready yet.");
    }

    // инициализация межстраничной рекламы Яндекс в приложении
    private void initYandexAdsInterstitial() {
        yandexInterstitialAd = new InterstitialAd(this);
        yandexInterstitialAd.setAdUnitId(getString(R.string.ads_yandex_interstitialAd_unitId));
        yandexInterstitialAd.loadAd(new AdRequest.Builder().build());
        yandexInterstitialAd.setInterstitialAdEventListener(new InterstitialAdEventListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Yandex: onAdLoaded");
                yandexInterstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                Log.d(TAG, "Yandex (onAdFailedToLoad): " + adRequestError.getDescription());
                yandexInterstitialAd = null;
                initYandexAdsInterstitial();
            }

            @Override
            public void onAdShown() {
                Log.d(TAG, "Yandex: onAdShown");
            }

            @Override
            public void onAdDismissed() {
                Log.d(TAG, "Yandex: onAdDismissed");
                yandexInterstitialAd = null;
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Yandex: onAdClicked");
            }

            @Override
            public void onLeftApplication() {
                Log.d(TAG, "Yandex: onLeftApplication");
            }

            @Override
            public void onReturnedToApplication() {
                Log.d(TAG, "Yandex: onReturnedToApplication");
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
                Log.d(TAG, "Yandex: onImpression");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mode.equals("game"))
            presenter.saveResult();
        if (mode.equals("training"))
            presenter.updateResult();
        firebaseManager.setDataCloudFirestore(level);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.bannerAdViewYandex.destroy();
    }
}