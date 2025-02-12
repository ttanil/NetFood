package com.tanil.netfood;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class EAdditiveDetailsFetcher {

    private Context context;

    // Constructor ile Context alıyoruz
    public EAdditiveDetailsFetcher(Context context) {
        this.context = context;
    }

    // E maddesi içeriğini almak için metot
    public void fetchEAdditiveDetails(String eCode) {
        // OpenFoodFacts API URL'si
        String url = "https://world.openfoodfacts.org/additive/" + eCode.toLowerCase() + ".json";

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
                            if (response.has("additive")) {
                                JSONObject additive = response.getJSONObject("additive");
                                String name = additive.optString("name", "Bilinmiyor");
                                String description = additive.optString("description", "Açıklama yok");

                                // Bilgileri ekranda göster
                                String message = "E Maddesi: " + eCode + "\nAd: " + name + "\nAçıklama: " + description;
                                //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                System.out.println(message);
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