package com.stockportfolio.view;

import com.stockportfolio.model.Stock;
import com.stockportfolio.service.ApiService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 종목 관리 패널
 * - 왼쪽: 입력 폼 (종목명, 매수가, 보유수량) + 추가/수정/삭제 버튼
 * - 오른쪽: 등록된 종목 테이블
 * - CSV 파일 저장/불러오기 연동
 */
public class StockManagementPanel extends JPanel {

    // ── 색상 상수 (DashboardPanel과 통일) ──
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

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");

    // 서비스
    private final CsvService csvService;
    private final ApiService apiService;

    // 데이터
    private final List<Stock> stockList;

    // UI 컴포넌트
    private JTextField nameField;
    private JTextField priceField;
    private JTextField quantityField;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public StockManagementPanel() {
        this.csvService = new CsvService();
        this.apiService = new ApiService();
        this.stockList = new ArrayList<>();
        loadFromCsv();
        initUI();
    }

    // ──────────────────────────────────────────────
    // CSV 로드/저장
    // ──────────────────────────────────────────────

    private void loadFromCsv() {
        try {
            stockList.clear();
            stockList.addAll(csvService.load());
        } catch (IOException e) {
            System.err.println("CSV 로드 실패: " + e.getMessage());
        }
    }

    private void saveToCsv() {
        try {
            csvService.save(stockList);
            setStatus("✓ 저장 완료 (" + stockList.size() + "개 종목)", ACCENT_GREEN);
        } catch (IOException e) {
            setStatus("✗ 저장 실패: " + e.getMessage(), ACCENT_RED);
        }
    }

    private void setStatus(String message, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setForeground(color);

            // 3초 후 상태 메시지 초기화
            Timer timer = new Timer(3000, evt -> {
                statusLabel.setText(" ");
                statusLabel.setForeground(TEXT_SECONDARY);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    // ──────────────────────────────────────────────
    // UI 초기화
    // ──────────────────────────────────────────────

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 상단: 제목 + 상태 바
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 중앙: 입력 폼(왼쪽) + 테이블(오른쪽)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createFormPanel(), createTablePanel());
        splitPane.setDividerLocation(340);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        splitPane.setBackground(BG_MAIN);
        splitPane.setOpaque(false);

        add(splitPane, BorderLayout.CENTER);
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

        JLabel title = new JLabel("📋 종목 관리");
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
    // 왼쪽: 입력 폼 + 버튼
    // ──────────────────────────────────────────────

    private JPanel createFormPanel() {
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
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setOpaque(false);

        // 섹션 제목
        JLabel sectionTitle = new JLabel("종목 정보 입력");
        sectionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContent.add(sectionTitle);
        formContent.add(Box.createVerticalStrut(20));

        // 종목명
        nameField = createStyledTextField();
        formContent.add(createFieldGroup("종목명", nameField));
        formContent.add(Box.createVerticalStrut(14));

        // 매수가
        priceField = createStyledTextField();
        formContent.add(createFieldGroup("매수가 (원)", priceField));
        formContent.add(Box.createVerticalStrut(14));

        // 보유수량
        quantityField = createStyledTextField();
        formContent.add(createFieldGroup("보유수량 (주)", quantityField));
        formContent.add(Box.createVerticalStrut(28));

        // 버튼 그룹
        formContent.add(createButtonGroup());
        formContent.add(Box.createVerticalGlue());

        wrapper.add(formContent, BorderLayout.NORTH);
        return wrapper;
    }

    /**
     * 라벨 + 텍스트필드 그룹
     */
    private JPanel createFieldGroup(String labelText, JTextField textField) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        group.add(label);
        group.add(Box.createVerticalStrut(6));
        group.add(textField);

        return group;
    }

    /**
     * 스타일링된 텍스트 필드
     */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? ACCENT_BLUE : INPUT_BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(INPUT_BG);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(new EmptyBorder(8, 12, 8, 12));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(0, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // 포커스 시 테두리 갱신
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { field.repaint(); }
            @Override
            public void focusLost(FocusEvent e) { field.repaint(); }
        });

