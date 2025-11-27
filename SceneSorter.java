// SceneSorter.java
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
/*
 --> SceneSorter is responsible for managing different UI scenes in the application.
    --> It uses a CardLayout to switch between different JPanel scenes.
    --> Scenes are added with a unique name and can be switched to by that name.
    --> This allows for easy navigation between different parts of the application (e.g., login, driver screen, admin panel).
*/

public class SceneSorter {
    private final JPanel cardsPanel;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> scenes = new HashMap<>();
    

    public SceneSorter() {
        this.cardLayout = new CardLayout();
        this.cardsPanel = new JPanel(cardLayout);
    }
    /*
    --> Allows the ability to add a new scene (JPanel) with a unique name
    --> scenes are stored in a map for easy retrieval
    --> if a duplicate scene exists, an exception is thrown
    */

    public void addScene(String name, JPanel scene) {
        if (scenes.containsKey(name)) {
            throw new IllegalArgumentException("A scene with this name already exists.");
        }
        scenes.put(name, scene);
        cardsPanel.add(scene, name);
    }
    /*
    --> switchPage method; allows to switch to a different scene by name
    --> Switches to the scene with the given name
    --> if the scene does not exist, an exception is thrown
    */

    public void switchPage(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("There is no page named " + name);
        }
        cardLayout.show(cardsPanel, name);
    }
    /*
    --> getCardsPanel method; returns the main panel containing all scenes
    --> used to integrate the SceneSorter into the main application UI
    */
    public JPanel getCardsPanel() {
        return cardsPanel;
    }
}
