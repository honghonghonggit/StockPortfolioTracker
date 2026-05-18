package com.stockportfolio.model;

public class Stock {
    private String name;
    private int quantity;
    private double buyPrice;
    private double currentPrice;
    private String currency;

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

    public Stock(String name, int quantity, double buyPrice) {
        this(name, quantity, buyPrice, buyPrice, "KRW");
    }

    public String toCsvLine() {
        return name + "," + quantity + "," + buyPrice + "," + currentPrice + "," + currency;
    }

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

    public double getProfitRate() {
        if (buyPrice == 0) return 0;
        return ((currentPrice - buyPrice) / buyPrice) * 100;
    }

    public double getEvalAmount() {
        return currentPrice * quantity;
    }

    public double getEvalAmountKrw(double usdToKrw) {
        double eval = getEvalAmount();
        return isUsd() ? eval * usdToKrw : eval;
    }

    public double getBuyAmount() {
        return buyPrice * quantity;
    }

    public double getBuyAmountKrw(double usdToKrw) {
        double buy = getBuyAmount();
        return isUsd() ? buy * usdToKrw : buy;
    }

    public double getProfit() {
        return getEvalAmount() - getBuyAmount();
    }
}
