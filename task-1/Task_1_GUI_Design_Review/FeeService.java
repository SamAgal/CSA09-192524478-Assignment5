package part1_gui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Service layer encapsulating validation, fee payment persistence,
 * and receipt retrieval completely decoupled from AWT/Swing UI components.
 */
public class FeeService {
    private final String url;
    private final String user;
    private final String password;

    public FeeService(String url, String user, String password) {
        this.url = Objects.requireNonNull(url, "DB URL required");
        this.user = Objects.requireNonNull(user, "DB user required");
        this.password = Objects.requireNonNull(password, "DB password required");
    }

    public record Receipt(String regNo, double amount, String status) {}

    public String processPayment(String regNoInput, String amountInput) throws Exception {
        String regNo = validateRegNo(regNoInput);
        double amount = validateAmount(amountInput);

        String sql = "INSERT INTO fees (reg_no, amount) VALUES (?, ?)";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, regNo);
            ps.setDouble(2, amount);
            ps.executeUpdate();
        }
        return String.format("Payment confirmed: RegNo=%s, Amount=%.2f", regNo, amount);
    }

    public Receipt fetchReceipt(String regNoInput) throws Exception {
        String regNo = validateRegNo(regNoInput);
        String sql = "SELECT reg_no, amount FROM fees WHERE reg_no = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Receipt(rs.getString("reg_no"), rs.getDouble("amount"), "PAID");
                }
            }
        }
        throw new IllegalArgumentException("No fee record found for Register No: " + regNo);
    }

    private String validateRegNo(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Register number cannot be empty.");
        }
        String trimmed = regNo.trim();
        if (!trimmed.matches("^[A-Za-z0-9]{5,15}$")) {
            throw new IllegalArgumentException("Invalid register number format (5-15 alphanumeric chars).");
        }
        return trimmed;
    }

    private double validateAmount(String amountText) {
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new IllegalArgumentException("Fee amount cannot be empty.");
        }
        try {
            double amt = Double.parseDouble(amountText.trim());
            if (Double.isNaN(amt) || Double.isInfinite(amt) || amt <= 0.0) {
                throw new IllegalArgumentException("Fee amount must be greater than zero.");
            }
            return amt;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Fee amount must be a valid numeric value.");
        }
    }
}
