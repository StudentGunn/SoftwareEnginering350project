import java.awt.*;
import javax.swing.JPanel;
import java.util.HashMap;
import java.util.Map;

public class SceneSorter {
    private final JPanel cardsPanel;
    // cardsPanel is directly tied to SceneSorter, and makes it easy to swap between "cards" easily.
    // Imagine the UI elements as cards, and this JPanel as a stage.
    // Cards can be easily swapped, but the stage cannot.
    // The stage is able to hold all cards, and allows easy switching between different UI elements.
    private final CardLayout cardLayout;
    // If you're imagining cardsPanel as the stage, and the scenes as the cards, imagine cardLayout as the magician.
    // CardLayout controls which card is currently visible.
    // So when something manipulates the cardLayout, it's changing which UI element is currently visible.
    // If cardLayout is told to use "Login", then it will bring up the login page.
    private final Map<String, JPanel> scenes = new HashMap<>();
    // And here's the card deck. Cards are stored here, and can be called by name to swap out cards.

    public SceneSorter() {
        this.cardLayout = new CardLayout();
        this.cardsPanel = new JPanel(cardLayout);
    }
    // cardsPanel is created as a new cardLayout, and will be used to switch between and show scenes.

    public void addScene(String name, JPanel scene) {
        if (scenes.containsKey(name)) {
            throw new IllegalArgumentException("A scene with this name already exists.");
        }
        scenes.put(name, scene);
        cardsPanel.add(scene, name);
    }
    // addScene is used to tie a method to create a UI scene to a single element (switchPage).
    // For instance, the Login logic (buildLoginPage) is currently tied to a scene titled "Login."
    // Rather than recreating the entire logic, we only need to call
    // sceneSorter.switchPage("Login");
    // To swap to the login page.
    // a scene must be added to the list using addScene before it can be swapped to.

    // This saves each scene to the scenes map, and the cardsPanel.

    public void switchPage(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("There is no page named " + name);
        }
        cardLayout.show(cardsPanel, name);
    }
    // Once a scene is added using addScene, it can be swapped to using switchPage.
    // the layout will switch to whichever scene is requested as long as it's been saved in the cardsPanel and scenes map.
    public JPanel getCardsPanel() {
        return cardsPanel;
    }
    // The cardsPanel has been explained above.
    // This line means we can easily set it up first and foremost in the createAndShow method.
}
