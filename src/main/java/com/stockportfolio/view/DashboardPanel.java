package com.stockportfolio.view;

import com.stockportfolio.model.Stock;
import com.stockportfolio.service.CsvService;
import com.stockportfolio.service.ExchangeRateService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 메인 대시보드 패널
 * - 상단: 환율 정보 + 총자산, 총수익금, 전체수익률 요약 카드
 * - 하단 왼쪽: 보유종목 테이블
 * - 하단 오른쪽: 종목별 비중 파이차트
 * - CsvService 연동으로 실시간 데이터 반영
 */
public class DashboardPanel extends JPanel {

    // 색상 상수
    private static final Color BG_MAIN = new Color(30, 30, 46);
    private static final Color BG_CARD = new Color(45, 45, 65);
    private static final Color BG_TABLE = new Color(40, 40, 58);
    private static final Color TEXT_PRIMARY = new Color(230, 230, 250);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 190);
    private static final Color ACCENT_BLUE = new Color(100, 149, 237);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_RED = new Color(240, 80, 80);
    private static final Color ACCENT_PURPLE = new Color(160, 120, 240);
    private static final Color BORDER_COLOR = new Color(60, 60, 85);

    // 파이 차트 색상 팔레트
    private static final Color[] PIE_COLORS = {
            new Color(100, 149, 237),
            new Color(80, 200, 120),
            new Color(240, 180, 60),
            new Color(160, 120, 240),
            new Color(240, 100, 100),
            new Color(60, 200, 200),
    };

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private final DecimalFormat rateFormat = new DecimalFormat("+#,##0.00;-#,##0.00");

    // 서비스
    private final CsvService csvService;
    private final ExchangeRateService exchangeRateService;

    // 데이터
    private List<Stock> stockList;
    private double usdToKrw = ExchangeRateService.getDefaultRate();
    private boolean isLiveRate = false;

    // 동적 업데이트용 UI 참조
    private JPanel summaryCardsPanel;
    private JPanel tableWrapper;
    private JPanel chartWrapper;
    private JLabel exchangeRateLabel;
    private DefaultTableModel tableModel;
    private DefaultPieDataset pieDataset;
    private JFreeChart pieChart;

    // 요약 카드 값 라벨 (동적 갱신용)
    private JLabel totalAssetValue;
    private JLabel totalProfitValue;
    private JLabel totalRateValue;

    public DashboardPanel() {
        this.csvService = new CsvService();
        this.exchangeRateService = new ExchangeRateService();
        this.stockList = new ArrayList<>();
        loadFromCsv();
        initUI();
        fetchExchangeRate();
    }

    /**
     * CSV에서 종목 데이터 로드
     */
    private void loadFromCsv() {
        try {
            stockList.clear();
            stockList.addAll(csvService.load());
        } catch (IOException e) {
            System.err.println("대시보드 CSV 로드 실패: " + e.getMessage());
        }
    }

    /**
     * 환율 비동기 조회
     */
    private void fetchExchangeRate() {
        exchangeRateService.fetchUsdKrw((rate, isLive) -> {
            SwingUtilities.invokeLater(() -> {
                usdToKrw = rate;
                isLiveRate = isLive;
                updateExchangeRateLabel();
                updateSummaryCards();
            });
        });
    }

    /**
     * 외부에서 호출 가능한 전체 데이터 새로고침
     * (종목 관리에서 추가/수정/삭제 시 호출)
     */
    public void refreshData() {
        loadFromCsv();
        updateSummaryCards();
        updateTable();
        updatePieChart();
    }

    // ──────────────────────────────────────────────
    // UI 초기화
    // ──────────────────────────────────────────────

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단: 환율 바 + 요약 카드
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setOpaque(false);

        northPanel.add(createExchangeRateBar());
        northPanel.add(Box.createVerticalStrut(12));
        summaryCardsPanel = createSummaryCardsPanel();
        northPanel.add(summaryCardsPanel);

        add(northPanel, BorderLayout.NORTH);

        // 하단 컨텐츠 영역 (테이블 + 차트)
        add(createContentPanel(), BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────
    // 환율 정보 바
    // ──────────────────────────────────────────────

    private JPanel createExchangeRateBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(35, 35, 55));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));
        bar.setPreferredSize(new Dimension(0, 42));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("💱 USD/KRW 환율  ");
        icon.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        icon.setForeground(TEXT_SECONDARY);
        bar.add(icon, BorderLayout.WEST);

        exchangeRateLabel = new JLabel("조회 중...");
        exchangeRateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        exchangeRateLabel.setForeground(ACCENT_BLUE);
        exchangeRateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        bar.add(exchangeRateLabel, BorderLayout.EAST);

        return bar;
    }

    private void updateExchangeRateLabel() {
        if (exchangeRateLabel != null) {
            String rateText = moneyFormat.format(usdToKrw) + " 원";
            if (isLiveRate) {
                exchangeRateLabel.setText("₩ " + rateText + "  (실시간)");
                exchangeRateLabel.setForeground(ACCENT_GREEN);
            } else {
                exchangeRateLabel.setText("₩ " + rateText + "  (기본값)");
                exchangeRateLabel.setForeground(new Color(240, 180, 60));
            }
        }
    }

    // ──────────────────────────────────────────────
    // 상단 요약 카드 3개
    // ──────────────────────────────────────────────

    private JPanel createSummaryCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        double totalAsset = stockList.stream().mapToDouble(s -> s.getEvalAmountKrw(usdToKrw)).sum();
        double totalBuy = stockList.stream().mapToDouble(s -> s.getBuyAmountKrw(usdToKrw)).sum();
        double totalProfit = totalAsset - totalBuy;
        double totalRate = (totalBuy == 0) ? 0 : (totalProfit / totalBuy) * 100;

        // 카드 생성 (값 라벨 참조 저장)
        totalAssetValue = new JLabel();
        totalProfitValue = new JLabel();
        totalRateValue = new JLabel();

        panel.add(createCard("총 자산", totalAssetValue, ACCENT_BLUE));
        panel.add(createCard("총 수익금", totalProfitValue, totalProfit >= 0 ? ACCENT_GREEN : ACCENT_RED));
        panel.add(createCard("전체 수익률", totalRateValue, totalRate >= 0 ? ACCENT_GREEN : ACCENT_RED));

        updateSummaryValues(totalAsset, totalProfit, totalRate);

        return panel;
    }

    private void updateSummaryValues(double totalAsset, double totalProfit, double totalRate) {
        if (totalAssetValue != null) {
            totalAssetValue.setText(moneyFormat.format(totalAsset) + " 원");
        }
        if (totalProfitValue != null) {
            totalProfitValue.setText(moneyFormat.format(totalProfit) + " 원");
            totalProfitValue.setForeground(TEXT_PRIMARY);
        }
        if (totalRateValue != null) {
            totalRateValue.setText(rateFormat.format(totalRate) + " %");
            totalRateValue.setForeground(TEXT_PRIMARY);
        }
    }

    private void updateSummaryCards() {
        double totalAsset = stockList.stream().mapToDouble(s -> s.getEvalAmountKrw(usdToKrw)).sum();
        double totalBuy = stockList.stream().mapToDouble(s -> s.getBuyAmountKrw(usdToKrw)).sum();
        double totalProfit = totalAsset - totalBuy;
        double totalRate = (totalBuy == 0) ? 0 : (totalProfit / totalBuy) * 100;
        updateSummaryValues(totalAsset, totalProfit, totalRate);
    }

    /**
     * 요약 카드 생성
     */
    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
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

        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    // ──────────────────────────────────────────────
    // 하단 컨텐츠 (테이블 + 파이차트)
    // ──────────────────────────────────────────────

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);

        panel.add(createTablePanel());
        panel.add(createChartPanel());

        return panel;
    }

    // ──────────────────────────────────────────────
    // 보유종목 테이블
    // ──────────────────────────────────────────────

    private JPanel createTablePanel() {
        tableWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel sectionTitle = new JLabel("  보유 종목");
        sectionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        tableWrapper.add(sectionTitle, BorderLayout.NORTH);

        String[] columns = {"종목명", "매수가", "현재가", "수익률", "평가금액"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        populateTableData();

        JTable table = new JTable(tableModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        return tableWrapper;
    }

    private void populateTableData() {
        tableModel.setRowCount(0);
        if (stockList.isEmpty()) {
            return;
        }
        for (Stock s : stockList) {
            String currTag = s.isUsd() ? " [USD]" : "";
            String sym = s.isUsd() ? "$" : "";
            String sfx = s.isUsd() ? "" : "";
            String evalStr = moneyFormat.format(s.getEvalAmountKrw(usdToKrw)) + " 원";

            tableModel.addRow(new Object[]{
                    s.getName() + currTag,
                    sym + moneyFormat.format(s.getBuyPrice()),
                    sym + moneyFormat.format(s.getCurrentPrice()),
                    rateFormat.format(s.getProfitRate()) + "%",
                    evalStr
            });
        }
    }

    private void updateTable() {
        populateTableData();
    }

    /**
     * 테이블 스타일 적용
     */
    private void styleTable(JTable table) {
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(60, 60, 90));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        header.setBackground(BG_TABLE);
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
        header.setReorderingAllowed(false);

        // 셀 렌더러 - 수익률 컬럼 색상
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);

                if (column == 3 && value != null) {
                    String v = value.toString();
                    if (v.startsWith("+")) {
                        setForeground(ACCENT_GREEN);
                    } else if (v.startsWith("-")) {
                        setForeground(ACCENT_RED);
                    } else {
                        setForeground(TEXT_PRIMARY);
                    }
                } else {
                    setForeground(isSelected ? TEXT_PRIMARY : TEXT_PRIMARY);
                }

                setBackground(isSelected ? new Color(60, 60, 90) : BG_CARD);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setForeground(TEXT_PRIMARY);
                setBackground(isSelected ? new Color(60, 60, 90) : BG_CARD);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // ──────────────────────────────────────────────
    // 종목별 비중 파이차트
    // ──────────────────────────────────────────────

    private JPanel createChartPanel() {
        chartWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        chartWrapper.setOpaque(false);
        chartWrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel sectionTitle = new JLabel("  종목별 비중");
        sectionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        chartWrapper.add(sectionTitle, BorderLayout.NORTH);

        pieDataset = new DefaultPieDataset();
        populatePieData();

        pieChart = ChartFactory.createPieChart(null, pieDataset, false, true, false);
        pieChart.setBackgroundPaint(BG_CARD);

        stylePiePlot();

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        chartPanel.setMouseWheelEnabled(false);

        chartWrapper.add(chartPanel, BorderLayout.CENTER);

        return chartWrapper;
    }

    private void populatePieData() {
        pieDataset.clear();
        for (Stock s : stockList) {
            pieDataset.setValue(s.getName(), s.getEvalAmount());
        }
    }

    private void stylePiePlot() {
        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setBackgroundPaint(BG_CARD);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("맑은 고딕", Font.PLAIN, 11));
        plot.setLabelPaint(TEXT_PRIMARY);
        plot.setLabelBackgroundPaint(new Color(50, 50, 70, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelLinkPaint(TEXT_SECONDARY);

        for (int i = 0; i < stockList.size(); i++) {
            plot.setSectionPaint(stockList.get(i).getName(), PIE_COLORS[i % PIE_COLORS.length]);
        }
    }

    private void updatePieChart() {
        populatePieData();
        stylePiePlot();
    }
}
