package com.stockportfolio.model;

/**
 * 보유 주식 종목 정보를 담는 모델 클래스
 */
public class Stock {
    private String name;        // 종목명
    private int quantity;       // 보유 수량
    private double buyPrice;    // 매수가
    private double currentPrice;// 현재가

    public Stock(String name, int quantity, double buyPrice, double currentPrice) {
        this.name = name;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.currentPrice = currentPrice;
    }

    /**
     * 종목 관리용 생성자 (현재가는 매수가로 초기화)
     */
    public Stock(String name, int quantity, double buyPrice) {
        this(name, quantity, buyPrice, buyPrice);
    }

    /**
     * CSV 한 줄로 변환 (종목명,보유수량,매수가,현재가)
     */
    public String toCsvLine() {
        return name + "," + quantity + "," + buyPrice + "," + currentPrice;
    }

    /**
     * CSV 한 줄에서 Stock 객체 생성
     */
    public static Stock fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("CSV 형식 오류: " + line);
        }
        String name = parts[0].trim();
        int quantity = Integer.parseInt(parts[1].trim());
        double buyPrice = Double.parseDouble(parts[2].trim());
        double currentPrice = (parts.length >= 4) ? Double.parseDouble(parts[3].trim()) : buyPrice;
        return new Stock(name, quantity, buyPrice, currentPrice);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    /**
     * 수익률 계산 (%)
     */
    public double getProfitRate() {
        if (buyPrice == 0) return 0;
        return ((currentPrice - buyPrice) / buyPrice) * 100;
    }

    /**
     * 평가금액 계산
     */
    public double getEvalAmount() {
        return currentPrice * quantity;
    }

    /**
     * 매수금액 계산
     */
    public double getBuyAmount() {
        return buyPrice * quantity;
    }

    /**
     * 수익금 계산
     */
    public double getProfit() {
        return getEvalAmount() - getBuyAmount();
    }
}
