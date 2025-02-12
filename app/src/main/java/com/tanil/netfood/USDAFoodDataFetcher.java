package com.tanil.netfood;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class USDAFoodDataFetcher {

    private Context context;
    private static final String API_KEY = "l5RIr1Vev2mg3cd1rJxgPdzZVkPLgPpBXc5TnU1D";

    // Constructor ile Context alıyoruz
    public USDAFoodDataFetcher(Context context) {
        this.context = context;
    }

    // E maddesi bilgilerini aramak için metot
    public void searchEAdditive(String eCode) {
        // API URL'si
        String url = "https://api.nal.usda.gov/fdc/v1/foods/search?query=" + eCode + "&api_key=" + API_KEY;

        // Volley RequestQueue oluştur
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // JSON Object Request oluştur
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // API'den gelen JSON verisini işleme
                            if (response.has("foods")) {
                                JSONArray foods = response.getJSONArray("foods");

                                // İlk 5 sonucu ekrana yazdır
                                for (int i = 0; i < Math.min(foods.length(), 5); i++) {
                                    JSONObject food = foods.getJSONObject(i);
                                    String description = food.optString("description", "Bilinmiyor");
                                    String fdcId = food.optString("fdcId", "Bilinmiyor");
                                    String ingredients = food.optString("ingredients", "Bilinmiyor");

                                    // Bilgileri ekranda göster
                                    String message = "E Maddesi: " + eCode + "\nÜrün: " + description + "\nFDC ID: " + fdcId + "\nİçerikler: " + ingredients;
                                    //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                    System.out.println(message);
                                }
                            } else {
                                //Toast.makeText(context, "E maddesi bilgisi bulunamadı.", Toast.LENGTH_SHORT).show();
                                System.out.println("E maddesi bilgisi bulunamadı.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(context, "JSON Hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            System.out.println("JSON Hatası: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hata durumunda işlem
                        error.printStackTrace();
                        //Toast.makeText(context, "API Hatası: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        System.out.println("API Hatası: " + error.getMessage());
                    }
                }
        );

        // İsteği kuyruğa ekle
        requestQueue.add(jsonObjectRequest);
    }
}