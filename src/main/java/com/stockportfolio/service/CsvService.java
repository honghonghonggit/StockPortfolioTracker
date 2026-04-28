package com.stockportfolio.service;

import com.stockportfolio.model.Stock;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 파일을 통한 종목 데이터 저장/로드 서비스
 * 파일 경로: 프로젝트 루트의 data/portfolio.csv
 */
public class CsvService {

    private static final String DATA_DIR = "data";
    private static final String FILE_NAME = "portfolio.csv";
    private static final String HEADER = "종목명,보유수량,매수가,현재가";

    private final Path filePath;

    public CsvService() {
        // 실행 위치 기준 data/portfolio.csv
        this.filePath = Paths.get(DATA_DIR, FILE_NAME);
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
}
