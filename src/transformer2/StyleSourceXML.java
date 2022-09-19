package transformer2;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

public class StyleSourceXML {

    public void fixLayout(String fName) throws IOException, TransformerConfigurationException, TransformerException {
        String fileName = fName;

        Document doc = null;

        File inFile = new File(fileName);
        File outFile = inFile;

        try {
            DOMParser parser = new DOMParser();
            parser.parse(fileName);
            doc = parser.getDocument();

            Element root = doc.getDocumentElement();

            /* Koden under ordner utseende på xml-dokumentet.*/
            String myXSLFileName = global.DATA_DIR + "/xml/stylesource.xsl";
            StreamSource stylesource = new StreamSource(new File(myXSLFileName));

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(stylesource);

            DOMSource source = new DOMSource(root);

            StreamResult result = new StreamResult((File) outFile);

            transformer.transform(source, result);

        } catch (SAXException e) {
        }
    }
}
