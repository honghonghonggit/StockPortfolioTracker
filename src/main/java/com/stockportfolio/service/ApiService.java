package com.stockportfolio.service;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Alpha Vantage API를 통한 실시간 주가 조회 서비스
 * - GLOBAL_QUOTE 엔드포인트 사용
 * - 무료 키 제한: 분당 5회, 일 500회
 */
public class ApiService {

    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private static String API_KEY = "";

    static {
        java.util.Properties prop = new java.util.Properties();
        try (java.io.FileInputStream input = new java.io.FileInputStream("config.properties")) {
            prop.load(input);
            API_KEY = prop.getProperty("api.key");
        } catch (java.io.IOException ex) {
            System.err.println("config.properties 파일을 읽는 중 오류가 발생했습니다: " + ex.getMessage());
        }
    }

    /**
     * API 조회 결과 콜백 인터페이스
     */
    public interface ApiCallback {
        void onSuccess(String stockName, double currentPrice);
        void onFailure(String stockName, String errorMessage);
    }

    /**
     * 종목의 현재가를 Alpha Vantage API로 조회
     *
     * @param symbol   종목 심볼 (예: AAPL, 005930.KS)
     * @param stockName 화면에 표시될 종목명
     * @param callback 결과 콜백
     */
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

                // 응답 읽기
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                conn.disconnect();

                // JSON 파싱
                JSONObject json = new JSONObject(sb.toString());

                // API 제한 초과 체크
                if (json.has("Note") || json.has("Information")) {
                    String msg = json.optString("Note", json.optString("Information", ""));
                    callback.onFailure(stockName, "API 호출 제한 초과 (분당 5회). 잠시 후 재시도하세요.");
                    return;
                }

                // Error Message 체크
                if (json.has("Error Message")) {
                    callback.onFailure(stockName, "잘못된 종목 심볼: " + symbol);
                    return;
                }

                // Global Quote 파싱
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

    /**
     * 종목명을 기반으로 심볼 검색 (SYMBOL_SEARCH)
     *
     * @param keyword  검색 키워드 (종목명)
     * @return 가장 관련성 높은 심볼, 없으면 null
     */
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

            // API 제한 체크
            if (json.has("Note") || json.has("Information")) {
                return null;
            }

            if (!json.has("bestMatches")) return null;

            var matches = json.getJSONArray("bestMatches");
            if (matches.isEmpty()) return null;

            // 첫 번째 결과의 심볼 반환
            return matches.getJSONObject(0).optString("1. symbol", null);

        } catch (Exception e) {
            System.err.println("심볼 검색 실패: " + e.getMessage());
            return null;
        }
    }
}
