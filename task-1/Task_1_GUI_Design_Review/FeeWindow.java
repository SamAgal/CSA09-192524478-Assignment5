package part1_gui;

import javax.swing.*;
import java.awt.*;

/**
 * Repaired GUI View: Responsible strictly for collecting user input,
 * dispatching background tasks off the Event Dispatch Thread (EDT),
 * and rendering user feedback.
 */
public class FeeWindow extends JFrame {
    private final JTextField regNoField = new JTextField(15);
    private final JTextField amountField = new JTextField(15);
    private final JButton payBtn = new JButton("Pay");
    private final JButton clearBtn = new JButton("Clear");
    private final JButton receiptBtn = new JButton("Receipt");
    private final JLabel statusLabel = new JLabel("Ready");
    private final FeeService feeService;

    public FeeWindow(FeeService feeService) {
        super("Student Fee Payment Portal");
        this.feeService = feeService;
        buildLayout();
        bindEvents();
    }

    private void buildLayout() {
        setLayout(new GridLayout(4, 2, 8, 8));
        add(new JLabel("Register No:"));
        add(regNoField);
        add(new JLabel("Amount (INR):"));
        add(amountField);
        add(payBtn);
        add(clearBtn);
        add(receiptBtn);
        add(statusLabel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private void bindEvents() {
        // Dedicated O(1) listener per button instead of fragile if-else string chain
        payBtn.addActionListener(e -> handlePay());
        clearBtn.addActionListener(e -> handleClear());
        receiptBtn.addActionListener(e -> handleReceipt());
    }

    private void handlePay() {
        String regNo = regNoField.getText();
        String amount = amountField.getText();
        payBtn.setEnabled(false);
        statusLabel.setText("Processing payment...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return feeService.processPayment(regNo, amount);
            }

            @Override
            protected void done() {
                payBtn.setEnabled(true);
                try {
                    statusLabel.setText(get());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(FeeWindow.this, cause.getMessage(),
                            "Payment Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleClear() {
        regNoField.setText("");
        amountField.setText("");
        statusLabel.setText("Cleared");
    }

    private void handleReceipt() {
        String regNo = regNoField.getText();
        receiptBtn.setEnabled(false);
        new SwingWorker<FeeService.Receipt, Void>() {
            @Override
            protected FeeService.Receipt doInBackground() throws Exception {
                return feeService.fetchReceipt(regNo);
            }

            @Override
            protected void done() {
                receiptBtn.setEnabled(true);
                try {
                    FeeService.Receipt r = get();
                    String msg = String.format("RECEIPT | RegNo: %s | Paid: %.2f | Status: %s",
                            r.regNo(), r.amount(), r.status());
                    statusLabel.setText(msg);
                    JOptionPane.showMessageDialog(FeeWindow.this, msg,
                            "Fee Receipt", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText("Receipt Error: " + cause.getMessage());
                }
            }
        }.execute();
    }
}
