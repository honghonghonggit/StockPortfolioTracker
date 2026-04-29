package com.stockportfolio.service;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * exchangerate-api.com을 통한 실시간 환율 조회 서비스
 * - USD/KRW 환율 조회
 * - 조회 실패 시 기본값 1350원 fallback
 */
public class ExchangeRateService {

    private static final String BASE_URL = "https://open.er-api.com/v6/latest/USD";
    private static final double DEFAULT_RATE = 1350.0;

    /**
     * 환율 조회 결과 콜백
     */
    public interface RateCallback {
        void onResult(double usdToKrw, boolean isLive);
    }

    /**
     * USD → KRW 환율을 비동기로 조회
     * 실패 시 기본값 1350원으로 fallback
     */
    public void fetchUsdKrw(RateCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int code = conn.getResponseCode();
                if (code != 200) {
                    callback.onResult(DEFAULT_RATE, false);
                    return;
                }

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                conn.disconnect();

                JSONObject json = new JSONObject(sb.toString());
                if (!"success".equals(json.optString("result"))) {
                    callback.onResult(DEFAULT_RATE, false);
                    return;
                }

                JSONObject rates = json.getJSONObject("rates");
                double krw = rates.getDouble("KRW");
                callback.onResult(krw, true);

            } catch (Exception e) {
                System.err.println("환율 조회 실패: " + e.getMessage());
                callback.onResult(DEFAULT_RATE, false);
            }
        }).start();
    }

    /**
     * 기본 환율 반환
     */
    public static double getDefaultRate() {
        return DEFAULT_RATE;
    }
}
