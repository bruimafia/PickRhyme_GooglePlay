package com.gukov.pickrhyme.main_view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gukov.pickrhyme.BuildConfig;
import com.gukov.pickrhyme.FirebaseManager;
import com.gukov.pickrhyme.GoogleAccountManager;
import com.gukov.pickrhyme.R;
import com.gukov.pickrhyme.SharedPreferencesManager;
import com.gukov.pickrhyme.databinding.ActivityMainBinding;
import com.gukov.pickrhyme.fragment.AboutFragment;
import com.gukov.pickrhyme.fragment.BuyFullFragment;
import com.gukov.pickrhyme.fragment.SuggestRhymeFragment;
import com.gukov.pickrhyme.game_view.GameActivity;
import com.gukov.pickrhyme.levels_view.LevelsActivity;
import com.gukov.pickrhyme.model.Model;

import java.io.File;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private static final String PACKAGE_NAME = "com.gukov.pickrhyme";
    private static final int RC_LEADERBOARD_UI = 9004; // код запроса для вызова таблицы лидеров
    private static final int RC_SIGN_IN = 9001; // код запроса для вызова взаимодействия с пользователем

    private ActivityMainBinding binding;
    private MainContract.Presenter presenter;
    private SharedPreferencesManager sPrefManager;
    private GoogleAccountManager googleAccountManager;
    private FirebaseManager firebaseManager;
    private RewardedAd mRewardedAd; // реклама с вознаграждением

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        presenter = new MainPresenter(this, this, new Model(this));
        sPrefManager = SharedPreferencesManager.getInstance(this);
        googleAccountManager = new GoogleAccountManager(this, this);
        firebaseManager = new FirebaseManager(this);

        migratingToNewVersion();

        presenter.checkPurchased(googleAccountManager.isPurchased()); // обычная версия приложения, с рекламой
