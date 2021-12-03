package com.gukov.pickrhyme;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.onesignal.OneSignal;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class AppClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Google Mobile Ads
        MobileAds.initialize(this);
//        MobileAds.initialize(this, initializationStatus -> Toasty.normal(ApplicationClass.this, "Модуль рекламы загружен", Toast.LENGTH_LONG).show());

        // AppMetrica
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(getString(R.string.appMetrica_api_key)).build();
        YandexMetrica.activate(getApplicationContext(), config);
        YandexMetrica.enableActivityAutoTracking(this);

        // OneSignal
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.oneSignal_app_id));
    }

}