package com.stockportfolio.service;

import com.stockportfolio.model.Stock;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV 파일을 통한 종목 데이터 저장/로드 서비스
 * - 종목 데이터: data/portfolio.csv
 * - 목표 수익률: data/goals.csv
 */
public class CsvService {

    private static final String DATA_DIR = "data";
    private static final String FILE_NAME = "portfolio.csv";
    private static final String GOALS_FILE_NAME = "goals.csv";
    private static final String HEADER = "종목명,보유수량,매수가,현재가,통화";
    private static final String GOALS_HEADER = "종목명,목표수익률";

    private final Path filePath;
    private final Path goalsFilePath;

    public CsvService() {
        // 실행 위치 기준 data/ 디렉터리
        this.filePath = Paths.get(DATA_DIR, FILE_NAME);
        this.goalsFilePath = Paths.get(DATA_DIR, GOALS_FILE_NAME);
    }

    /**
     * 종목 리스트를 CSV 파일에 저장
     */
    public void save(List<Stock> stocks) throws IOException {
        // data 디렉터리가 없으면 생성
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Stock stock : stocks) {
                writer.write(stock.toCsvLine());
                writer.newLine();
            }
        }
    }

    /**
     * CSV 파일에서 종목 리스트를 로드
     * 파일이 없으면 빈 리스트 반환
     */
    public List<Stock> load() throws IOException {
        List<Stock> stocks = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return stocks;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // 첫 줄(헤더) 건너뛰기
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                try {
                    stocks.add(Stock.fromCsvLine(line));
                } catch (Exception e) {
                    System.err.println("CSV 파싱 오류 (건너뜀): " + line + " → " + e.getMessage());
                }
            }
        }

        return stocks;
    }

    /**
     * CSV 파일 존재 여부 확인
     */
    public boolean exists() {
        return Files.exists(filePath);
    }

    /**
     * CSV 파일 경로 반환
     */
    public Path getFilePath() {
        return filePath;
    }

    // ──────────────────────────────────────────────
    // 목표 수익률 (goals.csv)
    // ──────────────────────────────────────────────

    /**
     * 종목별 목표 수익률을 goals.csv에 저장
     * @param goals 종목명 → 목표수익률(%) 매핑
     */
    public void saveGoals(Map<String, Double> goals) throws IOException {
        Files.createDirectories(goalsFilePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(goalsFilePath, StandardCharsets.UTF_8)) {
            writer.write(GOALS_HEADER);
            writer.newLine();
            for (Map.Entry<String, Double> entry : goals.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    /**
     * goals.csv에서 종목별 목표 수익률을 로드
     * 파일이 없으면 빈 맵 반환
     * @return 종목명 → 목표수익률(%) 매핑
     */
    public Map<String, Double> loadGoals() throws IOException {
        Map<String, Double> goals = new HashMap<>();

        if (!Files.exists(goalsFilePath)) {
            return goals;
        }

        try (BufferedReader reader = Files.newBufferedReader(goalsFilePath, StandardCharsets.UTF_8)) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        double targetRate = Double.parseDouble(parts[1].trim());
                        goals.put(name, targetRate);
                    }
                } catch (Exception e) {
                    System.err.println("Goals CSV 파싱 오류 (건너뜀): " + line + " → " + e.getMessage());
                }
            }
        }

        return goals;
    }
}
