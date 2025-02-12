package com.tanil.netfood;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AdView adViewShowImage;
    private FrameLayout adContainerView;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ImageView capturedImageView, captureButton, retakeButton;

    private RewardedAd rewardedAd;
    private InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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

        adContainerView = findViewById(R.id.adContainerView);
        // Reklam görünümünü oluştur
        adViewShowImage = new AdView(this);
        //adViewShowImage.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // Test Ad Unit ID
        adViewShowImage.setAdUnitId("ca-app-pub-1711674578900136/9008344090");
        adContainerView.addView(adViewShowImage);
        // Dinamik reklam boyutunu ayarla ve reklamı yükle
        loadBannerAd();
        // Geçiş reklamını yükle
        loadInterstitialAd();
        // Ödül reklamını yükle
        loadRewardedAd();

        previewView = findViewById(R.id.previewView);
        capturedImageView = findViewById(R.id.capturedImageView);
        captureButton = findViewById(R.id.captureButton);
        retakeButton = findViewById(R.id.retakeButton);
        TextView closeButtonCard = findViewById(R.id.closeButtonCard);
        View resultsCard = findViewById(R.id.resultsCard);
        CardView backgroundCard = findViewById(R.id.backgroundCard);
        CardView previewCard = findViewById(R.id.previewCard);
        View dots = findViewById(R.id.dots);

        // Kamera izni kontrolü
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera(); // İzin verilmişse kamerayı başlat
        }


        // Çekilecek alanın boyutlarını belirle
        int overlayWidth = 800; // Çekilecek alanın genişliği (örnek)
        int overlayHeight = 600; // Çekilecek alanın yüksekliği (örnek)

        // Overlay'in boyutlarını ayarla
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                overlayWidth,
                overlayHeight
        );
        params.gravity = Gravity.CENTER; // Ortaya yerleştir

        // Fotoğraf çekme butonu
        captureButton.setOnClickListener(v -> takePhoto());


        retakeButton.setOnClickListener(v -> {
            // Çekilen görüntüyü gizle ve kamera önizlemesini tekrar göster
            capturedImageView.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            retakeButton.setVisibility(View.GONE);
            captureButton.setVisibility(View.VISIBLE);

            startCamera();
        });

        closeButtonCard.setOnClickListener(v -> {
            resultsCard.setVisibility(View.GONE);
            backgroundCard.setVisibility(View.VISIBLE);
            previewCard.setVisibility(View.VISIBLE);
            capturedImageView.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            retakeButton.setVisibility(View.GONE);
            captureButton.setVisibility(View.VISIBLE);
            startCamera();
        });

        dots.setOnClickListener(v -> {
            Intent intent = new Intent(this, Info.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        //USDAFoodDataFetcher foodDataFetcher = new USDAFoodDataFetcher(this);
        //foodDataFetcher.searchEAdditive("Tamek");

        //EAdditiveDetailsFetcher fetcher = new EAdditiveDetailsFetcher(this);
        //fetcher.fetchEAdditiveDetails("E120");
/*
        String result = getMatchesAsString("Bu ürün E174, Titanyum Beyazı ve Demir Boyası içeriyor E120");
        //E171 : Titanyum Beyazı, E172 : Demir Boyası, E174 : -, E120 : -
        System.out.println(processAdditives(result));
        // Titanyum Beyazı:renklendiriciler:helal_kabul_edilen_katkilar, Demir Boyası:renklendiriciler:helal_kabul_edilen_katkilar, E174:renklendiriciler:saglik_icin_tehlikeli_katkilar, E120:renklendiriciler:kesin_hayvan_cogunlukla_domuz_kokenli_katkilar,
*/

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

    private void loadInterstitialAd() {
        // InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", new AdRequest.Builder().build(), // test
        InterstitialAd.load(this, "ca-app-pub-1711674578900136/7564009290", new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        Log.d("InterstitialAd", "Reklam yüklendi.");

                        // Reklam olaylarını dinlemek için bir listener ekleyin
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Kullanıcı reklamı kapattığında burası çalışır
                                //System.out.println("Reklam kapatıldı.");
                                onAdClosed();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                // Reklam gösterilemediğinde burası çalışır
                                Log.d("InterstitialAd", "Reklam gösterilemedi: " + adError.getMessage());
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Reklam gösterildiğinde burası çalışır
                                System.out.println("Reklam gösteriliyor.");
                                interstitialAd = null; // Reklam gösterildikten sonra referansı temizleyin
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d("InterstitialAd", "Reklam yüklenemedi: " + loadAdError.getMessage());
                        interstitialAd = null;
                    }
                });
    }

    // Reklam kapandıktan sonra yapılacak işlemler
    private void onAdClosed() {
        // SharedPreferences'a erişim
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Mevcut sayaç değerini al
        int currentCount = sharedPreferences.getInt("reviewButtonClickCount", 0);

        currentCount++;

        // Yeni değeri SharedPreferences'a kaydet
        editor.putInt("reviewButtonClickCount", currentCount);
        editor.apply();
    }

    private void loadRewardedAd() {
        RewardedAd.load(this, "ca-app-pub-1711674578900136/7372437603", new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d("RewardedAd", "Ödül reklamı yüklendi.");
                        System.out.println("Ödül reklamı yüklendi.");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        Log.d("RewardedAd", "Ödül reklamı yüklenemedi: " + adError.getMessage());
                        rewardedAd = null;
                    }
                });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            return;
        }

        captureButton.setVisibility(View.GONE); // Fotoğraf çekme işlemi sırasında butonu devre dışı bırak
        retakeButton.setVisibility(View.VISIBLE);

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                runOnUiThread(() -> {
                    // ImageProxy'yi Bitmap'e dönüştür
                    Bitmap bitmap = imageProxyToBitmap(image);

                    // Bitmap'in ortasından alanı kırp
                    Bitmap croppedBitmap = cropBitmap(bitmap, 1600,1200);

                    // Kırpılmış Bitmap'i ImageView'de göster
                    capturedImageView.setImageBitmap(croppedBitmap);

                    // Kamera önizlemesini gizle ve çekilen görüntüyü göster
                    previewView.setVisibility(View.GONE);
                    capturedImageView.setVisibility(View.VISIBLE);

                    // Metin tanıma işlemini başlat
                    recognizeTextFromImage(croppedBitmap);
                });

                // ImageProxy'yi kapatmayı unutmayın
                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraX", "Fotoğraf yakalama başarısız: " + exception.getMessage(), exception);
            }
        });
    }


    private Bitmap imageProxyToBitmap(ImageProxy image) {
        // ByteBuffer'dan byte dizisi oluştur
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        // Byte dizisini Bitmap'e dönüştür
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Rotasyon bilgisini al
        float rotationDegrees = image.getImageInfo().getRotationDegrees();

        // Bitmap'i döndür
        return rotateBitmap(bitmap, rotationDegrees);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, float rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Bitmap'i kırpmak için metot
    private Bitmap cropBitmap(Bitmap bitmap, int cropWidthDp, int cropHeightDp) {
        // Bitmap'in boyutlarını al
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // DP'yi piksele dönüştür (ekran yoğunluğuna göre)
        float density = getResources().getDisplayMetrics().density;
        int cropWidthPx = (int) (cropWidthDp * density); // dp -> px
        int cropHeightPx = (int) (cropHeightDp * density); // dp -> px

        // Orta noktayı hesapla
        int startX = (bitmapWidth - cropWidthPx) / 2;
        int startY = (bitmapHeight - cropHeightPx) / 2;

        // Bitmap'in sınırlarını kontrol et (kırpma alanı bitmap'in dışına çıkmasın)
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;
        if (startX + cropWidthPx > bitmapWidth) cropWidthPx = bitmapWidth - startX;
        if (startY + cropHeightPx > bitmapHeight) cropHeightPx = bitmapHeight - startY;

        // Bitmap'i kırp
        return Bitmap.createBitmap(bitmap, startX, startY, cropWidthPx, cropHeightPx);
    }

    private void recognizeTextFromImage(Bitmap bitmap) {
        // ML Kit için InputImage oluştur
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        // TextRecognizer oluştur (Latin tabanlı diller için)
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Metin tanıma işlemini başlat
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    // Başarıyla tanınan metni işleyin
                    String recognizedText = visionText.getText();
                    if(recognizedText == null || Objects.equals(recognizedText,"")){
                        recognizedText = "Taranan alanda yazılı metin tespit edilemedi!";
                    }
                    // Metni ekranda bir Dialog ile göster
                    showRecognizedTextBottomSheet(recognizedText);
                    //Toast.makeText(MainActivity.this, recognizedText, Toast.LENGTH_LONG).show();
                    Log.d("MLKit", "Tanınan Metin: " + recognizedText);
                })
                .addOnFailureListener(e -> {
                    // Hata durumunda işlem yapın
                    Log.e("MLKit", "Metin tanıma başarısız: " + e.getMessage());
                });
    }

    private void showRecognizedTextBottomSheet(String recognizedText) {
        // BottomSheetDialog oluştur
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.dialog_recognized_text);

        // TextView ve Button'u bağla
        TextView textView = bottomSheetDialog.findViewById(R.id.recognizedTextView);
        Button closeButton = bottomSheetDialog.findViewById(R.id.closeButton);
        Button reviewButton = bottomSheetDialog.findViewById(R.id.reviewButton);

        if(recognizedText == null || Objects.equals(recognizedText,"Taranan alanda yazılı metin tespit edilemedi!")){
            closeButton.setVisibility(View.VISIBLE);
            reviewButton.setVisibility(View.GONE);
        } else{
            closeButton.setVisibility(View.GONE);
            reviewButton.setVisibility(View.VISIBLE);
        }

        // Metni ayarla
        if (textView != null) {
            textView.setText(recognizedText);
        }

        // "Tamam" butonuna tıklama işlemi
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        // "İncele" butonuna tıklama işlemi
        if (reviewButton != null) {
            reviewButton.setOnClickListener(v -> {
                // Sayaç değerini güncelle ve SharedPreferences'a kaydet
                int clickCount = incrementClickCount();
                System.out.println("sayaç "+clickCount);
                // Sayaç değerine göre reklam göster
                if (clickCount % 3 == 0) {
                    showInterstitialAd(); // Ödül reklamı göster
                }
                //System.out.println("tt "+recognizedText);
                // Sonuçları göster
                String result = getMatchesAsString(recognizedText);
                System.out.println("tt "+result);
                String processString = processAdditives(result);
                showResults(processString);
                //System.out.println(processString);
                // BottomSheetDialog'u kapat
                bottomSheetDialog.dismiss();
            });
        }

        // BottomSheetDialog'u göster
        bottomSheetDialog.show();
    }

    private int incrementClickCount() {
        // SharedPreferences'a erişim
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Mevcut sayaç değerini al
        int currentCount = sharedPreferences.getInt("reviewButtonClickCount", 0);

        if(currentCount %3 !=0){
            // Sayaç değerini artır
            currentCount++;

            // Yeni değeri SharedPreferences'a kaydet
            editor.putInt("reviewButtonClickCount", currentCount);
            editor.apply(); // Değişiklikleri uygula
        }

        // Güncel sayaç değerini döndür
        return currentCount;
    }

    private String getMatchesAsString(String inputText) {
        Compare compare = new Compare(this);

        // JSON'daki kelime ya da kelime gruplarını ve E kodlarını metin içinde ara
        String result = compare.findMatchesInText(inputText);

        // Eğer eşleşme yoksa bir mesaj döndür
        if (result == null || result.isEmpty()) {
            return "Eşleşme bulunamadı.";
        }

        // Sonucu döndür
        return result;
    }

    private String processAdditives(String text) {
        Compare compare = new Compare(MainActivity.this);

        // Gelen stringi ", " ile ayırarak katkı maddelerini al
        String[] additives = text.split(", "); // ", " ile ayır

        // Sonuçları saklamak için bir StringBuilder oluştur
        StringBuilder results = new StringBuilder();

        try {
            // Her bir katkı maddesi kodunu kontrol et
            for (String additive : additives) {
                // Eğer katkı maddesi kodu boş değilse kontrol et
                if (!additive.isEmpty()) {
                    // E kodu ve isim ayrımı yap
                    String[] parts = additive.split(" : "); // " : " ile ayır
                    String eCode = parts[0].trim(); // E kodu
                    String name = parts.length > 1 ? parts[1].trim() : null; // İsim varsa al

                    // findAdditiveCategory metodunu çağır ve sonucu al
                    String result = compare.findAdditiveCategory(eCode);

                    // Eğer isim varsa, sonucu isimle birlikte düzenle
                    if (name != null && !name.equals("-")) {
                        // İsimle birlikte sonucu düzenle
                        result = name + result.substring(eCode.length());
                    }

                    // Sonucu StringBuilder'a ekle
                    results.append(result).append(", ");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sonuçları String olarak döndür
        return results.toString();
    }

    private void showResults(String recognizedText) {
        // Gelen stringi ", " ile ayırarak katkı maddelerini al
        String[] additives = recognizedText.split(", "); // ", " ile ayır

        // Sonuçları saklamak için bir JSONObject oluştur
        JSONObject categorizedAdditives = new JSONObject();

        try {
            // Başlıkları JSON içine ekle
            categorizedAdditives.put("renklendiriciler", new JSONObject());
            categorizedAdditives.put("koruyucular", new JSONObject());
            categorizedAdditives.put("antioksidanlar_asitler_mineraller_tuzlar", new JSONObject());
            categorizedAdditives.put("kivam_articilar_stabilizorler_homojenlestiriciler", new JSONObject());
            categorizedAdditives.put("incelticiler", new JSONObject());
            categorizedAdditives.put("parlaticilar_tatlandiricilar", new JSONObject());

            // Her kategori için alt başlıkları ekle
            for (Iterator<String> categoryIterator = categorizedAdditives.keys(); categoryIterator.hasNext(); ) {
                String category = categoryIterator.next();
                JSONObject subCategories = categorizedAdditives.getJSONObject(category);
                subCategories.put("helal_kabul_edilen_katkilar", new JSONArray());
                subCategories.put("saglik_icin_tehlikeli_katkilar", new JSONArray());
                subCategories.put("kesin_hayvan_cogunlukla_domuz_kokenli_katkilar", new JSONArray());
                subCategories.put("supheli_katkilar", new JSONArray());
            }

            // Her bir katkı maddesi kodunu kontrol et
            for (String additive : additives) {
                // Eğer katkı maddesi kodu boş değilse kontrol et
                if (!additive.isEmpty()) {
                    // Katkı maddesini ":" ile ayır
                    String[] parts = additive.split(":"); // ":" ile ayır
                    String nameOrCode = parts[0].trim(); // İsim veya E kodu
                    String category = parts.length > 1 ? parts[1].trim() : null; // Kategori
                    String subCategory = parts.length > 2 ? parts[2].trim() : null; // Alt kategori

                    if (category != null && subCategory != null) {
                        // Kategori ve alt kategoriye ekle
                        addToCategory(categorizedAdditives, category, subCategory, nameOrCode);
                    }
                }
            }

            // Görünümü güncelle
            CardView resultsCard = findViewById(R.id.resultsCard);
            CardView backgroundCard = findViewById(R.id.backgroundCard);
            CardView previewCard = findViewById(R.id.previewCard);
            ImageView capturedImageView = findViewById(R.id.capturedImageView);
            TextView resultsText = findViewById(R.id.resultsText);
            resultsCard.setVisibility(View.VISIBLE);
            capturedImageView.setVisibility(View.GONE);
            backgroundCard.setVisibility(View.GONE);
            previewCard.setVisibility(View.GONE);

            // Sonuçları TextView'e yazdır
            displayFormattedJsonInTextView(categorizedAdditives, resultsText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // JSON'a katkı maddesini eklemek için yardımcı metot
    private void addToCategory(JSONObject categorizedAdditives, String category, String subCategory, String value) {
        try {
            // Kategoriyi al
            JSONObject subCategories = categorizedAdditives.getJSONObject(category);

            // Alt kategoriyi al ve değeri ekle
            JSONArray subCategoryArray = subCategories.getJSONArray(subCategory);
            subCategoryArray.put(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Kategoriyi çıkarmak için yardımcı metot
    private String extractCategory(String result) {
        // Kategori adını çıkarmak için düzenli ifade kullan
        if (result.contains("'renklendiriciler'")) {
            return "renklendiriciler";
        } else if (result.contains("'koruyucular'")) {
            return "koruyucular";
        } else if (result.contains("'antioksidanlar_asitler_mineraller_tuzlar'")) {
            return "antioksidanlar_asitler_mineraller_tuzlar";
        } else if (result.contains("'kivam_articilar_stabilizorler_homojenlestiriciler'")) {
            return "kivam_articilar_stabilizorler_homojenlestiriciler";
        } else if (result.contains("'incelticiler'")) {
            return "incelticiler";
        } else if (result.contains("'parlaticilar_tatlandiricilar'")) {
            return "parlaticilar_tatlandiricilar";
        }
        return null; // Eğer kategori bulunamazsa null döndür
    }

    // Alt kategoriyi çıkarmak için yardımcı metot
    private String extractSubCategory(String result) {
        // Alt kategori adını çıkarmak için düzenli ifade kullan
        if (result.contains("'helal_kabul_edilen_katkilar'")) {
            return "helal_kabul_edilen_katkilar";
        } else if (result.contains("'saglik_icin_tehlikeli_katkilar'")) {
            return "saglik_icin_tehlikeli_katkilar";
        } else if (result.contains("'kesin_hayvan_cogunlukla_domuz_kokenli_katkilar'")) {
            return "kesin_hayvan_cogunlukla_domuz_kokenli_katkilar";
        } else if (result.contains("'supheli_katkilar'")) {
            return "supheli_katkilar";
        }
        return null; // Eğer alt kategori bulunamazsa null döndür
    }

    private void displayFormattedJsonInTextView(JSONObject categorizedAdditives, TextView resultTextView) {
        // Son sonucu tutacak Builder
        SpannableStringBuilder formattedOutput = new SpannableStringBuilder();

        try {
            // Kategori ve alt kategori başlıklarını eşleştiren haritalar
            Map<String, String> categoryTitles = new HashMap<>();
            categoryTitles.put("renklendiriciler", "Renklendiriciler");
            categoryTitles.put("koruyucular", "Koruyucular");
            categoryTitles.put("antioksidanlar_asitler_mineraller_tuzlar", "Antioksidanlar, Asitler, Mineraller ve Tuzlar");
            categoryTitles.put("kivam_articilar_stabilizorler_homojenlestiriciler", "Kıvam Artırıcılar, Stabilizörler ve Homojenleştiriciler");
            categoryTitles.put("incelticiler", "İncelticiler");
            categoryTitles.put("parlaticilar_tatlandiricilar", "Parlatıcılar ve Tatlandırıcılar");

            Map<String, String> subCategoryTitles = new HashMap<>();
            subCategoryTitles.put("helal_kabul_edilen_katkilar", "Kabul Gören Katkılar");
            subCategoryTitles.put("saglik_icin_tehlikeli_katkilar", "Sağlık İçin Tehlikeli Katkılar");
            subCategoryTitles.put("kesin_hayvan_cogunlukla_domuz_kokenli_katkilar", "Sakıncalı Katkılar");
            subCategoryTitles.put("supheli_katkilar", "Şüpheli Katkılar");

            // Ana kategorileri dolaş
            for (Iterator<String> categoryIterator = categorizedAdditives.keys(); categoryIterator.hasNext(); ) {
                String category = categoryIterator.next();
                JSONObject subCategories = categorizedAdditives.getJSONObject(category);

                // Alt kategorilerde veri olup olmadığını kontrol etmek için bir bayrak
                boolean hasDataInCategory = false;

                // Alt kategorileri dolaş
                SpannableStringBuilder categoryOutput = new SpannableStringBuilder(); // Geçici kategori çıktısı
                for (Iterator<String> subCategoryIterator = subCategories.keys(); subCategoryIterator.hasNext(); ) {
                    String subCategory = subCategoryIterator.next();
                    JSONArray data = subCategories.getJSONArray(subCategory);

                    // Eğer alt kategoride veri varsa
                    if (data.length() > 0) {
                        hasDataInCategory = true; // Bu kategoride veri var

                        // Alt kategori başlığını al
                        String displaySubCategory = subCategoryTitles.getOrDefault(subCategory, subCategory);

                        // Alt kategoriyi italik yazıyla ve renkle
                        SpannableString subCategorySpannable = new SpannableString("   -" + displaySubCategory + ":\n");
                        subCategorySpannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, subCategorySpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        subCategorySpannable.setSpan(new ForegroundColorSpan(Color.parseColor("#707070")), 0, subCategorySpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Turuncu renk
                        categoryOutput.append(subCategorySpannable);

                        // Verileri liste olarak yazdır
                        for (int i = 0; i < data.length(); i++) {
                            categoryOutput.append("      • ").append(data.getString(i)).append("\n");
                        }
                    }
                }

                // Eğer bu kategoride herhangi bir alt kategoride veri varsa, kategoriyi ekle
                if (hasDataInCategory) {
                    // Kategori başlığını al
                    String displayCategory = categoryTitles.getOrDefault(category, category);

                    //stil uyguluyoruz (koyu ve renkli)
                    SpannableString categorySpannable = new SpannableString(displayCategory + ":\n");
                    categorySpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, categorySpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    categorySpannable.setSpan(new ForegroundColorSpan(Color.parseColor("#4A4A4A")), 0, categorySpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Mavi renk
                    formattedOutput.append(categorySpannable);
                    formattedOutput.append(categoryOutput); // Alt kategorileri ekle
                    formattedOutput.append("\n"); // Kategori sonuna boşluk ekle
                }
            }

            // Eğer hiçbir veri yoksa, kullanıcıya bilgi ver
            if (formattedOutput.length() == 0) {
                formattedOutput.append("Hiçbir veri bulunamadı.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            formattedOutput.append("Bir hata oluştu: ").append(e.getMessage());
        }

        // TextView'e son sonucu yaz
        resultTextView.setText(formattedOutput);
    }

    private void showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                // Ödül reklamı tamamlandığında yapılacak işlemler
                Log.d("RewardedAd", "Ödül reklamı tamamlandı!");
            });
        } else {
            Log.d("RewardedAd", "Ödül reklamı yüklenmedi.");
        }
    }
    private void showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd.show(this);
        } else {
            Log.d("InterstitialAd", "Geçiş reklamı yüklenmedi.");
        }
    }

}


// E 120 şeklinde yazılırsa da bitişik olarak algılasın