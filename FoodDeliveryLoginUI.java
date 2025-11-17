import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.*;

/*
 * Simple Swing login/register UI for the Food Delivery app.
 * - Top: welcome message
 * - Center: username/password fields with Login and Register
 * - Users Saved to a local SQLite database `users.db` in the working directory
 * - Passwords are stored as SHA-256 hex hashes replace with a proper KDF for production
 */
public class FoodDeliveryLoginUI {
    // use a custom panel that can draw a color or an image as background
    private final BackgroundPanel main = new BackgroundPanel();
    private final SceneSorter sceneSorter = new SceneSorter();
    public final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    public SceneSorter getSceneSorter() {
        return sceneSorter;
    }
    private final JPanel centerPanel = new JPanel(new GridBagLayout());
    // a card panel so form controls are readable over image backgrounds
    private final JPanel cardPanel = new JPanel(new GridBagLayout());
    private final JFrame frame = new JFrame("Food Delivery Service");
    public UserDataBase userDb;
    public PaymentDatabase paymentDb;
    public DriverDatabase driverDb;
    public OrderDatabase orderDb;

    public void createAndShow() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(420, 260));
    // Initialize Login UI with reference to this parent
    LoginUI log = new LoginUI(this);
    sceneSorter.addScene("Login", log.buildLoginPanel());

        main.add(sceneSorter.getCardsPanel(), BorderLayout.CENTER);
        sceneSorter.switchPage("Login");
        main.add(messageLabel, BorderLayout.SOUTH);

        frame.setContentPane(main);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    //   builds the main window and layout, including title, form fields, and
    //   buttons. CardLayout is also called here, allowing us to easily switch
    //   between UI layouts in the future. Switchpage is used to ensure the first
    //   page will always be login.

    // No CSV fallback: persistence is provided by the SQLite-backed UserDatabase only.

    // --- utilities ---

    /*
     * Set the background image programmatically from an absolute path or relative path.
     * This will disable the dim overlay so the image shows fully.
    /*Set image and by default make it cover the UI (card becomes transparent). */
    public void setBackgroundImage(Path imagePath) throws IOException {
        setBackgroundImage(imagePath, true);
    }

    /*
     * Set the background image programmatically.
     * @param imagePath path to image
     * @param coverUI if true, make the card/title/messages transparent 
     */
    public void setBackgroundImage(Path imagePath, boolean coverUI) throws IOException {
        Image img = javax.imageio.ImageIO.read(imagePath.toFile());
        if (img != null) {
            main.setBackgroundImage(img);
            main.setOverlayAlpha(0f);
            // if coverUI, make card non-opaque so image shows through everywhere
            if (coverUI) {
                cardPanel.setOpaque(false);
                cardPanel.setBackground(new Color(0,0,0,0));
                messageLabel.setOpaque(false);
            } else {
                // restore translucent card
                cardPanel.setOpaque(true);
                cardPanel.setBackground(new Color(255,255,255,220));
                messageLabel.setOpaque(true);
                messageLabel.setBackground(main.getBackground());
            }
            main.repaint();
        } else {
            throw new IOException("Unsupported image or failed to read: " + imagePath);
        }
    }

    // setBackgroundImage(imagePath, coverUI) summary:
    // - reads an image from disk using ImageIO.read (throws IOException if read fails)
    // - sets the image into BackgroundPanel and turns off overlay dimming
    // - if coverUI==true, the UI card and labels are made non-opaque so the
    //   image shows through; otherwise the translucent card is restored

    /**
     * Set a solid background color programmatically. This clears any background image.
     */
    // setBackgroundColor(Color c):
    // - c is a java.awt.Color instance and is used to paint the panel background
    // - clears any previously set background image by setting bgImage=null and
    //   switching the panel to opaque so the solid color is visible
    // - calls repaint to update the UI immediately
    public void setBackgroundColor(Color c) {
        main.setBackgroundColor(c);
        main.setOverlayAlpha(0f);
        main.repaint();
    }

    
    // setCardOpacity
    // - alpha is an integer 0..255 representing the translucency 
    //   same RGBA color with the alpha value
    // - titleLabel and messageLabel backgrounds are updated to match the card
    //   when they are opaque so visuals remain consistent
    public void setCardOpacity(int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        cardPanel.setOpaque(alpha > 0);
        cardPanel.setBackground(new Color(255,255,255, alpha));
        // ensure message backgrounds track card if opaque
        if (messageLabel.isOpaque()) messageLabel.setBackground(cardPanel.getBackground());
        main.repaint();
    }

    /** Close the main application window (used after switching to an external screen). */
    public void closeWindow() {
        if (frame != null) {
            frame.dispose();
        }
    }
    /*
    private void buildCenter() {

        // - creates the username and password label+field pairs and positions
        //   them using GridBagLayout constraints
        // - creates Login and Register buttons and attaches action listeners
        //   (loginBtn,onLogin,registerBtn ,onRegister
        // - the buttons panel is added to the center area so user can submit
        //   the form; all components are standard Swing components (JLabel,
        //   JTextField, JPasswordField, JButton) and listeners receive
        //   ActionEvent when triggered.
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST;
        centerPanel.add(new JLabel("Username:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        centerPanel.add(userField, c);

        c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.EAST;
        centerPanel.add(new JLabel("Password:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        centerPanel.add(passField, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(this::onLogin);
        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(this::onRegister);
        btns.add(loginBtn);
        btns.add(registerBtn);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        centerPanel.add(btns, c);
    }

    private void onLogin(ActionEvent e) {
        messageLabel.setForeground(Color.RED);
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Please enter username and password.");
            return;
        }

    // Hash the password input to a hex string. sha256Hex returns a String.
    // Steps:
    // 1) read password char[] from passField and convert to String
    // 2) call sha256Hex(pass) to produce a hex String
    // 3) pass the username (String) and password hash (String) to
    //    userDb.authenticate(user, hash) which compares the stored value
    // Note: both username and hash are passed as JDBC Strings to PreparedStatement.setString
        String hash = sha256Hex(pass);
        try {
            if (userDb == null) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("User database not initialized.");
                return;
            }
            // Ask the database to verify the provided password hash
            // matches the stored value for this username.
            boolean ok = userDb.authenticate(user, hash);
            if (ok) {
                messageLabel.setForeground(new Color(0, 128, 0));
                messageLabel.setText("Login successful. Welcome, " + user + "!");
                passField.setText("");
            } else {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Invalid username or password.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("DB error: " + ex.getMessage());
        }
    }

    // onLogin(ActionEvent):
    // - validates non-empty inputs
    // - computes SHA-256 hex of password
    // - calls userDb.authenticate(username, hash) to check credentials
    // - updates messageLabel with success or failure messages

    private void onRegister(ActionEvent e) {
    // Registration flow (step-level):
    // 1) Prompt for username uses (String) via JOptionPane
    // 2) Prompt for password in a JPasswordField (char[]), convert to String
    // 3) Compute password hash (sha256Hex) producing a String hex
    // 4) Call userDb.userExists which uses p.setString
    //    to bind the username as a JDBC String for the SELECT
    // 5) If not exists, call userDb.register this uses
    //    p.setString for username/hash and p.setLong for created_at
        String user = JOptionPane.showInputDialog(frame, "Choose a username:", "Register", JOptionPane.QUESTION_MESSAGE);
        if (user == null) return; // cancelled
        user = user.trim();
        if (user.isEmpty()) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Username cannot be empty.");
            return;
        }

        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(frame, pf, "Enter password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        String pass = new String(pf.getPassword()).trim();
        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hash = sha256Hex(pass);
        try {
            if (userDb == null) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("User database not initialized.");
                return;
            }
            // Check DB to ensure the username isn't already taken.
            if (userDb.userExists(user)) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Username already exists.");
                return;
            }
            // Insert the new user record into the SQLite database.
            userDb.register(user, hash);
            messageLabel.setForeground(new Color(0, 128, 0));
            messageLabel.setText("Registered successfully. You can now login.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Failed to save user: " + ex.getMessage());
        }
    }

    //  Removed.CSV fall back, not allowed only provided by the SQLite-backed UserDatabase

    // --- utilities ---
    */
    public static String sha256Hex(String input) {
        // sha256Hex(String input):
        // - input is the plaintext password string. This method:
        //   1) obtains a MessageDigest for SHA-256
        //   2) converts the input to bytes
        //   3) computes the digest (32 bytes) and converts each byte to
        //      a two-character lowercase hex representation
        // - returns the hex String representation of the hash
        // Performance optimized: uses lookup table for hex conversion
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(b.length * 2);
            final char[] hexChars = "0123456789abcdef".toCharArray();
            for (byte x : b) {
                int v = x & 0xff;
                sb.append(hexChars[v >>> 4]);
                sb.append(hexChars[v & 0x0f]);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is expected to be present; convert to unchecked if not
            throw new RuntimeException(e);
        }
    }
}

