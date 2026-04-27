package com.stockportfolio.view;

import com.stockportfolio.model.Stock;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 메인 대시보드 패널
 * - 상단: 총자산, 총수익금, 전체수익률 요약 카드
 * - 하단 왼쪽: 보유종목 테이블
 * - 하단 오른쪽: 종목별 비중 파이차트
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
            new Color(100, 149, 237),  // 코발트 블루
            new Color(80, 200, 120),   // 에메랄드
            new Color(240, 180, 60),   // 골드
            new Color(160, 120, 240),  // 퍼플
            new Color(240, 100, 100),  // 코랄
            new Color(60, 200, 200),   // 틸
    };

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
    private final DecimalFormat rateFormat = new DecimalFormat("+#,##0.00;-#,##0.00");

    private List<Stock> stockList;

    public DashboardPanel() {
        this.stockList = createDummyData();
        initUI();
    }

    /**
     * 더미 데이터 생성
     */
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

    /**
     * UI 초기화
     */
    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단 요약 카드 영역
        add(createSummaryCardsPanel(), BorderLayout.NORTH);

        // 하단 컨텐츠 영역 (테이블 + 차트)
        add(createContentPanel(), BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────
    // 상단 요약 카드 3개
    // ──────────────────────────────────────────────

    private JPanel createSummaryCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setOpaque(false);

        double totalAsset = stockList.stream().mapToDouble(Stock::getEvalAmount).sum();
        double totalBuy = stockList.stream().mapToDouble(Stock::getBuyAmount).sum();
        double totalProfit = totalAsset - totalBuy;
        double totalRate = (totalBuy == 0) ? 0 : (totalProfit / totalBuy) * 100;

        panel.add(createCard("총 자산", moneyFormat.format(totalAsset) + " 원", ACCENT_BLUE));
        panel.add(createCard("총 수익금", moneyFormat.format(totalProfit) + " 원",
                totalProfit >= 0 ? ACCENT_GREEN : ACCENT_RED));
        panel.add(createCard("전체 수익률", rateFormat.format(totalRate) + " %",
                totalRate >= 0 ? ACCENT_GREEN : ACCENT_RED));

        return panel;
    }

    /**
     * 요약 카드 생성
     */
    private JPanel createCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 카드 배경 (둥근 모서리)
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // 왼쪽 악센트 바
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
        JPanel wrapper = new JPanel(new BorderLayout()) {
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
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 섹션 제목
        JLabel sectionTitle = new JLabel("  보유 종목");
        sectionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        wrapper.add(sectionTitle, BorderLayout.NORTH);

        // 테이블 데이터 구성
        String[] columns = {"종목명", "매수가", "현재가", "수익률", "평가금액"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 편집 불가
            }
        };

        for (Stock s : stockList) {
            model.addRow(new Object[]{
                    s.getName(),
                    moneyFormat.format(s.getBuyPrice()),
                    moneyFormat.format(s.getCurrentPrice()),
                    rateFormat.format(s.getProfitRate()) + "%",
                    moneyFormat.format(s.getEvalAmount()) + " 원"
            });
        }

        JTable table = new JTable(model);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
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

        // 헤더 스타일
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

                // 수익률 컬럼 색상 처리
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

        // 종목명은 왼쪽 정렬
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
        JPanel wrapper = new JPanel(new BorderLayout()) {
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
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 섹션 제목
        JLabel sectionTitle = new JLabel("  종목별 비중");
        sectionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        wrapper.add(sectionTitle, BorderLayout.NORTH);

        // 파이차트 데이터
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Stock s : stockList) {
            dataset.setValue(s.getName(), s.getEvalAmount());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null,       // 제목 (별도 라벨 사용)
                dataset,
                false,      // 범례
                true,       // 툴팁
                false       // URL
        );

        // 차트 스타일링
        chart.setBackgroundPaint(BG_CARD);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(BG_CARD);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("맑은 고딕", Font.PLAIN, 11));
        plot.setLabelPaint(TEXT_PRIMARY); // 라벨 텍스트 색상을 밝게
        plot.setLabelBackgroundPaint(new Color(50, 50, 70, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelLinkPaint(TEXT_SECONDARY); // 연결선 색상

        // 각 종목별 색상 지정
        for (int i = 0; i < stockList.size(); i++) {
            plot.setSectionPaint(stockList.get(i).getName(), PIE_COLORS[i % PIE_COLORS.length]);
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        chartPanel.setMouseWheelEnabled(false);

        wrapper.add(chartPanel, BorderLayout.CENTER);

        return wrapper;
    }
}
