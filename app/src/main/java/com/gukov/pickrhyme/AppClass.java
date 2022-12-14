package com.gukov.pickrhyme;

import android.app.Application;
import android.util.Log;

import com.yandex.mobile.ads.common.MobileAds;
import com.onesignal.OneSignal;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class AppClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Yandex Mobile Ads
        MobileAds.initialize(this, () -> Log.d("ADS", "Yandex: SDK initialized"));

        // Yandex AppMetrica
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(getString(R.string.appMetrica_yandex_api_key)).build();
        YandexMetrica.activate(getApplicationContext(), config);
        YandexMetrica.enableActivityAutoTracking(this);

        // OneSignal Initialization
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.oneSignal_app_id));
    }

}