//        presenter.checkPurchased(true); // полная версия приложения, не покупая его

        checkUpdateAvailability();

        binding.btnSignIn.setOnClickListener(v -> presenter.onSignInClicked());
        binding.btnPlay.setOnClickListener(v -> presenter.onPlayGameClicked());
        binding.btnTraining.setOnClickListener(v -> presenter.onTrainingGameClicked());
        binding.btnLeaderboards.setOnClickListener(v -> presenter.openLeaderboardsClicked());
        binding.btnGetHintForViewAds.setOnClickListener(v -> presenter.onViewAdsForHint());
        binding.btnSuggestRhyme.setOnClickListener(v -> presenter.openSuggestRhymeClicked());
        binding.btnReset.setOnClickListener(v -> presenter.onResetGameClicked());
        binding.btnSignOut.setOnClickListener(v -> presenter.onSignOutClicked());
        binding.tvBuyFullApp.setOnClickListener(v -> presenter.onBuyFullAppClicked());
        binding.tvAbout.setOnClickListener(v -> presenter.openAboutAppClicked());
        binding.tvPrivacyPolicy.setOnClickListener(v -> presenter.linkPrivacyPolicyClicked());
    }

    private void migratingToNewVersion() {
        File f = new File(getApplicationInfo().dataDir + "/shared_prefs/result.xml");
        if (f.exists()) {
            Log.d("CheckVersion", "old (2.x)");
            SharedPreferences sp = getApplicationContext().getSharedPreferences("result", Context.MODE_PRIVATE);
            sPrefManager.setUserLevel(Integer.parseInt(sp.getString("POINTS", "0")) / 10 + 1);
            sPrefManager.setUserPoints(Integer.parseInt(sp.getString("POINTS", "0")));
            sPrefManager.setUserHints(sp.getInt("HINTS", 2));
            sp.edit().clear().apply();
            f.delete();
        } else Log.d("CheckVersion", "new (3.x)");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            googleAccountManager.saveSignIn(data);
            firebaseManager.checkDataCloudFirestore();
            presenter.loadSavedData();
        }
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
    public void showPlayerName(String playerName) {
        binding.tvPlayerName.setText(String.format(getString(R.string.welcome_player), playerName));
    }

    @Override
    public void showMessageBuyFullApp(boolean isPurchased) {
        binding.tvBuyFullApp.setVisibility(isPurchased ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSignIn() {
        googleAccountManager.startSignInIntent();
    }

    @Override
    public void onPlayGame() {
        startActivity(new Intent(this, GameActivity.class));
    }

    @Override
    public void onTrainingGame() {
        startActivity(new Intent(this, LevelsActivity.class));
    }

    @Override
    public void openLeaderboards() {
        googleAccountManager.getAllLeaderboardsIntent(presenter.isNetworkOnline(this));
        if (!googleAccountManager.getAllLeaderboardsIntent(presenter.isNetworkOnline(this)))
            showSnackbar("Кажется, отсутствует интернет-соединение");
    }

    @Override
    public void viewAdsForHint() {
        if (mRewardedAd != null) {
            mRewardedAd.show(this, rewardItem -> {
                // здесь происходит возаграждение
                presenter.upUserHintsForViewAds();
                showToastInfo("Ура! Вы заработали одну подсказку, теперь у Вас их: " + sPrefManager.getUserHints());
                mRewardedAd = null;
            });
        } else
            showToastInfo("Ищем рекламу... Подождите несколько секунд и повторите попытку");
    }

    @Override
    public void openSuggestRhyme() {
        SuggestRhymeFragment fragment = new SuggestRhymeFragment();
        fragment.show(getSupportFragmentManager(), "dialogSuggestRhyme");
    }

    @Override
    public void onResetGame() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.are_you_sure));
        if (googleAccountManager.isSignedIn())
            dialog.setMessage(getString(R.string.go_to_play_game_for_reset_game_online));
        else
            dialog.setMessage(getString(R.string.go_to_play_game_for_reset_game));
        dialog.setPositiveButton(getString(R.string.reset), (d, which) -> {
            firebaseManager.clearData();
            sPrefManager.clearAll();
            presenter.loadSavedData();
            d.dismiss();
        })
                .setNegativeButton(getString(R.string.cancel), (d, id) -> d.cancel());
        dialog.create();
        dialog.show();
    }

    @Override
    public void onSignOut() {
        if (googleAccountManager.isSignedIn())
            googleAccountManager.signOut();
        presenter.loadSavedData();
    }

    @Override
    public void onBuyFullApp() {
        BuyFullFragment fragment = new BuyFullFragment();
        fragment.show(getSupportFragmentManager(), "dialogBuyFull");
    }

    @Override
    public void openAboutApp() {
        AboutFragment fragment = new AboutFragment();
        fragment.show(getSupportFragmentManager(), "dialogAbout");
    }

    @Override
    public void linkPrivacyPolicy() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link))));
    }

    @Override
    public void showHandleExceptionDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void showSomeIntent(Intent intent, int code) {
        startActivityForResult(intent, code);
    }

    @Override
    public void showMainMenu() {
        if (!googleAccountManager.isSignedIn()) {
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.tvPlayerName.setVisibility(View.GONE);
            binding.btnLeaderboards.setVisibility(View.GONE);
            binding.btnSignOut.setVisibility(View.GONE);
        } else {
            Log.d("GoogleAccount", "Пользователь уже авторизован");
            binding.btnSignIn.setVisibility(View.GONE);
            binding.tvPlayerName.setVisibility(View.VISIBLE);
            binding.btnLeaderboards.setVisibility(View.VISIBLE);
            binding.btnSignOut.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showExtraMenu() {
        if (sPrefManager.getUserLevel() <= 1) {
            binding.btnTraining.setVisibility(View.GONE);
            binding.btnReset.setVisibility(View.GONE);
        } else {
            binding.btnTraining.setVisibility(View.VISIBLE);
            binding.btnReset.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(binding.constraintLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showToastInfo(String message) {
        Toasty.info(this, message, Toast.LENGTH_LONG, false).show();
    }

    // инициализация рекламы с вознаграждением в приложении
    public void initAdsRewarded() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.rewarded_ad_unit_id), adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("RewardedAd", loadAdError.getMessage()); // ошибка инициализации
                mRewardedAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d("RewardedAd", "Ad was shown."); // вызывается при показе объявления
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.d("RewardedAd", "Ad failed to show."); // вызывается, когда объявление не отображается
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // вызывается, когда объявление отклоняется. Установить значение null, чтобы не показывать объявление во второй раз
                        Log.d("RewardedAd", "Ad was dismissed.");
                        mRewardedAd = null;
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleAccountManager.signInSilently();
        presenter.loadSavedData();
        firebaseManager.checkDataCloudFirestore();
        presenter.loadSavedData();
        initAdsRewarded(); // инициализация рекламы с вознаграждением в приложении
    }

    @Override
    protected void onPause() {
        super.onPause();
        googleAccountManager.updateLeaderboards(sPrefManager.getUserPoints(), sPrefManager.getUserLevel());
    }

    @Override
    protected void onDestroy() {
        googleAccountManager.billingProcessorDestroy();
        super.onDestroy();
    }

    // проверка обновлений
    private void checkUpdateAvailability() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            8);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
