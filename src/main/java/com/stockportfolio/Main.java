package com.stockportfolio;

import com.stockportfolio.view.DashboardPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
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

            // 메인 대시보드 패널 설정
            frame.setContentPane(new DashboardPanel());

            frame.setVisible(true);
        });
    }
}
