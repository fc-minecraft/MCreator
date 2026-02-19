package net.mcreator.ui.dialogs;

import net.mcreator.ui.init.DRMAuthManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class DRMLoginDialog extends JDialog {

    private final JTextField loginField;
    private final JPasswordField passwordField;
    private final JLabel statusLabel;
    private final JButton loginButton;

    private boolean authenticated = false;

    // Hardcoded dark theme colors for consistency
    private static final Color BG_COLOR = new Color(0x1E2A3C);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0x53DDFF);
    private static final Color BUTTON_TEXT_COLOR = Color.BLACK;

    public DRMLoginDialog() {
        this((Frame) null);
    }

    public DRMLoginDialog(Frame parent) {
        super(parent, "Авторизация", true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Block closing without auth (or exit app)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!authenticated) {
                    System.exit(0);
                }
            }
        });

        // Main Content Panel with Gradient or Solid Color
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = BG_COLOR;
                Color color2 = new Color(0x151E2B); // Slightly darker for gradient
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. HEADER
        JLabel headerLabel = new JLabel(
                "<html><div style='text-align: center; color: white;'>ВОЙДИ НА УЧЕБНУЮ ПЛАТФОРМУ,<br>ЧТОБЫ ПРОДОЛЖИТЬ</div></html>");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        headerPanel.add(headerLabel);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. FORM (Labels left, Fields right)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Login Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("Логин:");
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); // BOLD
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        formPanel.add(userLabel, gbc);

        // Login Field with Paste Button
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;

        JPanel loginWrapper = new JPanel(new BorderLayout());
        loginWrapper.setBackground(new Color(0x253248)); // Darker
        loginWrapper.setBorder(BorderFactory.createLineBorder(new Color(0x405060), 1));

        loginField = new JTextField();
        loginField.setPreferredSize(new Dimension(220, 40));
        loginField.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Regular font
        loginField.setForeground(Color.WHITE);
        loginField.setCaretColor(Color.WHITE);
        loginField.setBackground(new Color(0x253248));
        loginField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        loginWrapper.add(loginField, BorderLayout.CENTER);

        JButton pasteBtn = new JButton();
        pasteBtn.setText("");
        pasteBtn.setIcon(new PasteIcon(20, new Color(0x90A0B0))); // Light Gray
        pasteBtn.setToolTipText("Вставить");
        pasteBtn.setFocusPainted(false);
        pasteBtn.setBorderPainted(false);
        pasteBtn.setContentAreaFilled(false);
        pasteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pasteBtn.setPreferredSize(new Dimension(40, 40));
        pasteBtn.addActionListener(e -> loginField.paste());
        loginWrapper.add(pasteBtn, BorderLayout.EAST);

        formPanel.add(loginWrapper, gbc);

        // Password Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel passLabel = new JLabel("Пароль:");
        passLabel.setForeground(TEXT_COLOR);
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        passLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        formPanel.add(passLabel, gbc);

        // Password Field with Eye Icon
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(new Color(0x253248));
        fieldWrapper.setBorder(BorderFactory.createLineBorder(new Color(0x405060), 1));

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(220, 40));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBackground(new Color(0x253248));
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        fieldWrapper.add(passwordField, BorderLayout.CENTER);

        JToggleButton showPassBtn = new JToggleButton();
        showPassBtn.setText("");
        showPassBtn.setIcon(new EyeIcon(20, new Color(0x90A0B0)));
        showPassBtn.setSelectedIcon(new EyeIcon(20, ACCENT_COLOR));
        showPassBtn.setFocusPainted(false);
        showPassBtn.setBorderPainted(false);
        showPassBtn.setContentAreaFilled(false);
        showPassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPassBtn.setPreferredSize(new Dimension(40, 40));
        showPassBtn.addActionListener(e -> {
            if (showPassBtn.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        fieldWrapper.add(showPassBtn, BorderLayout.EAST);

        formPanel.add(fieldWrapper, gbc);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        // 3. FOOTER (Status + Button)
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(0xFF5555));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(statusLabel);

        footerPanel.add(Box.createVerticalStrut(15));

        loginButton = new JButton("Войти") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(ACCENT_COLOR.darker());
                } else {
                    g2d.setColor(ACCENT_COLOR);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Rounded corners

                // Draw text manually to ensure visibility
                g2d.setColor(BUTTON_TEXT_COLOR);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        };
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(200, 45)); // Slightly shorter
        loginButton.setPreferredSize(new Dimension(200, 45));
        loginButton.setForeground(BUTTON_TEXT_COLOR);
        loginButton.setFocusPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginButton.addActionListener(e -> performLogin());

        footerPanel.add(loginButton);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        // Enter key support
        getRootPane().setDefaultButton(loginButton);

        setContentPane(contentPanel);
        pack();
        setSize(550, 420);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    // Custom Icons with improved geometry
    private static class PasteIcon implements Icon {
        private final int size;
        private final Color color;

        public PasteIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(1.5f));

            int w = 14;
            int h = 18;
            int startX = x + (size - w) / 2;
            int startY = y + (size - h) / 2;

            // Clipboard Body
            g2d.drawRoundRect(startX, startY, w, h, 2, 2);

            // Clip
            g2d.setColor(color);
            g2d.fillRect(startX + w / 2 - 3, startY - 1, 6, 4);

            // Lines
            g2d.setColor(new Color(0x607080));
            g2d.drawLine(startX + 3, startY + 6, startX + w - 3, startY + 6);
            g2d.drawLine(startX + 3, startY + 10, startX + w - 3, startY + 10);
            g2d.drawLine(startX + 3, startY + 14, startX + w - 3, startY + 14);

            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private static class EyeIcon implements Icon {
        private final int size;
        private final Color color;

        public EyeIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2f)); // Thicker stroke

            int w = 18;
            int h = 12;
            int startX = x + (size - w) / 2;
            int startY = y + (size - h) / 2;

            // Simple Eye Shape: Arc
            java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
            path.moveTo(startX, startY + h / 2.0);
            path.quadTo(startX + w / 2.0, startY - h / 2.0, startX + w, startY + h / 2.0);
            path.quadTo(startX + w / 2.0, startY + h * 1.5, startX, startY + h / 2.0);

            g2d.draw(path);

            // Pupil
            g2d.fillOval(startX + w / 2 - 3, startY + h / 2 - 3, 6, 6);

            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private void performLogin() {
        String login = loginField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (login.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Введите логин и пароль");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Вход...");
        statusLabel.setForeground(TEXT_COLOR);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    return DRMAuthManager.login(login, pass);
                } catch (java.net.UnknownHostException e) {
                    e.printStackTrace();
                    return "Сервер не найден (DNS)";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Ошибка сети: " + e.getMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Ошибка: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        authenticated = true;
                        dispose();
                    } else {
                        statusLabel.setForeground(new Color(0xFF5555));
                        statusLabel.setText("<html><div style='text-align: center;'>" + error + "</div></html>");
                        loginButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    // ignore
                    loginButton.setEnabled(true);
                }
            }
        }.execute();
    }

    public static void showAndWait() {
        if (DRMAuthManager.validate())
            return;

        SwingUtilities.invokeLater(() -> {
            new DRMLoginDialog().setVisible(true);
        });

        // The dialog is modal, but if called from non-EDT or if we need to block main
        // startup:
        // Since it's modal and called on EDT, it blocks EDT. But application startup
        // might be on main thread.
        // We need to coordinate.
    }
}
