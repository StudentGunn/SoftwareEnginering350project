// Logger.java
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class Logger {

    private static final String LOG_FILE = "errors.log";
    private static final String BUG_LOG_FILE = "bugs.log";
    private static final AtomicLong bugIdCounter = new AtomicLong(1000);

    /*
     -->  Bug recorder for structured JIRA-like logging
     -->  Each bug has an auto-incremented bugId, date, module, description,
     -->  steps to reproduce, expected result, actual result, severity, and status *Final* can't be changed
     -->  Logs to bugs.log in a readable format
     */
    public static class Bug {
        public final long bugId;
        public final String date;
        public final String module;
        public final String description;
        public final String stepsToReproduce;
        public final String expectedResult;
        public final String actualResult;
        public final String severity;  // LOW, MEDIUM, HIGH, CRITICAL
        public final String status;    // OPEN, IN_PROGRESS, RESOLVED, CLOSED

        public Bug(String module, String description, String stepsToReproduce, 
                   String expectedResult, String actualResult, String severity, String status) {
            this.bugId = bugIdCounter.incrementAndGet();
            this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.module = module;
            this.description = description;
            this.stepsToReproduce = stepsToReproduce;
            this.expectedResult = expectedResult;
            this.actualResult = actualResult;
            this.severity = severity;
            this.status = status;
        }
    }

    public static void logError(Exception e) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("-----");
            pw.println("Time: " + java.time.LocalDateTime.now());
            e.printStackTrace(pw);  // writes full stack trace to the file
            pw.println();

        } catch (IOException io) {
            io.printStackTrace(); // If fails, print to console
        }
    }

    /*
     --> Logs the bug report to bugs.log 
     --> Bug ID, date, module, severity, status, description, steps to reproduce, expected and actual results
     */
    public static void logBug(Bug bug) {
        try (FileWriter fw = new FileWriter(BUG_LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("=".repeat(80));
            pw.println("BUG ID: " + bug.bugId);
            pw.println("Date: " + bug.date);
            pw.println("Module: " + bug.module);
            pw.println("Severity: " + bug.severity);
            pw.println("Status: " + bug.status);
            pw.println("-".repeat(80));
            pw.println("Description:");
            pw.println("  " + bug.description);
            pw.println();
            pw.println("Steps to Reproduce:");
            pw.println("  " + bug.stepsToReproduce);
            pw.println();
            pw.println("Expected Result:");
            pw.println("  " + bug.expectedResult);
            pw.println();
            pw.println("Actual Result:");
            pw.println("  " + bug.actualResult);
            pw.println();
            pw.println("=".repeat(80));
            pw.println();

        } catch (IOException io) {
            io.printStackTrace(); // If fails, print to console
        }
    }

    /*
    --> log the bug in one call with all fields.
    -->Returns the auto-generated bugId for reference *tracking*.
     */
    public static long reportBug(String module, String description, String stepsToReproduce,
                                  String expectedResult, String actualResult, String severity, String status) {
        Bug bug = new Bug(module, description, stepsToReproduce, expectedResult, actualResult, severity, status);
        logBug(bug);

        // If Jira integration is enabled, attempt to create an issue *Fake Jira*
        try {
            String enabled = System.getenv("JIRA_ENABLED");
            if (enabled != null && enabled.equalsIgnoreCase("true")) {
                String issueKey = postBugToJira(bug);
                if (issueKey != null) {
                    try (FileWriter fw = new FileWriter(BUG_LOG_FILE, true);
                         PrintWriter pw = new PrintWriter(fw)) {
                        pw.println("Jira Issue: " + issueKey + " (linked to Bug ID: " + bug.bugId + ")");
                        pw.println();
                    } catch (IOException ignored) { }
                }
            }
        } catch (Exception ignored) {
            // never let Jira failures break app logic; bugs are already written to file
        }

        return bug.bugId;
    }

    /*  
    --> Simulated Jira Format: create a Jira-like issue key and write a log entry.
    */
    private static String postBugToJira(Bug bug) {
        try {
            String projectKey = System.getenv("JIRA_PROJECT");
            String keyPrefix = (projectKey != null && !projectKey.isBlank()) ? projectKey : "SIM";
            String issueKey = keyPrefix + "-" + bug.bugId;

            try (FileWriter fw = new FileWriter(BUG_LOG_FILE, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println("[Simulated Jira Issue Created] " + issueKey + " for Bug ID: " + bug.bugId);
                pw.println();
            } catch (IOException ignored) { }

            return issueKey;
        } catch (Exception ex) {
            return null;
        }
    }

   

    /*
    --> Catch any exception and auto-log it as a bug
    --> Extracts module from exception stack trace, uses exception message as description,
    --> and captures full stack trace as actual result. Auto-marks as HIGH severity and OPEN status.
     */
    public static long catchAndLogBug(Exception e, String module) {
        // Extract stack trace as string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        pw.close();

        // Extract line number and method from top of stack trace if possible *error location*
        StackTraceElement[] trace = e.getStackTrace();
        String locationInfo = "";
        if (trace.length > 0) {
            StackTraceElement top = trace[0];
            locationInfo = top.getClassName() + "." + top.getMethodName() + "() at line " + top.getLineNumber();
        }

        String description = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "No message");
        String stepsToReproduce = "See stack trace for conditions. Occurred in: " + locationInfo;
        String expectedResult = "No exception thrown";
        String actualResult = stackTrace;

        String severity = mapSeverity(e, module);
        return reportBug(module, description, stepsToReproduce, expectedResult, actualResult, severity, "OPEN");
    }

    /*
     --> Overload: if module not present, attempt to grab  from stack trace, *location* 
    --> then log the bug.
     */
    public static long catchAndLogBug(Exception e) {
        String module = "UNKNOWN";
        StackTraceElement[] trace = e.getStackTrace();
        if (trace.length > 0) {
            module = trace[0].getClassName();
        }
        return catchAndLogBug(e, module);
    }

    // Explicit severity overload for callers needing manual control
    public static long catchAndLogBug(Exception e, String module, String severity) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        pw.close();

        StackTraceElement[] trace = e.getStackTrace();
        String locationInfo = "";
        if (trace.length > 0) {
            StackTraceElement top = trace[0];
            locationInfo = top.getClassName() + "." + top.getMethodName() + "() at line " + top.getLineNumber();
        }

        String description = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "No message");
        String stepsToReproduce = "See stack trace for conditions. Occurred in: " + locationInfo;
        String expectedResult = "No exception thrown";
        String actualResult = stackTrace;

        return reportBug(module, description, stepsToReproduce, expectedResult, actualResult, severity, "OPEN");
    }

    // Severity mapping by exception type and module hints
    private static String mapSeverity(Exception e, String module) {
        try {
            if (e instanceof java.sql.SQLException) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("constraint") || msg.contains("foreign key") || msg.contains("unique")) {
                    return "HIGH";
                }
                return "HIGH"; // DB failures generally high
            }
            if (e instanceof NullPointerException) {
                return "HIGH";
            }
            if (e instanceof IllegalArgumentException) {
                return "LOW";
            }
            if (e instanceof ArrayIndexOutOfBoundsException || e instanceof IndexOutOfBoundsException) {
                return "HIGH";
            }
            if (e instanceof IOException) {
                return "MEDIUM";
            }
            if (e instanceof ClassNotFoundException) {
                return "HIGH";
            }
            String m = module != null ? module.toLowerCase() : "";
            if (m.contains("loginiu") || m.contains("resturantscreen") || m.contains("mainscreen")) {
                return "MEDIUM";
            }
        } catch (Exception ignored) {
        }
        return "HIGH";
    }
}

