package transformer2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

public class DomParser {

    public void addNewUniqueName(String cName, String uName) throws IOException {
        /* Metoden legger nytt uniqueName til i riktig kategori (group) i uniqueNames.xml.
         * UniqueNames.xml inneholder en liste over alle kategorier (group) med tilhørende
         * uniqueNames på denne formen:
         * <category>
         * <group id="Innenriks">
         * <uniqueName>nyheter_iriks_politikk</uniqueName>
         * <uniqueName>nyheter_iriks_nokas</uniqueName>
         * </group>
         * </category>*/

        String categoryName = cName;    // Kategori/group-navn
        String uniqueName = uName;  // UniqueName

        /* Elementene i uniqueNames.xml som vi skal jobbe med.*/
        String targetElementName = "group";
        String newElementName = "uniqueName";

        Document doc = null;
        String filename = global.UNIQUE_NAMES_DIR + "/uniqueNames.xml";
        File outputFile = new File(filename);

        try {
            /* Setter opp en DOM-parser som vil parse og finne riktig plassering til nytt uniqueName.*/
            DOMParser parser = new DOMParser();
            parser.parse(global.UNIQUE_NAMES_DIR + "/uniqueNames.xml");
            doc = parser.getDocument();

            Element root = doc.getDocumentElement();

            NodeList groupElements = root.getElementsByTagName(targetElementName);  // Liste over alle group-elementer
            for (int i = 0; i < groupElements.getLength(); i++) {
                Node groupElement = groupElements.item(i);  // Hvert enkelt group-element
                NamedNodeMap attributes = groupElement.getAttributes(); // Attributtet til et group-element

                for (int a = 0; a < attributes.getLength(); a++) {
                    Node theAttribute = attributes.item(a);
                    String attr = theAttribute.getNodeValue();  // Attributt-verdien

                    if (attr.equals(categoryName)) {
                        Element descriptionElement = doc.createElement(newElementName); // Oppretter nytt uniqueName-element
                        Text descriptionText = doc.createTextNode(uniqueName);  // Verdien til nytt uniqueName-element
                        descriptionElement.appendChild(descriptionText);    // Setter verdien inn i elementet
                        groupElement.appendChild(descriptionElement);   // Legger elementet til aktuelt group-element
                    }
                }
            }
        } catch (SAXException e) {
        }

        /* Serialize DOM tree*/
        DOMSerializer serializer = new DOMSerializer();
        serializer.setEncoding("UTF-8");
        serializer.setIndent(0);
        serializer.setLineSeparator("");

        serializer.serialize(doc, outputFile);
    }

    public boolean removeNode(String fName, int j) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        String inputFileName = fName; // Fila som skal valideres.
        int k = j; // Teller som sier hvor mange valideringsfeil vi har funnet.

        Boolean removeComplete = false;

        String outputFileName = global.OUTPUT_DATA_DIR + "/AftenpostenFeedValid.xml"; // Navnet på den validerte fila.
        File outputFile = new File(outputFileName);
        String ouputFilePath = outputFile.getAbsolutePath();
        Document doc = null;

        DOMParser parser = new DOMParser();
        parser.parse(inputFileName);
        doc = parser.getDocument();
        Element root = doc.getDocumentElement();

        int level3count = 1; // Teller som brukes til å holde orden på hvilken <level3> element vi jobber med.
        int level4count = 1; // Teller som brukes til å holde orden på hvilken <level4> element vi jobber med.

        /* Følgende setning brukes for å endre xml:lang på rot-elementet (dtbook) fra "nb-NO" til "NO".
         * Det skal visstnok fungere bedre i DAISY Pipeline. */
        root.setAttribute("xml:lang", "NO");

        /* Løkke som gjennomgåes samme antall ganger som det er feil.*/
        for (int i = 0; i < k; i++) {
            String targetElementName = DOMValidator.errorLevel[i];
            int index = DOMValidator.errorIndex[i];

            if (targetElementName.equals("level3")) {
                NodeList levelElements = root.getElementsByTagName(targetElementName);
                Element levelElement = (Element) levelElements.item(index - level3count);
                Node parentElement = levelElement.getParentNode();
                global.insertNewRow(new Object[]{"", " Fikser valideringsfeil", " Feil nr. " + (i + 1)});
                parentElement.removeChild(levelElement);
                level3count = level3count + 1;
            }
            if (targetElementName.equals("level4")) {
                NodeList levelElements = root.getElementsByTagName(targetElementName);
                Element levelElement = (Element) levelElements.item(index - level4count);
                Node parentElement = levelElement.getParentNode();
                global.insertNewRow(new Object[]{"", " Fikser valideringsfeil", " Feil nr. " + (i + 1)});
                parentElement.removeChild(levelElement);
                level4count = level4count + 1;
            }
        }

        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        format.setLineWidth(0);
        format.setPreserveSpace(true);
        format.setEncoding("ISO-8859-1");
        format.setDoctype("-//NISO//DTD dtbook 2005-3//EN", "http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd");

        FileWriter writer = new FileWriter(outputFile);
        XMLSerializer serl = new XMLSerializer(writer, format);
        serl.asDOMSerializer();
        serl.serialize(doc);
        writer.close();

        StyleSourceXML styleDoc = new StyleSourceXML();
        styleDoc.fixLayout(outputFileName);
        global.insertNewRow(new Object[]{"", " Gyldig DTBook-fil klar for produksjon i DAISY Pipeline", ouputFilePath});

        return removeComplete;
    }
}
