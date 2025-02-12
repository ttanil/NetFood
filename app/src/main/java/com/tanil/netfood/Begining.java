package com.tanil.netfood;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.MobileAds;

public class Begining extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_begining);
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
            getWindow().setNavigationBarColor(Color.TRANSPARENT); // Navigation bar rengi
        }

        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();
/*
        // Lottie Animation View
        LottieAnimationView lottieAnimation = findViewById(R.id.lottieAnimation);
        // Animasyon hızını düşür (örneğin, yarı hızda oynatmak için 0.5)
        lottieAnimation.setSpeed(0.45f);
        // Animasyonu başlat
        lottieAnimation.setVisibility(View.VISIBLE);
        lottieAnimation.playAnimation();

 */
        View magnifyingGlassView = findViewById(R.id.magnifyingGlassView);
        View packageView = findViewById(R.id.packageView);
        View safeFoodView = findViewById(R.id.safeFoodView);

        // magnifyingGlassView için ScaleAnimation (küçülme -> büyüme)
        ScaleAnimation magnifyingGlassAnimation = new ScaleAnimation(
                1.5f, 1f, // X ekseninde başlangıç ve bitiş ölçeği (büyükten küçüğe)
                1.5f, 1f, // Y ekseninde başlangıç ve bitiş ölçeği (büyükten küçüğe)
                Animation.RELATIVE_TO_SELF, 0.5f, // X ekseninde pivot noktası (merkez)
                Animation.RELATIVE_TO_SELF, 0.5f  // Y ekseninde pivot noktası (merkez)
        );
        magnifyingGlassAnimation.setDuration(3000); // Animasyon süresi
        magnifyingGlassAnimation.setRepeatCount(Animation.INFINITE); // Sonsuz döngü
        magnifyingGlassAnimation.setRepeatMode(Animation.REVERSE); // Tersine oynatma

        // packageView için ScaleAnimation (küçükten -> büyüğe)
        ScaleAnimation packageViewAnimation = new ScaleAnimation(
                1f, 1.6f, // X ekseninde başlangıç ve bitiş ölçeği (küçükten büyüğe)
                1f, 1.6f, // Y ekseninde başlangıç ve bitiş ölçeği (küçükten büyüğe)
                Animation.RELATIVE_TO_SELF, 0.5f, // X ekseninde pivot noktası (merkez)
                Animation.RELATIVE_TO_SELF, 0.5f  // Y ekseninde pivot noktası (merkez)
        );
        packageViewAnimation.setDuration(3000); // Animasyon süresi
        packageViewAnimation.setRepeatCount(Animation.INFINITE); // Sonsuz döngü
        packageViewAnimation.setRepeatMode(Animation.REVERSE); // Tersine oynatma

        // safeFoodView için ScaleAnimation (küçükten -> büyüğe)
        ScaleAnimation safeFoodViewAnimation = new ScaleAnimation(
                1f, 2.3f, // X ekseninde başlangıç ve bitiş ölçeği (küçükten büyüğe)
                1f, 2.3f, // Y ekseninde başlangıç ve bitiş ölçeği (küçükten büyüğe)
                Animation.RELATIVE_TO_SELF, 0.5f, // X ekseninde pivot noktası (merkez)
                Animation.RELATIVE_TO_SELF, 0.5f  // Y ekseninde pivot noktası (merkez)
        );
        safeFoodViewAnimation.setDuration(3000); // Animasyon süresi
        safeFoodViewAnimation.setRepeatCount(Animation.INFINITE); // Sonsuz döngü
        safeFoodViewAnimation.setRepeatMode(Animation.REVERSE); // Tersine oynatma

        // Animasyonları başlat
        magnifyingGlassView.startAnimation(magnifyingGlassAnimation);
        packageView.startAnimation(packageViewAnimation);
        safeFoodView.startAnimation(safeFoodViewAnimation);

        new Handler().postDelayed(() -> {
            // Animasyonu durdur ve görünürlüğünü gizle
            //lottieAnimation.cancelAnimation();
            //lottieAnimation.setVisibility(View.GONE);

            // MainActivity'ye geçiş yap
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }, 4100);
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
}