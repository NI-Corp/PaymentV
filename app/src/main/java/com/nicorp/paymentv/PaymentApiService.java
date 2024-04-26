package com.nicorp.paymentv;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaymentApiService {
    private static PaymentApiService _instance;
    private final String secretKey;

    private PaymentApiService() {
        // Получение значения переменной окружения SECRET_KEY
//        this.secretKey = System.getenv("SECRET_KEY");
        this.secretKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNQTYyMjk3NiIsImp0aSI6ImI1OTNkODRkLTk1MWYtNGIyZi05ZGViLTcxOWExNDM4NWVmZCJ9.si-87k3Aw5GN67orgJpoyTXC0C2OpWwRCKzLogRWawU";
        Log.d("SECRET_KEY", this.secretKey);
    }

    public static PaymentApiService getInstance() {
        if (_instance == null) {
            _instance = new PaymentApiService();
        }
        return _instance;
    }

    public void getOrderDataAsync(String orderId, OnOrderStatusReceivedListener listener) {
        new OrderStatusTask(listener).execute(orderId);
    }

    private class OrderStatusTask extends AsyncTask<String, Void, String> {
        private final OnOrderStatusReceivedListener listener;

        public OrderStatusTask(OnOrderStatusReceivedListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            String orderId = params[0];
            try {
                String apiUrl = "https://pay-test.raif.ru/api/payment/v1/orders/" + orderId;
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Добавляем заголовок Authorization
                urlConnection.setRequestProperty("Authorization", "Bearer " + secretKey);

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                String orderQR = jsonResponse.getJSONObject("qr").getString("id");

                apiUrl = "https://pay-test.raif.ru/api/sbp/v2/qrs/" + orderQR;
                url = new URL(apiUrl);
                urlConnection = (HttpURLConnection) url.openConnection();

                // Добавляем заголовок Authorization
                urlConnection.setRequestProperty("Authorization", "Bearer " + secretKey);

                response.setLength(0);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }

                jsonResponse = new JSONObject(response.toString());
                String orderStatus = jsonResponse.getString("qrStatus");

                return orderStatus;
            } catch (IOException | JSONException e) {
                Log.e("Error", "Exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (listener != null) {
                listener.onOrderStatusReceived(result);
            }
        }
    }

    public interface OnOrderStatusReceivedListener {
        void onOrderStatusReceived(String status);
    }
}
