package net.mcreator.ui.dialogs;

import io.sentry.Sentry;
import io.sentry.UserFeedback;
import io.sentry.protocol.SentryId;
import net.mcreator.ui.init.L10N;

import javax.swing.*;
import java.awt.*;

public class UserFeedbackDialog extends JDialog {

    private final JTextField emailField;
    private final JTextArea commentArea;

    public UserFeedbackDialog(JFrame parent) {
        super(parent, L10N.t("dialog.feedback.title"), true);
        if (parent.getIconImage() != null)
            this.setIconImage(parent.getIconImage());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Comments Section (Larger)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(L10N.t("dialog.feedback.comments")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        commentArea = new JTextArea(12, 40); // Increased size
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(commentArea), gbc);

        // Email Section (Optional)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        panel.add(new JLabel(L10N.t("dialog.feedback.email")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailField = new JTextField(30);
        panel.add(emailField, gbc);

        // Analytics Note
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JLabel analyticsNote = new JLabel("<html><body style='width: 400px; color: gray;'>"
                + L10N.t("dialog.feedback.analytics_note") + "</body></html>");
        analyticsNote.setFont(analyticsNote.getFont().deriveFont(Font.ITALIC, 11f));
        panel.add(analyticsNote, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton(L10N.t("dialog.feedback.cancel"));
        cancelBtn.addActionListener(e -> dispose());
        JButton sendBtn = new JButton(L10N.t("dialog.feedback.send"));
        sendBtn.addActionListener(e -> {
            if (commentArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, L10N.t("dialog.feedback.error_empty"),
                        L10N.t("dialog.feedback.error_title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            sendFeedback();
            dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(sendBtn);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        panel.add(btnPanel, gbc);

        this.add(panel);
        this.setMinimumSize(new Dimension(550, 450));
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    private void sendFeedback() {
        SentryId sentryId = Sentry.captureMessage(
                "User Feedback: " + commentArea.getText().substring(0, Math.min(commentArea.getText().length(), 50)));

        UserFeedback userFeedback = new UserFeedback(sentryId);
        userFeedback.setComments(commentArea.getText());
        if (!emailField.getText().trim().isEmpty()) {
            userFeedback.setEmail(emailField.getText());
        }
        Sentry.captureUserFeedback(userFeedback);

        JOptionPane.showMessageDialog(this, L10N.t("dialog.feedback.success_message"),
                L10N.t("dialog.feedback.success_title"), JOptionPane.INFORMATION_MESSAGE);
    }

}
