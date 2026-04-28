package com.stockportfolio.view;

import com.stockportfolio.model.Stock;
import com.stockportfolio.service.CsvService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * 목표 수익률 관리 패널
 * - 종목별 목표 수익률 입력/수정
 * - 현재 수익률 vs 목표 수익률 비교 (달성=초록, 미달성=빨강)
 * - 목표까지 남은 수익률 표시
 * - data/goals.csv에 저장/로드
 */
public class GoalManagementPanel extends JPanel {

    // ── 색상 상수 ──
    private static final Color BG_MAIN = new Color(30, 30, 46);
    private static final Color BG_CARD = new Color(45, 45, 65);
    private static final Color BG_TABLE = new Color(40, 40, 58);
    private static final Color TEXT_PRIMARY = new Color(230, 230, 250);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 190);
    private static final Color ACCENT_BLUE = new Color(100, 149, 237);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color ACCENT_RED = new Color(240, 80, 80);
    private static final Color ACCENT_AMBER = new Color(240, 180, 60);
    private static final Color BORDER_COLOR = new Color(60, 60, 85);
    private static final Color INPUT_BG = new Color(55, 55, 78);
    private static final Color INPUT_BORDER = new Color(80, 80, 110);
    private static final Color ACHIEVED_BG = new Color(80, 200, 120, 25);
    private static final Color NOT_ACHIEVED_BG = new Color(240, 80, 80, 25);

    private final DecimalFormat rateFormat = new DecimalFormat("+#,##0.00;-#,##0.00");
    private final DecimalFormat rateInputFormat = new DecimalFormat("#0.00");

    private final CsvService csvService;
    private List<Stock> stockList;
    private Map<String, Double> goalMap; // 종목명 → 목표수익률

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    // 요약 라벨
    private JLabel achievedCountLabel;
    private JLabel notAchievedCountLabel;
    private JLabel avgGapLabel;

    public GoalManagementPanel() {
        this.csvService = new CsvService();
        this.stockList = new ArrayList<>();
        this.goalMap = new HashMap<>();
        loadData();
        initUI();
    }

    // ──────────────────────────────────────────────
    // 데이터 로드/저장
    // ──────────────────────────────────────────────

    private void loadData() {
        try {
            List<Stock> loaded = csvService.load();
            if (!loaded.isEmpty()) {
                stockList = loaded;
            } else {
                stockList = createDummyData();
            }
        } catch (IOException e) {
            stockList = createDummyData();
        }

        try {
            goalMap = csvService.loadGoals();
        } catch (IOException e) {
            System.err.println("Goals 로드 실패: " + e.getMessage());
        }
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

    private void saveGoals() {
        try {
            csvService.saveGoals(goalMap);
            setStatus("✓ 목표 수익률 저장 완료", ACCENT_GREEN);
        } catch (IOException e) {
            setStatus("✗ 저장 실패: " + e.getMessage(), ACCENT_RED);
        }
    }

    private void setStatus(String message, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
            javax.swing.Timer timer = new javax.swing.Timer(3000, evt -> {
                statusLabel.setText(" ");
                statusLabel.setForeground(TEXT_SECONDARY);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    // ──────────────────────────────────────────────
    // UI
    // ──────────────────────────────────────────────

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(createSummaryCards(), BorderLayout.NORTH);
        centerPanel.add(createGoalTablePanel(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────
    // 상단 헤더
    // ──────────────────────────────────────────────

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
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
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setPreferredSize(new Dimension(0, 64));

        JLabel title = new JLabel("🎯 목표 수익률 관리");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.WEST);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    // ──────────────────────────────────────────────
    // 요약 카드 3개
    // ──────────────────────────────────────────────

    private JPanel createSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setOpaque(false);

        long achieved = countAchieved();
        long notAchieved = countNotAchieved();
        double avgGap = calcAvgGap();

        // 달성 종목 수
        JPanel card1 = createCard("달성 종목", null, ACCENT_GREEN);
        achievedCountLabel = (JLabel) ((JPanel) card1).getComponent(2);
        achievedCountLabel.setText(achieved + "개");
        panel.add(card1);

        // 미달성 종목 수
        JPanel card2 = createCard("미달성 종목", null, ACCENT_RED);
        notAchievedCountLabel = (JLabel) ((JPanel) card2).getComponent(2);
        notAchievedCountLabel.setText(notAchieved + "개");
        panel.add(card2);

        // 평균 잔여 수익률
        JPanel card3 = createCard("평균 잔여 수익률", null, ACCENT_BLUE);
        avgGapLabel = (JLabel) ((JPanel) card3).getComponent(2);
        avgGapLabel.setText(rateInputFormat.format(avgGap) + " %p");
        panel.add(card3);

        return panel;
    }

    private JPanel createCard(String title, String value, Color accentColor) {
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

        JLabel valueLabel = new JLabel(value != null ? value : " ");
        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    // ──────────────────────────────────────────────
    // 목표 수익률 테이블
    // ──────────────────────────────────────────────

    private JPanel createGoalTablePanel() {
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

        // 섹션 제목 + 저장 버튼
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel sectionTitle = new JLabel("  종목별 목표 수익률 설정");
        sectionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        sectionTitle.setForeground(TEXT_PRIMARY);
        topBar.add(sectionTitle, BorderLayout.WEST);

        JLabel hintLabel = new JLabel("목표수익률 셀을 더블클릭하여 편집  ");
        hintLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        hintLabel.setForeground(TEXT_SECONDARY);
        topBar.add(hintLabel, BorderLayout.EAST);

        wrapper.add(topBar, BorderLayout.NORTH);

        // 테이블 구성
        String[] columns = {"종목명", "현재 수익률", "목표 수익률", "달성 여부", "잔여 수익률"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // 목표 수익률만 편집 가능
            }
        };

        refreshTable();

        table = new JTable(tableModel);
        styleTable(table);

        // 셀 편집 완료 시 저장
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 2 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0 && row < stockList.size()) {
                    handleGoalEdit(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    /**
     * 목표 수익률 편집 처리
     */
    private void handleGoalEdit(int row) {
        String stockName = stockList.get(row).getName();
        Object value = tableModel.getValueAt(row, 2);
        String rawValue = value.toString().replace("%", "").replace(" ", "").trim();

        try {
            double targetRate = Double.parseDouble(rawValue);
            goalMap.put(stockName, targetRate);
            saveGoals();
            refreshTable();
            updateSummaryCards();
        } catch (NumberFormatException ex) {
            setStatus("⚠ 숫자를 입력하세요 (예: 15.0)", ACCENT_AMBER);
            refreshTable();
        }
    }

    /**
     * 테이블 갱신
     */
    private void refreshTable() {
        if (tableModel == null) return;

        // 편집 중인 셀이 있으면 편집 중단
        if (table != null && table.isEditing()) {
            table.getCellEditor().cancelCellEditing();
        }

        tableModel.setRowCount(0);
        for (Stock s : stockList) {
            double currentRate = s.getProfitRate();
            Double targetRate = goalMap.get(s.getName());

            String currentStr = rateFormat.format(currentRate) + "%";
            String targetStr = targetRate != null ? rateInputFormat.format(targetRate) + "%" : "미설정";

            String achievedStr;
            String gapStr;

            if (targetRate != null) {
                boolean achieved = currentRate >= targetRate;
                achievedStr = achieved ? "✅ 달성" : "❌ 미달성";
                double gap = targetRate - currentRate;
                gapStr = gap <= 0 ? "🎉 초과달성" : rateInputFormat.format(gap) + "%p 남음";
            } else {
                achievedStr = "—";
                gapStr = "—";
            }

            tableModel.addRow(new Object[]{
                    s.getName(), currentStr, targetStr, achievedStr, gapStr
            });
        }
    }

    /**
     * 요약 카드 갱신
     */
    private void updateSummaryCards() {
        if (achievedCountLabel != null) {
            achievedCountLabel.setText(countAchieved() + "개");
        }
        if (notAchievedCountLabel != null) {
            notAchievedCountLabel.setText(countNotAchieved() + "개");
        }
        if (avgGapLabel != null) {
            avgGapLabel.setText(rateInputFormat.format(calcAvgGap()) + " %p");
        }
    }

    // ──────────────────────────────────────────────
    // 통계 계산
    // ──────────────────────────────────────────────

    private long countAchieved() {
        return stockList.stream()
                .filter(s -> {
                    Double target = goalMap.get(s.getName());
                    return target != null && s.getProfitRate() >= target;
                }).count();
    }

    private long countNotAchieved() {
        return stockList.stream()
                .filter(s -> {
                    Double target = goalMap.get(s.getName());
                    return target != null && s.getProfitRate() < target;
                }).count();
    }

    private double calcAvgGap() {
        List<Double> gaps = new ArrayList<>();
        for (Stock s : stockList) {
            Double target = goalMap.get(s.getName());
            if (target != null) {
                double gap = target - s.getProfitRate();
                gaps.add(gap);
            }
        }
        return gaps.isEmpty() ? 0 : gaps.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    // ──────────────────────────────────────────────
    // 테이블 스타일링
    // ──────────────────────────────────────────────

    private void styleTable(JTable table) {
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(44);
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
        header.setPreferredSize(new Dimension(0, 42));

        // 종목명 (왼쪽 정렬)
        table.getColumnModel().getColumn(0).setCellRenderer(createRenderer(SwingConstants.LEFT, false));

        // 현재 수익률 (오른쪽, 수익률 색상)
        table.getColumnModel().getColumn(1).setCellRenderer(createProfitRateRenderer());

        // 목표 수익률 (중앙, 편집 가능 표시)
        table.getColumnModel().getColumn(2).setCellRenderer(createGoalCellRenderer());

        // 달성 여부 (중앙, 달성 색상)
        table.getColumnModel().getColumn(3).setCellRenderer(createAchievedRenderer());

        // 잔여 수익률 (오른쪽)
        table.getColumnModel().getColumn(4).setCellRenderer(createGapRenderer());

        // 목표 수익률 편집 에디터 스타일
        JTextField editorField = new JTextField();
        editorField.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        editorField.setForeground(TEXT_PRIMARY);
        editorField.setBackground(INPUT_BG);
        editorField.setCaretColor(ACCENT_BLUE);
        editorField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                new EmptyBorder(4, 8, 4, 8)
        ));
        editorField.setHorizontalAlignment(JTextField.CENTER);
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(editorField));

        // 열 너비
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
    }

    /**
     * 기본 렌더러
     */
    private DefaultTableCellRenderer createRenderer(int alignment, boolean bold) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(alignment);
                setFont(new Font("맑은 고딕", bold ? Font.BOLD : Font.PLAIN, 13));
                setForeground(TEXT_PRIMARY);
                setBackground(isSelected ? new Color(60, 60, 90) : BG_CARD);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        };
    }

    /**
     * 현재 수익률 렌더러 (양수 초록, 음수 빨강)
     */
    private DefaultTableCellRenderer createProfitRateRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("맑은 고딕", Font.BOLD, 13));
                setBorder(new EmptyBorder(0, 8, 0, 12));

                String v = value != null ? value.toString() : "";
                if (v.startsWith("+")) {
                    setForeground(ACCENT_GREEN);
                } else if (v.startsWith("-")) {
                    setForeground(ACCENT_RED);
                } else {
                    setForeground(TEXT_PRIMARY);
                }

                setBackground(isSelected ? new Color(60, 60, 90) : BG_CARD);
                return this;
            }
        };
    }

    /**
     * 목표 수익률 셀 렌더러 (편집 가능 표시)
     */
    private DefaultTableCellRenderer createGoalCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("맑은 고딕", Font.BOLD, 13));
                setBorder(new EmptyBorder(0, 8, 0, 8));

                String v = value != null ? value.toString() : "";
                if ("미설정".equals(v)) {
                    setForeground(TEXT_SECONDARY);
                    setFont(new Font("맑은 고딕", Font.ITALIC, 12));
                } else {
                    setForeground(ACCENT_AMBER);
                }

                if (isSelected) {
                    setBackground(new Color(60, 60, 90));
                } else {
                    setBackground(new Color(50, 50, 72));
                }
                return this;
            }
        };
    }

    /**
     * 달성 여부 렌더러
     */
    private DefaultTableCellRenderer createAchievedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("맑은 고딕", Font.BOLD, 13));
                setBorder(new EmptyBorder(0, 8, 0, 8));

                String v = value != null ? value.toString() : "";
                if (v.contains("달성") && !v.contains("미달성")) {
                    setForeground(ACCENT_GREEN);
                    setBackground(isSelected ? new Color(60, 60, 90) : ACHIEVED_BG);
                } else if (v.contains("미달성")) {
                    setForeground(ACCENT_RED);
                    setBackground(isSelected ? new Color(60, 60, 90) : NOT_ACHIEVED_BG);
                } else {
                    setForeground(TEXT_SECONDARY);
                    setBackground(isSelected ? new Color(60, 60, 90) : BG_CARD);
                }
                return this;
            }
        };
    }

    /**
     * 잔여 수익률 렌더러
     */
    private DefaultTableCellRenderer createGapRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("맑은 고딕", Font.PLAIN, 13));
                setBorder(new EmptyBorder(0, 8, 0, 12));

                String v = value != null ? value.toString() : "";
                if (v.contains("초과달성")) {
                    setForeground(ACCENT_GREEN);
                    setFont(new Font("맑은 고딕", Font.BOLD, 13));
                } else if (v.contains("남음")) {
                    setForeground(ACCENT_AMBER);
                } else {
                    setForeground(TEXT_SECONDARY);
                }

                setBackground(isSelected ? new Color(60, 60, 90) : BG_CARD);
                return this;
            }
        };
    }
}
