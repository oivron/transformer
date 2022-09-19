package transformer2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

/* Brukes for å vise og endre innstillingsfila settings.xml. */

public class ModifySettings {

    public String showSettings(String eName) throws IOException {
        String value = null;
        Document doc = null;

        String ElementName = eName;
        String filename = global.DATA_DIR + "/xml/settings.xml";
        String filenameURI = (new File(filename).toURI().toString());
        try {
            DOMParser parser = new DOMParser();

            parser.parse(filename);
            doc = parser.getDocument();

            Element root = doc.getDocumentElement();

            //doc.createCDATASection("<!DOCTYPE document SYSTEM \"settings\" [<!ENTITY % iso-lat1 PUBLIC \"ISO 8879:1986//ENTITIES Added Latin 1//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-lat1.ent\">%iso-lat1;]>");

            NodeList groupElements = root.getElementsByTagName(ElementName);  // Liste over alle group-elementer
            //String name = groupElements.item(0).getNodeName();
            value = groupElements.item(0).getTextContent(); //Tekstverdien av elementet

            for (int i = 0; i < groupElements.getLength(); i++) {
                Node groupElement = groupElements.item(i);  // Hvert enkelt group-element
                NamedNodeMap attributes = groupElement.getAttributes(); // Attributtet til et group-element

                for (int a = 0; a < attributes.getLength(); a++) {
                    Node theAttribute = attributes.item(a);
                    String attr = theAttribute.getNodeValue();  // Attributt-verdien
                }
            }

        } catch (SAXException e) {
        }
        return value;
    }

    public void saveSettings(String a, String u) throws SAXException, IOException {
        String about = a;
        String uniqueNamesPath = u;
        /* Konverterer til URI for at filbanen skal fungere i processAftenpostenDTBook.xsl. */
        String uniqueNamesPathURI = new File(uniqueNamesPath).toURI().toString();

        String filename = global.DATA_DIR + "/xml/settings.xml";
        File outputFile = new File(filename);

        String fileBody = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><settings><about>" + about + "</about><uniqueNamesPath>" + uniqueNamesPathURI + "</uniqueNamesPath><settingsPath>" + filename + "</settingsPath><installDirectory>" + global.INSTALL_DIR + "</installDirectory></settings>";

        FileWriter fileWriter = new FileWriter(outputFile);
        fileWriter.write(fileBody);
        fileWriter.close();
    }
}