        return field;
    }

    /**
     * 버튼 그룹 (추가 / 수정 / 삭제)
     */
    private JPanel createButtonGroup() {
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 94));

        JButton addBtn = createStyledButton("추가", ACCENT_BLUE);
        JButton editBtn = createStyledButton("수정", ACCENT_AMBER);
        JButton deleteBtn = createStyledButton("삭제", ACCENT_RED);
        JButton fetchBtn = createStyledButton("📡 현재가 조회", ACCENT_GREEN);

        addBtn.addActionListener(e -> handleAdd());
        editBtn.addActionListener(e -> handleEdit());
        deleteBtn.addActionListener(e -> handleDelete());
        fetchBtn.addActionListener(e -> handleFetchPrice(fetchBtn));

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(fetchBtn);

        return btnPanel;
    }

    /**
     * 스타일링된 버튼
     */
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = hovered
                        ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 220)
                        : new Color(color.getRed(), color.getGreen(), color.getBlue(), 180);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 42));
        return btn;
    }

    // ──────────────────────────────────────────────
    // 오른쪽: 종목 테이블
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
        JLabel sectionTitle = new JLabel("  등록된 종목 목록");
        sectionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        wrapper.add(sectionTitle, BorderLayout.NORTH);

        // 테이블 구성
        String[] columns = {"종목명", "매수가", "현재가", "수익률", "보유수량"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshTable();

        table = new JTable(tableModel);
        styleTable(table);

        // 행 선택 시 입력 폼에 데이터 채우기
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < stockList.size()) {
                    Stock s = stockList.get(row);
                    nameField.setText(s.getName());
                    priceField.setText(String.valueOf((long) s.getBuyPrice()));
                    quantityField.setText(String.valueOf(s.getQuantity()));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CARD);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        // 하단: 종목 수 표시
        JLabel countLabel = new JLabel(" ");
        countLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_SECONDARY);
        countLabel.setBorder(new EmptyBorder(8, 4, 0, 0));
        updateCountLabel(countLabel);
        wrapper.add(countLabel, BorderLayout.SOUTH);

        // tableModel 변경 시 카운트 업데이트
        tableModel.addTableModelListener(e -> updateCountLabel(countLabel));

        return wrapper;
    }

    private void updateCountLabel(JLabel label) {
        label.setText("총 " + stockList.size() + "개 종목 등록됨");
    }

    /**
     * 테이블 데이터 갱신
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Stock s : stockList) {
            String currentPriceStr = (s.getCurrentPrice() == s.getBuyPrice())
                    ? "조회 전" : moneyFormat.format(s.getCurrentPrice()) + " 원";
            double rate = s.getProfitRate();
            String rateStr = (s.getCurrentPrice() == s.getBuyPrice())
                    ? "-" : String.format("%+.2f%%", rate);
            tableModel.addRow(new Object[]{
                    s.getName(),
                    moneyFormat.format(s.getBuyPrice()) + " 원",
                    currentPriceStr,
                    rateStr,
                    moneyFormat.format(s.getQuantity()) + " 주"
            });
        }
    }

    /**
     * 테이블 스타일 적용
     */
    private void styleTable(JTable table) {
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(70, 70, 110));
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
        header.setPreferredSize(new Dimension(0, 40));

        // 종목명: 왼쪽 정렬
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.LEFT);
                setForeground(TEXT_PRIMARY);
                setBackground(isSelected ? new Color(70, 70, 110) : BG_CARD);
                setBorder(new EmptyBorder(0, 12, 0, 8));
                return this;
            }
        };

        // 숫자: 오른쪽 정렬
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setForeground(TEXT_PRIMARY);
                setBackground(isSelected ? new Color(70, 70, 110) : BG_CARD);
                setBorder(new EmptyBorder(0, 8, 0, 12));
                return this;
            }
        };

        // 수익률 컬럼 색상 렌더러
        DefaultTableCellRenderer rateRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                String text = (value != null) ? value.toString() : "";
                if (text.startsWith("+")) setForeground(ACCENT_GREEN);
                else if (text.startsWith("-") && !text.equals("-")) setForeground(ACCENT_RED);
                else setForeground(TEXT_SECONDARY);
                setBackground(isSelected ? new Color(70, 70, 110) : BG_CARD);
                setBorder(new EmptyBorder(0, 8, 0, 12));
                return this;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rateRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
    }

    // ──────────────────────────────────────────────
    // CRUD 핸들러
    // ──────────────────────────────────────────────

    /**
     * 추가 처리
     */
    private void handleAdd() {
        Stock stock = parseInput();
        if (stock == null) return;

        // 중복 종목명 체크
        for (Stock s : stockList) {
            if (s.getName().equals(stock.getName())) {
                setStatus("⚠ 이미 등록된 종목입니다: " + stock.getName(), ACCENT_AMBER);
                return;
            }
        }

        stockList.add(stock);
        refreshTable();
        saveToCsv();
        clearFields();
        setStatus("✓ 종목 추가됨: " + stock.getName(), ACCENT_GREEN);
    }

    /**
     * 수정 처리
     */
    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            setStatus("⚠ 수정할 종목을 테이블에서 선택하세요", ACCENT_AMBER);
            return;
        }

        Stock stock = parseInput();
        if (stock == null) return;

        Stock existing = stockList.get(row);
        existing.setName(stock.getName());
        existing.setBuyPrice(stock.getBuyPrice());
        existing.setQuantity(stock.getQuantity());

        refreshTable();
        saveToCsv();
        clearFields();
        table.clearSelection();
        setStatus("✓ 종목 수정됨: " + existing.getName(), ACCENT_GREEN);
    }

    /**
     * 삭제 처리
     */
    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            setStatus("⚠ 삭제할 종목을 테이블에서 선택하세요", ACCENT_AMBER);
            return;
        }

        String name = stockList.get(row).getName();

        int confirm = JOptionPane.showConfirmDialog(this,
                "\"" + name + "\" 종목을 삭제하시겠습니까?",
                "종목 삭제 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            stockList.remove(row);
            refreshTable();
            saveToCsv();
            clearFields();
            setStatus("✓ 종목 삭제됨: " + name, ACCENT_GREEN);
        }
    }

    /**
     * 입력값 파싱 & 유효성 검증
     */
    private Stock parseInput() {
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String qtyStr = quantityField.getText().trim();

        if (name.isEmpty()) {
            setStatus("⚠ 종목명을 입력하세요", ACCENT_AMBER);
            nameField.requestFocus();
            return null;
        }

        double buyPrice;
        try {
            buyPrice = Double.parseDouble(priceStr);
            if (buyPrice <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("⚠ 매수가를 올바르게 입력하세요 (양수)", ACCENT_AMBER);
            priceField.requestFocus();
            return null;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setStatus("⚠ 보유수량을 올바르게 입력하세요 (양의 정수)", ACCENT_AMBER);
            quantityField.requestFocus();
            return null;
        }

        return new Stock(name, quantity, buyPrice);
    }

    /**
     * 입력 필드 초기화
     */
    private void clearFields() {
        nameField.setText("");
        priceField.setText("");
        quantityField.setText("");
        nameField.requestFocus();
    }

    // ──────────────────────────────────────────────
    // Alpha Vantage 현재가 조회
    // ──────────────────────────────────────────────

    /**
     * 현재가 조회 버튼 핸들러
     * - 종목명을 심볼로 변환 후 현재가를 비동기 조회
     * - 로딩 표시 + 에러 메시지 처리
     */
    private void handleFetchPrice(JButton fetchBtn) {
        if (stockList.isEmpty()) {
            setStatus("⚠ 등록된 종목이 없습니다", ACCENT_AMBER);
            return;
        }

        // 로딩 상태 표시
        fetchBtn.setEnabled(false);
        fetchBtn.setText("⏳ 조회 중...");
        setStatus("🔄 현재가를 조회하고 있습니다... (" + stockList.size() + "개 종목)", ACCENT_BLUE);

        final int totalCount = stockList.size();
        final int[] completedCount = {0};
        final int[] successCount = {0};
        final int[] failCount = {0};

        for (Stock stock : stockList) {
            String symbol = stock.getName(); // 종목명을 심볼로 사용

            apiService.fetchCurrentPrice(symbol, stock.getName(), new ApiService.ApiCallback() {
                @Override
                public void onSuccess(String stockName, double currentPrice) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        stock.setCurrentPrice(currentPrice);
                        successCount[0]++;
                        completedCount[0]++;
                        checkComplete();
                    });
                }

                @Override
                public void onFailure(String stockName, String errorMessage) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        System.err.println("[" + stockName + "] 조회 실패: " + errorMessage);
                        failCount[0]++;
                        completedCount[0]++;
                        checkComplete();
                    });
                }

                private void checkComplete() {
                    if (completedCount[0] >= totalCount) {
                        refreshTable();
                        saveToCsv();
                        fetchBtn.setEnabled(true);
                        fetchBtn.setText("📡 현재가 조회");

                        if (failCount[0] == 0) {
                            setStatus("✓ 전체 " + successCount[0] + "개 종목 현재가 조회 완료", ACCENT_GREEN);
                        } else if (successCount[0] == 0) {
                            setStatus("✗ 전체 조회 실패 (API 제한 또는 네트워크 오류)", ACCENT_RED);
                        } else {
                            setStatus("⚠ " + successCount[0] + "개 성공, " + failCount[0] + "개 실패", ACCENT_AMBER);
                        }
                    }
                }
            });
        }
    }

    /**
     * 외부에서 종목 리스트를 가져갈 수 있도록 (대시보드 연동 등)
     */
    public List<Stock> getStockList() {
        return new ArrayList<>(stockList);
    }
}
