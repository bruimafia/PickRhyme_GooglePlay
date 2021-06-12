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
        // Google Mobile Ads SDK
        MobileAds.initialize(this);
//        MobileAds.initialize(this, initializationStatus -> Toasty.normal(ApplicationClass.this, "Модуль рекламы загружен", Toast.LENGTH_LONG).show());

        // OneSignal Initialization
        // Enable verbose OneSignal logging to debug issues if needed / Включите подробное ведение журнала одного сигнала для отладки проблем, если это необходимо
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.oneSignal_api_key));

        // Создание расширенной конфигурации библиотеки
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(getString(R.string.appMetrica_api_key)).build();
        // Инициализация AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), config);
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(this);
    }

}