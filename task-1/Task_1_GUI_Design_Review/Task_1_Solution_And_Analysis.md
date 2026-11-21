# TASK 1 — GUI DESIGN REVIEW (6 Marks)

## Task 1.1 — Diagnose the Design (2 Marks)
1. **Poor Separation of Concerns:** UI layout, input parsing, JDBC `DriverManager` connection, SQL execution, and `Thread.sleep(3000)` are all written inside `FeeWindow.actionPerformed()`. Business logic cannot be unit-tested without launching AWT frames.
2. **String Reference Comparison (`==`) on Action Command:** `e.getActionCommand() == "Pay"` compares object references rather than string values (`.equals()`). If a non-interned or localized command string is passed, it evaluates to `false` and jumps to `else`, wiping user inputs.
3. **Missing Input Validation & Uncaught Exceptions:** `Double.parseDouble(amount.getText())` runs with no empty, numeric, or positive-range validation; `SQLException` and `InterruptedException` are unhandled, crashing the event handler.
4. **SQL Injection Vulnerability:** String concatenation (`"INSERT INTO fees VALUES('" + regNo.getText() + "'," + a + ")"`) allows malicious input to alter SQL syntax.
5. **Blocking the Event Dispatch Thread (EDT):** Synchronous DB calls and `Thread.sleep(3000)` freeze the entire UI for 3+ seconds, causing users to click "Pay" repeatedly and trigger duplicate charges.
6. **Broken Control Flow & Resource Leaks:** Clicking "Receipt" enters the `else` branch and clears the text fields instead of displaying a receipt; `Connection` and `Statement` are never closed.

## Task 1.2 — Repair the Design (3 Marks)
- `FeeService.java`: Encapsulates `validateRegNo()`, `validateAmount()`, `processPayment()`, and `fetchReceipt()` using parameterized `PreparedStatement` and try-with-resources.
- `FeeWindow.java`: Collects inputs, binds dedicated `O(1)` `ActionListener`s per button (`payBtn`, `clearBtn`, `receiptBtn`), and executes database operations asynchronously via `SwingWorker`.

## Task 1.3 — Design Defence (1 Mark | 75 Words)
Separating `FeeWindow` from `FeeService` isolates business rules and SQL persistence from graphical widgets. Developers can unit-test `FeeService` directly using JUnit with valid, empty, and malicious inputs without instantiating AWT/Swing windows or simulating mouse clicks. Maintenance improves because database schema or validation changes require modifying only `FeeService`, while UI layout updates occur solely in `FeeWindow`. Furthermore, dedicated button listeners and `SwingWorker` background execution prevent accidental cross-button side effects and keep the interface responsive during network operations.

## Alternative Concept & Complexity Analysis
- **Solution:** Dedicated listener object / Swing `Action` per button instead of a shared listener with an `if-else` chain.
- **Dispatch Time:** Improves from $O(k)$ worst-case sequential string comparisons across $k$ buttons to $O(1)$ direct method dispatch.
- **Space:** Increases from $O(1)$ (1 listener) to $O(k)$ lightweight listener instances (~16–32 bytes each).
