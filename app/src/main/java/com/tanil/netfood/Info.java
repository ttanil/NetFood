package com.tanil.netfood;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

public class Info extends AppCompatActivity {

    private AdView adViewShowImage;
    private FrameLayout adContainerViewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.BLACK); // Durum çubuğu rengi
        }
        // Android 11 ve üzeri için yazıları beyaz yapmayı zorla
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(Color.BLACK); // Navigation bar rengi
        }
        adContainerViewInfo = findViewById(R.id.adContainerViewInfo);
        // Reklam görünümünü oluştur
        adViewShowImage = new AdView(this);
        //adViewShowImage.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // Test Ad Unit ID
        adViewShowImage.setAdUnitId("ca-app-pub-1711674578900136/9008344090");
        adContainerViewInfo.addView(adViewShowImage);
        // Dinamik reklam boyutunu ayarla ve reklamı yükle
        loadBannerAd();

        TextView infoText = findViewById(R.id.infoText);
        CardView resultsCardInfo = findViewById(R.id.resultsCardInfo);
        TextView closeButtonCardInfo = findViewById(R.id.closeButtonCardInfo);

        infoText.setOnClickListener(v -> {
            resultsCardInfo.setVisibility(View.VISIBLE);
        });

        closeButtonCardInfo.setOnClickListener(v -> {
            resultsCardInfo.setVisibility(View.GONE);
        });

    }
    // transparan systembar metodu
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    // Dinamik reklam boyutunu hesaplayan ve reklamı yükleyen fonksiyon
    private void loadBannerAd() {
        AdSize adSize = getAdSize(); // Dinamik reklam boyutunu al
        adViewShowImage.setAdSize(adSize); // Reklam boyutunu ayarla

        // Reklam isteği oluştur ve yükle
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewShowImage.loadAd(adRequest);

        // Reklam dinleyicisi ekle
        adViewShowImage.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                System.out.println("yüklendi");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                System.out.println("yüklenemedi");
            }
        });
    }
    // Dinamik reklam boyutunu hesaplayan fonksiyon
    private AdSize getAdSize() {
        // Ekran genişliğini piksel cinsinden al
        int adWidthPixels;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 ve üzeri için ekran genişliğini al
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            adWidthPixels = windowMetrics.getBounds().width();
        } else {
            // Daha eski Android sürümleri için ekran genişliğini al
            adWidthPixels = displayMetrics.widthPixels;
        }

        // Piksel genişliğini dp'ye çevir
        float density = displayMetrics.density;
        int adWidth = (int) (adWidthPixels / density);

        // Dinamik reklam boyutunu döndür
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
}