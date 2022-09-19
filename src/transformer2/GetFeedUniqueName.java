package transformer2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/* Denne klassen brukes til å parse Aftenposten-feeden. For hver forekomst av elementet uniqueName i feeden, 
 * opprettes en ny parser som skal sjekke om det aktuelle uniqueName finnes i fila uniqueNames.xml eller ikke.
*/
class GetFeedUniqueName implements ContentHandler {

    boolean isUniqueName = false;
    private Locator locator;
    /* Fila uniqueNames.xml brukes til å lagre alle uniqueNames i ulike kategorier. Under transformeringen
     av avisene brukes denne fila av XSL-fila. Derfor må denne fila være oppdatert.*/
    String uniqueNamesFile = global.UNIQUE_NAMES_DIR + "/uniqueNames.xml";

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        /* Sjekker om start-taggen i elementene som parseren finner er <uniqueName>*/
        if (localName.equals("uniqueName")) {
            isUniqueName = true;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isUniqueName) { // Sjekker om det er et uniqueName-element vi har funnet.
            try {
                String s = new String(ch, start, length);   // Tekstnoden til uniqueName-elementet vi har funnet i feeden.
                global.currentUniqueName = s;   // Setter currentUniqueName lik uniqueName-elementet vi har funnet i feeden.

                /* Oppretter en ny parser som skal finne ut om uniqueName-elementet vi fant i feeden også finnes i uniqueNames.xml.*/
                XMLReader reader = XMLReaderFactory.createXMLReader();
                CheckUniqueNameExist check = new CheckUniqueNameExist();
                reader.setContentHandler(check);

                InputSource inputSource = new InputSource(uniqueNamesFile);

                reader.parse(inputSource);

            } catch (IOException ex) {
                //Logger.getLogger(check.class.getName()).log(Level.SEVERE, null, ex);
                Logger.getLogger(Transformer2View.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        isUniqueName = false;
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
