package com.stockportfolio.view;

import com.stockportfolio.model.Stock;
import com.stockportfolio.service.CsvService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProfitAnalysisPanel extends JPanel {

    private static final Color BG_MAIN = new Color(30, 30, 46);
    private static final Color BG_CARD = new Color(45, 45, 65);
    private static final Color TEXT_PRIMARY = new Color(230, 230, 250);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 190);
    private static final Color ACCENT_BLUE = new Color(100, 149, 237);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_RED = new Color(240, 80, 80);
    private static final Color BORDER_COLOR = new Color(60, 60, 85);

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private final DecimalFormat rateFormat = new DecimalFormat("+#,##0.00;-#,##0.00");
    private List<Stock> stockList;

    public ProfitAnalysisPanel() {
        this.stockList = loadStocks();
        initUI();
    }

    private List<Stock> loadStocks() {
        CsvService csvService = new CsvService();
        try {
            List<Stock> loaded = csvService.load();
            if (!loaded.isEmpty()) return loaded;
        } catch (IOException e) {
            System.err.println("CSV 로드 실패: " + e.getMessage());
        }
        return createDummyData();
    }

    private List<Stock> createDummyData() {
        List<Stock> list = new ArrayList<>();
        list.add(new Stock("삼성전자", 50, 65000, 72000));
        list.add(new Stock("SK하이닉스", 20, 120000, 145000));
        list.add(new Stock("NAVER", 10, 210000, 195000));
        list.add(new Stock("카카오", 30, 55000, 48000));
        list.add(new Stock("LG에너지솔루션", 5, 420000, 460000));
        list.add(new Stock("현대차", 15, 185000, 210000));
        return list;
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(createSummaryCards(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(createBarChartPanel(), BorderLayout.CENTER);
        centerPanel.add(createRankingPanel(), BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setOpaque(false);

        double avgRate = stockList.stream().mapToDouble(Stock::getProfitRate).average().orElse(0);
        double totalProfit = stockList.stream().mapToDouble(Stock::getProfit).sum();
        long profitCount = stockList.stream().filter(s -> s.getProfitRate() >= 0).count();
        long lossCount = stockList.size() - profitCount;

        panel.add(createCard("평균 수익률", rateFormat.format(avgRate) + " %", avgRate >= 0 ? ACCENT_GREEN : ACCENT_RED));
        panel.add(createCard("총 수익금", moneyFormat.format(totalProfit) + " 원", totalProfit >= 0 ? ACCENT_GREEN : ACCENT_RED));
        panel.add(createCard("수익 / 손실", profitCount + "개 수익  ·  " + lossCount + "개 손실", ACCENT_BLUE));
        return panel;
    }

    private JPanel createCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setPreferredSize(new Dimension(0, 100));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        return card;
    }

    private JPanel createBarChartPanel() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel sectionTitle = new JLabel("  📊 종목별 수익률");
        sectionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        wrapper.add(sectionTitle, BorderLayout.NORTH);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Stock> sorted = new ArrayList<>(stockList);
        sorted.sort(Comparator.comparingDouble(Stock::getProfitRate).reversed());
        for (Stock s : sorted) dataset.addValue(s.getProfitRate(), "수익률", s.getName());

        JFreeChart chart = ChartFactory.createBarChart(null, null, "수익률 (%)", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(BG_CARD);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(35, 35, 52));
        plot.setOutlinePaint(null);
        plot.setDomainGridlinePaint(BORDER_COLOR);
        plot.setRangeGridlinePaint(BORDER_COLOR);
        plot.setRangeZeroBaselinePaint(new Color(200, 200, 200, 100));
        plot.setRangeZeroBaselineVisible(true);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 11));
        domainAxis.setTickLabelPaint(TEXT_SECONDARY);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6));
        domainAxis.setAxisLinePaint(BORDER_COLOR);
        domainAxis.setTickMarkPaint(BORDER_COLOR);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("맑은 고딕", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(TEXT_SECONDARY);
        rangeAxis.setLabelFont(new Font("맑은 고딕", Font.PLAIN, 12));
        rangeAxis.setLabelPaint(TEXT_SECONDARY);
        rangeAxis.setAxisLinePaint(BORDER_COLOR);
        rangeAxis.setTickMarkPaint(BORDER_COLOR);

        final List<Stock> sortedFinal = sorted;
        BarRenderer renderer = new BarRenderer() {
            @Override public Paint getItemPaint(int row, int column) {
                if (column >= 0 && column < sortedFinal.size())
                    return sortedFinal.get(column).getProfitRate() >= 0 ? ACCENT_GREEN : ACCENT_RED;
                return ACCENT_BLUE;
            }
        };
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.08);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}%", new DecimalFormat("#0.0")));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("맑은 고딕", Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(TEXT_PRIMARY);
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        chartPanel.setMouseWheelEnabled(false);
        wrapper.add(chartPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createRankingPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 180));

        List<Stock> sorted = new ArrayList<>(stockList);
        sorted.sort(Comparator.comparingDouble(Stock::getProfitRate).reversed());
        int topCount = Math.min(3, sorted.size());

        panel.add(createRankCard("🏆 수익 상위 종목", sorted.subList(0, topCount), ACCENT_GREEN));

        List<Stock> bottom = new ArrayList<>(sorted.subList(Math.max(0, sorted.size() - topCount), sorted.size()));
        java.util.Collections.reverse(bottom);
        panel.add(createRankCard("📉 수익 하위 종목", bottom, ACCENT_RED));
        return panel;
    }

    private JPanel createRankCard(String title, List<Stock> stocks, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < stocks.size(); i++) {
            Stock s = stocks.get(i);
            listPanel.add(createRankRow(medals[i % medals.length], s.getName(),
                    rateFormat.format(s.getProfitRate()) + "%",
                    moneyFormat.format(s.getProfit()) + " 원",
                    s.getProfitRate() >= 0 ? ACCENT_GREEN : ACCENT_RED));
            if (i < stocks.size() - 1) listPanel.add(Box.createVerticalStrut(6));
        }
        card.add(listPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRankRow(String medal, String name, String rate, String profit, Color rateColor) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(55, 55, 78));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel leftLabel = new JLabel(medal + "  " + name);
        leftLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        leftLabel.setForeground(TEXT_PRIMARY);
        row.add(leftLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JLabel profitLabel = new JLabel(profit);
        profitLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        profitLabel.setForeground(TEXT_SECONDARY);

        JLabel rateLabel = new JLabel(rate);
        rateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        rateLabel.setForeground(rateColor);

        rightPanel.add(profitLabel);
        rightPanel.add(rateLabel);
        row.add(rightPanel, BorderLayout.EAST);
        return row;
    }
}
