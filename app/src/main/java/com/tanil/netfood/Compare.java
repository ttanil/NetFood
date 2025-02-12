package com.tanil.netfood;

import android.content.Context;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Compare {
    private Context context;

    // Constructor ile Context'i alıyoruz
    public Compare(Context context) {
        this.context = context;
    }

    // Katkı maddesi kodunu bulmak için metot
    public String findAdditiveCategory(String code) {
        try {
            // JSON dosyasını oku
            InputStream inputStream = context.getResources().openRawResource(R.raw.food_addictives);

            String jsonString;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                StringBuilder builder = new StringBuilder();
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    builder.append((char) ch);
                }
                jsonString = builder.toString();
            }

            // JSON String'i JSONObject'e çevir
            JSONObject jsonObject = new JSONObject(jsonString);

            // "katkilar" anahtarına eriş
            JSONObject katkilar = jsonObject.getJSONObject("katkilar");

            // Kategoriler arasında dolaş
            Iterator<String> kategoriIterator = katkilar.keys();
            while (kategoriIterator.hasNext()) {
                String kategori = kategoriIterator.next();
                JSONObject siniflar = katkilar.getJSONObject(kategori);

                // Sınıflar arasında dolaş
                Iterator<String> sinifIterator = siniflar.keys();
                while (sinifIterator.hasNext()) {
                    String sinif = sinifIterator.next();
                    JSONArray maddeler = siniflar.getJSONArray(sinif);

                    // Kodun bu sınıfta olup olmadığını kontrol et
                    for (int i = 0; i < maddeler.length(); i++) {
                        if (maddeler.getString(i).equalsIgnoreCase(code)) {
                            return code + ":" + kategori + ":" + sinif;
                        }
                    }
                }
            }

            // Kod bulunamazsa
            return code + " listede bulunamadı.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Bir hata oluştu: " + e.getMessage();
        }
    }
    // Metin içinde JSON'daki kelime ya da kelime gruplarını ve E kodlarını arayan metot
    public String findMatchesInText(String inputText) {
        List<String> matches = new ArrayList<>();

        try {
            // JSON dosyasını oku
            InputStream inputStream = context.getResources().openRawResource(R.raw.e_codes_detailed);

            // UTF-8 kodlamasıyla dosyayı oku
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            // JSON String'i JSONObject'e çevir
            JSONObject jsonObject = new JSONObject(builder.toString());

            // "E_Codes_Details" anahtarına eriş ve JSONArray olarak al
            JSONArray eCodesDetails = jsonObject.getJSONArray("E_Codes_Details");

            // Kullanıcıdan gelen metni küçük harfe çevir ve temizle
            String lowerCaseInputText = inputText.toLowerCase();
            lowerCaseInputText = lowerCaseInputText.replaceAll("[.;:!?\\[\\]{}\"']", " "); // Noktalama işaretlerini boşlukla değiştir

            // Metni virgül, boşluk ve ardından gelen ( işaretine göre ayır
            String[] phrases = lowerCaseInputText.split(",|\\s\\(|\\)");

            // JSON'daki her bir öğeyi kontrol et
            for (int i = 0; i < eCodesDetails.length(); i++) {
                JSONObject item = eCodesDetails.getJSONObject(i);

                // E kodunu, Name ve Alternative_Name değerlerini al
                String code = item.getString("Code").toLowerCase();
                String name = item.getString("Name").toLowerCase();
                String alternativeName = item.optString("Alternative_Name", "").toLowerCase();

                // Her bir kelime grubunu kontrol et
                for (String phrase : phrases) {
                    // Kelime grubundaki fazla boşlukları temizle
                    String cleanedPhrase = phrase.trim();

                    // Eğer tam eşleşme varsa
                    if (Objects.equals(cleanedPhrase, code) || Objects.equals(cleanedPhrase, name) || Objects.equals(cleanedPhrase, alternativeName)) {
                        // Eğer isim eşleşiyorsa E kodu ve ismi birlikte ekle
                        String matchedName = "";
                        if (Objects.equals(cleanedPhrase, name)) {
                            matchedName = item.getString("Name");
                        } else if (Objects.equals(cleanedPhrase, alternativeName)) {
                            matchedName = item.getString("Alternative_Name");
                        }

                        // Eğer isim yoksa sadece E kodunu ekle ve yanına "-"
                        String result = matchedName.isEmpty() ? (item.getString("Code") + " : -") : (item.getString("Code") + " : " + matchedName);

                        // Aynı eşleşme tekrar eklenmesin
                        if (!matches.contains(result)) {
                            matches.add(result);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listeyi ", " ile birleştirerek döndür
        return String.join(", ", matches);
    }

}