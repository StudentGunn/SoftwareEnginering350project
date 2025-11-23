public class TestJira {
    public static void main(String[] args) {
        System.out.println("Running Logger.reportBug test (this will attempt Jira POST if JIRA_ENABLED=true and env vars are set)...");
        long id = Logger.reportBug(
                "TestJira",
                "Automated test bug created by TestJira",
                "1) Run TestJira\n2) Observe bugs.log and Jira (if enabled)",
                "No exception expected",
                "This is a synthetic test for Jira integration",
                "MEDIUM",
                "OPEN"
        );
        System.out.println("Created local Bug ID: " + id);
        System.out.println("Check bugs.log in the project root for details.");
    }
}
