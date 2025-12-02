import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * SceneSorter manages different UI scenes in the application using CardLayout.
 * Scenes are added with unique names and can be switched dynamically.
 * Allows easy navigation between different parts of the application (e.g., login, driver screen, admin panel).
 */
public class SceneSorter {
    private final JPanel cardsPanel;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> scenes = new HashMap<>();
    

    public SceneSorter() {
        this.cardLayout = new CardLayout();
        this.cardsPanel = new JPanel(cardLayout);
    }
    
    /**
     * Adds a new scene (JPanel) with a unique name.
     * Scenes are stored in a map for easy retrieval.
     * If a duplicate scene exists, it will be replaced with the new one.
     * @param name Unique identifier for the scene.
     * @param scene JPanel to add to the scene manager.
     */
    public void addScene(String name, JPanel scene) {
        if (scenes.containsKey(name)) {
            // Remove old scene from card panel
            JPanel oldScene = scenes.get(name);
            cardsPanel.remove(oldScene);
        }
        scenes.put(name, scene);
        cardsPanel.add(scene, name);
        // Force the panel to revalidate and repaint
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    
    /**
     * Switches to a different scene by name.
     * @param name Name of the scene to switch to.
     * @throws IllegalArgumentException if the scene does not exist.
     */
    public void switchPage(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("There is no page named " + name);
        }
        cardLayout.show(cardsPanel, name);
    }
    
    /**
     * Returns the main panel containing all scenes.
     * Used to integrate the SceneSorter into the main application UI.
     * @return The JPanel containing all managed scenes.
     */
    public JPanel getCardsPanel() {
        return cardsPanel;
    }
    
    /**
     * Returns a specific scene by name.
     * Returns null if the scene doesn't exist.
     * Allows checking if a scene exists and retrieving it for updates.
     * @param <T> The expected JPanel subtype.
     * @param name Name of the scene to retrieve.
     * @return The scene panel, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends JPanel> T getScene(String name) {
        return (T) scenes.get(name);
    }
}
