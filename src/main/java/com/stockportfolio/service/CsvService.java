package com.stockportfolio.service;

import com.stockportfolio.model.Stock;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvService {

    private static final String DATA_DIR = "data";
    private static final String FILE_NAME = "portfolio.csv";
    private static final String GOALS_FILE_NAME = "goals.csv";
    private static final String HEADER = "종목명,보유수량,매수가,현재가,통화";
    private static final String GOALS_HEADER = "종목명,목표수익률";

    private final Path filePath;
    private final Path goalsFilePath;

    public CsvService() {
        this.filePath = Paths.get(DATA_DIR, FILE_NAME);
        this.goalsFilePath = Paths.get(DATA_DIR, GOALS_FILE_NAME);
    }

    public void save(List<Stock> stocks) throws IOException {
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

    public boolean exists() {
        return Files.exists(filePath);
    }

    public Path getFilePath() {
        return filePath;
    }

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
