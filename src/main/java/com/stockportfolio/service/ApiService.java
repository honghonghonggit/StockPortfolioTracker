package com.stockportfolio.service;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiService {

    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private static String API_KEY = "";

    static {
        java.util.Properties prop = new java.util.Properties();
        try (java.io.FileInputStream input = new java.io.FileInputStream(".env")) {
            prop.load(input);
            API_KEY = prop.getProperty("api.key");
        } catch (java.io.IOException ex) {
            System.err.println(".env 파일을 읽는 중 오류가 발생했습니다: " + ex.getMessage());
        }
    }

    public interface ApiCallback {
        void onSuccess(String stockName, double currentPrice);
        void onFailure(String stockName, String errorMessage);
    }

    public void fetchCurrentPrice(String symbol, String stockName, ApiCallback callback) {
        new Thread(() -> {
            try {
                String urlStr = BASE_URL
                        + "?function=GLOBAL_QUOTE"
                        + "&symbol=" + symbol
                        + "&apikey=" + API_KEY;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    callback.onFailure(stockName, "HTTP 오류 코드: " + responseCode);
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

                if (json.has("Note") || json.has("Information")) {
                    callback.onFailure(stockName, "API 호출 제한 초과 (분당 5회). 잠시 후 재시도하세요.");
                    return;
                }

                if (json.has("Error Message")) {
                    callback.onFailure(stockName, "잘못된 종목 심볼: " + symbol);
                    return;
                }

                if (!json.has("Global Quote")) {
                    callback.onFailure(stockName, "데이터를 찾을 수 없습니다: " + symbol);
                    return;
                }

                JSONObject quote = json.getJSONObject("Global Quote");
                if (quote.isEmpty()) {
                    callback.onFailure(stockName, "종목 데이터 없음: " + symbol);
                    return;
                }

                String priceStr = quote.optString("05. price", "");
                if (priceStr.isEmpty()) {
                    callback.onFailure(stockName, "현재가 데이터 없음: " + symbol);
                    return;
                }

                double price = Double.parseDouble(priceStr);
                callback.onSuccess(stockName, price);

            } catch (Exception e) {
                callback.onFailure(stockName, "네트워크 오류: " + e.getMessage());
            }
        }).start();
    }

    public String searchSymbol(String keyword) {
        try {
            String urlStr = BASE_URL
                    + "?function=SYMBOL_SEARCH"
                    + "&keywords=" + java.net.URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                    + "&apikey=" + API_KEY;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) return null;

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

            if (json.has("Note") || json.has("Information")) {
                return null;
            }

            if (!json.has("bestMatches")) return null;

            var matches = json.getJSONArray("bestMatches");
            if (matches.isEmpty()) return null;

            return matches.getJSONObject(0).optString("1. symbol", null);

        } catch (Exception e) {
            System.err.println("심볼 검색 실패: " + e.getMessage());
            return null;
        }
    }
}
