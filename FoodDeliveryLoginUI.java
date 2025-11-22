import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.*;

// main UI class -> handles the app window and scene switching 
// uses SceneSorter to swap between different screens (ex:login, customer, driver, admin)
public class FoodDeliveryLoginUI {
    private final BackgroundPanel main = new BackgroundPanel();
    private final SceneSorter sceneSorter = new SceneSorter();
    public final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    public SceneSorter getSceneSorter() {
        return sceneSorter;
    }

    private final JPanel centerPanel = new JPanel(new GridBagLayout());
    private final JPanel cardPanel = new JPanel(new GridBagLayout());
    private final JFrame frame = new JFrame("Food Delivery Service");

    // load and hold database connections
    public UserDataBase userDb;
    public PaymentDatabase paymentDb;
    public DriverDatabase driverDb;
    public OrderDatabase orderDb;

    // creates and shows the main window
    public void createAndShow() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(420, 260));

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

    // sets a background image for the UI
    public void setBackgroundImage(Path imagePath) throws IOException {
        setBackgroundImage(imagePath, true);
    }

    public void setBackgroundImage(Path imagePath, boolean coverUI) throws IOException {
        Image img = javax.imageio.ImageIO.read(imagePath.toFile());
        if (img != null) {
            main.setBackgroundImage(img);
            main.setOverlayAlpha(0f);
            if (coverUI) {
                cardPanel.setOpaque(false);
                cardPanel.setBackground(new Color(0,0,0,0));
                messageLabel.setOpaque(false);
            } else {
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

    // sets background to solid color
    public void setBackgroundColor(Color c) {
        main.setBackgroundColor(c);
        main.setOverlayAlpha(0f);
        main.repaint();
    }

    // shows notification message that auto-clears
    public void showNotification(String message, Color bg, Color fg, int durationMs) {
        messageLabel.setText(message);
        messageLabel.setOpaque(true);
        if (bg != null) messageLabel.setBackground(bg);
        if (fg != null) messageLabel.setForeground(fg);
        javax.swing.Timer t = new javax.swing.Timer(Math.max(1000, durationMs), e -> {
            messageLabel.setText(" ");
            messageLabel.setOpaque(false);
            messageLabel.repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    // adjusts card transparency
    public void setCardOpacity(int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        cardPanel.setOpaque(alpha > 0);
        cardPanel.setBackground(new Color(255,255,255, alpha));
        if (messageLabel.isOpaque()) messageLabel.setBackground(cardPanel.getBackground());
        main.repaint();
    }

    // closes window
    public void closeWindow() {
        if (frame != null) {
            frame.dispose();
        }
    }

    // hashes password using SHA-256
    public static String sha256Hex(String input) {
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
            throw new RuntimeException(e);
        }
    }
}

// panel that can show a background image or solid color if needed
class BackgroundPanel extends JPanel {
    private Color bgColor = Color.decode("#f7f9fc");
    private Image bgImage = null;
    private float overlayAlpha = 0.4f;

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

// label with drop shadow for readabilily on images
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
        g2.setColor(shadowColor);
        g2.translate(offset, offset);
        super.paintComponent(g2);
        g2.dispose();
        super.paintComponent(g);
    }
}
