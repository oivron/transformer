/*
 * Transformer2App.java
 */
package transformer2;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.xml.sax.SAXException;

/**
 * The main class of the application.
 */
public class Transformer2App extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        try {
            try {
                show(new Transformer2View(this));
            } catch (SAXException ex) {
                Logger.getLogger(Transformer2App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(Transformer2App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(Transformer2App.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Transformer2App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
        // Setter ikonet på tittellinjen.
        root.setIconImage(Toolkit.getDefaultToolkit().getImage("res/gear.gif"));
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of Transformer2App
     */
    public static Transformer2App getApplication() {
        return Application.getInstance(Transformer2App.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(Transformer2App.class, args);
    }
}
