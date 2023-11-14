package org.example;// TwoWindowsApp.java
import javax.swing.*;

public class TwoWindowsApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.show();
        });
    }
}