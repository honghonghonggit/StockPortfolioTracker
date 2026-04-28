package com.stockportfolio;

import com.stockportfolio.view.DashboardPanel;
import com.stockportfolio.view.StockManagementPanel;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class Main {

    // 색상 상수 (앱 전체 다크 테마)
    private static final Color BG_MAIN = new Color(30, 30, 46);
    private static final Color BG_TAB = new Color(45, 45, 65);
    private static final Color BG_TAB_SELECTED = new Color(60, 60, 90);
    private static final Color TEXT_PRIMARY = new Color(230, 230, 250);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 190);
    private static final Color ACCENT_BLUE = new Color(100, 149, 237);

    public static void main(String[] args) {
        // Swing UI는 EDT(Event Dispatch Thread)에서 실행
        SwingUtilities.invokeLater(() -> {
            try {
                // 시스템 룩앤필 적용
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Stock Portfolio Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 800);
            frame.setMinimumSize(new Dimension(900, 650));
            frame.setLocationRelativeTo(null); // 화면 중앙에 배치

            // 탭 패널 구성
            JTabbedPane tabbedPane = createStyledTabbedPane();
            tabbedPane.addTab("  📊 대시보드  ", new DashboardPanel());
            tabbedPane.addTab("  📋 종목 관리  ", new StockManagementPanel());

            frame.setContentPane(tabbedPane);
            frame.setVisible(true);
        });
    }

    /**
     * 다크 테마 스타일의 탭 패널 생성
     */
    private static JTabbedPane createStyledTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(BG_MAIN);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        tabbedPane.setOpaque(true);

        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                contentBorderInsets = new Insets(0, 0, 0, 0);
                tabInsets = new Insets(10, 20, 10, 20);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                               int tabIndex, int x, int y,
                                               int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setColor(BG_TAB_SELECTED);
                } else {
                    g2.setColor(BG_TAB);
                }
                g2.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 12, 12);

                // 선택된 탭 하단 액센트 라인
                if (isSelected) {
                    g2.setColor(ACCENT_BLUE);
                    g2.fillRect(x + 8, y + h - 3, w - 16, 3);
                }
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement,
                                           int tabIndex, int x, int y,
                                           int w, int h, boolean isSelected) {
                // 탭 테두리 제거
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // 콘텐츠 테두리 제거
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                                Rectangle[] rects, int tabIndex,
                                                Rectangle iconRect, Rectangle textRect,
                                                boolean isSelected) {
                // 포커스 인디케이터 제거
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement,
                                      Font font, FontMetrics metrics,
                                      int tabIndex, String title,
                                      Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(font);
                g2.setColor(isSelected ? TEXT_PRIMARY : TEXT_SECONDARY);
                g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
                g2.dispose();
            }

            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_MAIN);
                g2.fillRect(0, 0, tabPane.getWidth(), calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight));
                g2.dispose();
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }
        });

        return tabbedPane;
    }
}
