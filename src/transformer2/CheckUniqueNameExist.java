package transformer2;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

/* Denne klassen tar uniqueName-elementet vi har funnet i feeden (currentUniqueName) og ser om 
 * det også finnes i fila uniqueNames.xml.
 */
public class CheckUniqueNameExist implements ContentHandler {

    boolean isUniqueName = false;
    boolean uniqueNameExits = false;
    boolean verifyUniqueName = false;
    String feedFileName = global.DATA_DIR + "/tmp/aviserMergedReady.xml";
    private Locator locator;

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
        /* Hvis parseren har gått gjennom uniqueNames.xml uten å finne aktuelt uniqueName, skal det 
         * legges til. Men først må vi sjekke at vi ikke allerede har lagt det til i lista over nye uniqueNames.
         * Lista over nye uniqueNames er rett og slett en String hvor hvert navn er separert med linjeskift (\n).
         */
        if (!uniqueNameExits) {
            if (global.newUniqueNames.indexOf(global.currentUniqueName) <= 0) {
                /* Legger til nytt uniqueName.*/
                global.newUniqueNames = global.newUniqueNames + "\n" + global.currentUniqueName;
            }
        }
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
        String c = new String(ch, start, length);

        if (isUniqueName) { // Sjekker om det er et uniqueName-element vi har funnet.
            if (global.currentUniqueName.equals(c)) {   // Sjekker om uniqueName-elementet er lik det vi har funnet i feeden.
                uniqueNameExits = true;
            }
        } else {
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
