package com.stockportfolio.model;

/**
 * 보유 주식 종목 정보를 담는 모델 클래스
 */
public class Stock {
    private String name;        // 종목명
    private int quantity;       // 보유 수량
    private double buyPrice;    // 매수가
    private double currentPrice;// 현재가
    private String currency;    // 통화 (KRW 또는 USD)

    public Stock(String name, int quantity, double buyPrice, double currentPrice, String currency) {
        this.name = name;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.currentPrice = currentPrice;
        this.currency = (currency != null) ? currency.toUpperCase() : "KRW";
    }

    public Stock(String name, int quantity, double buyPrice, double currentPrice) {
        this(name, quantity, buyPrice, currentPrice, "KRW");
    }

    /**
     * 종목 관리용 생성자 (현재가는 매수가로 초기화)
     */
    public Stock(String name, int quantity, double buyPrice) {
        this(name, quantity, buyPrice, buyPrice, "KRW");
    }

    /**
     * CSV 한 줄로 변환 (종목명,보유수량,매수가,현재가,통화)
     */
    public String toCsvLine() {
        return name + "," + quantity + "," + buyPrice + "," + currentPrice + "," + currency;
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
        String currency = (parts.length >= 5) ? parts[4].trim().toUpperCase() : "KRW";
        return new Stock(name, quantity, buyPrice, currentPrice, currency);
    }

    // ── Getters / Setters ──

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = (currency != null) ? currency.toUpperCase() : "KRW"; }

    public boolean isUsd() { return "USD".equals(currency); }

    /**
     * 수익률 계산 (%)
     */
    public double getProfitRate() {
        if (buyPrice == 0) return 0;
        return ((currentPrice - buyPrice) / buyPrice) * 100;
    }

    /**
     * 평가금액 계산 (해당 통화 기준)
     */
    public double getEvalAmount() {
        return currentPrice * quantity;
    }

    /**
     * 평가금액 계산 (원화 환산)
     */
    public double getEvalAmountKrw(double usdToKrw) {
        double eval = getEvalAmount();
        return isUsd() ? eval * usdToKrw : eval;
    }

    /**
     * 매수금액 계산
     */
    public double getBuyAmount() {
        return buyPrice * quantity;
    }

    /**
     * 매수금액 계산 (원화 환산)
     */
    public double getBuyAmountKrw(double usdToKrw) {
        double buy = getBuyAmount();
        return isUsd() ? buy * usdToKrw : buy;
    }

    /**
     * 수익금 계산
     */
    public double getProfit() {
        return getEvalAmount() - getBuyAmount();
    }
}
