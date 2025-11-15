import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Logger {

    private static final String LOG_FILE = "errors.log";

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
}