// small panel that supports a solid background color or a scaled image
class BackgroundPanel extends JPanel {
    private Color bgColor = Color.decode("#f7f9fc");
    private Image bgImage = null;
    private float overlayAlpha = 0.4f; // 0..1, higher = more dim

    public BackgroundPanel() {
        super(new BorderLayout(10, 10));
        setOpaque(true);
    }

    public void setBackgroundColor(Color c) {
        this.bgColor = c;
        this.bgImage = null;
        setOpaque(true);
    }

    public void setBackgroundImage(Image img) {
        this.bgImage = img;
        setOpaque(false);
    }

    public void setOverlayAlpha(float a) {
        this.overlayAlpha = Math.max(0f, Math.min(1f, a));
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (bgImage != null) {
            // draw image scaled to fill (preserve aspect ratio)
            int w = getWidth();
            int h = getHeight();
            double imgW = bgImage.getWidth(null);
            double imgH = bgImage.getHeight(null);
            if (imgW > 0 && imgH > 0) {
                double scale = Math.max((double) w / imgW, (double) h / imgH);
                int iw = (int) (imgW * scale);
                int ih = (int) (imgH * scale);
                int x = (w - iw) / 2;
                int y = (h - ih) / 2;
                g.drawImage(bgImage, x, y, iw, ih, this);
                // draw dark overlay to improve readability
                if (overlayAlpha > 0f) {
                    Color old = g.getColor();
                    g.setColor(new Color(0, 0, 0, Math.round(overlayAlpha * 255)));
                    g.fillRect(0, 0, w, h);
                    g.setColor(old);
                }
            } else {
                super.paintComponent(g);
            }
        } else {
            g.setColor(bgColor != null ? bgColor : getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}

/*
 * JLabel with a subtle drop shadow to improve readability over images.
*/
class ShadowLabel extends JLabel {
    private Color shadowColor = new Color(0,0,0,160);
    private int offset = 2;

    public ShadowLabel(String text) {
        super(text, SwingConstants.CENTER);
        setForeground(Color.WHITE);
        setFont(getFont().deriveFont(Font.BOLD, 16f));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // draw shadow
        g2.setColor(shadowColor);
        g2.translate(offset, offset);
        super.paintComponent(g2);
        g2.dispose();

        // draw text normally
        super.paintComponent(g);
    }
}
