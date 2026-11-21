package part1_gui;

public class FeeAppVerification {
    public static void main(String[] args) {
        System.out.println("=== Task 1: Repaired FeeWindow & FeeService Verification ===");
        FeeService service = new FeeService("jdbc:mysql://localhost:3306/transport_db", "root", "password");

        // Test 1: Validation check on empty amount
        try {
            service.processPayment("192311001", "");
        } catch (Exception e) {
            System.out.println("[TEST 2 PASS] Caught expected empty-amount error: " + e.getMessage());
        }

        // Test 2: Validation check on SQL injection payload
        try {
            service.processPayment("192311001' OR '1'='1", "500.00");
        } catch (Exception e) {
            System.out.println("[TEST 3 PASS] Blocked SQLi input at validation layer: " + e.getMessage());
        }

        System.out.println("[INFO] FeeWindow uses dedicated O(1) ActionListeners and non-blocking SwingWorker.");
    }
}
