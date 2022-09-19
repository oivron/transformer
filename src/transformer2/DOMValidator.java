package transformer2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

/* Klassen brukes til å validere output-filen fra Aftenposten. Artikler som ikke
 * validerer, blir fjernet fra filen.*/
class DOMValidator {

    /* De to første variablene under holder styr på hvor eventuelle valideringsfeil befinner seg 
     * i AftenpostenFeed.xml. errorLevel sier om det gjelder <level3> eller <level4> for hver feil.
     * (level3 tilsvarer hver artikkel i Aftenposten, mens level4 tilsvarer hver artikkel i E24. 
     * errorIndex lagrer hvilken <level3> eller <level4> feilen befinner seg, nummerert forfra.*/
    public static String[] errorLevel = {"", "", "", "", "", "", ""};
    public static int[] errorIndex = {-1, -1, -1, -1, -1, -1, -1};
    public static int j = 0; // Teller som bestemmer hvilken valideringsfeil vi jobber med av totalt antall feil.

    public void validate(String f) throws TransformerConfigurationException, TransformerException {
        String fileName = f; // Navnet på fil som skal valideres.
        File file = new File(fileName);
        String filePath = file.getAbsolutePath();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            DocumentBuilder b = factory.newDocumentBuilder();
            ErrorHandler h = new MyErrorHandler();
            b.setErrorHandler(h);
            global.insertNewRow(new Object[]{"", " Validerer fil", filePath});
            Document d = b.parse(file);
            DomParser remove = new DomParser();
            remove.removeNode(fileName, j);
            j = 0;
        } catch (ParserConfigurationException e) {
            System.out.println(e.toString());
            System.out.println("ParserException");
        } catch (SAXException e) {
            System.out.println(e.toString());
            System.out.println("SAXException");
        } catch (IOException e) {
            System.out.println(e.toString());
            System.out.println("IOException");
        }
    }

    public class MyErrorHandler implements ErrorHandler {

        public void warning(SAXParseException e) throws SAXException {
            global.insertNewRow(new Object[]{"  NB!", " Validation: warning", ""});
            try {
                System.out.println("PrintInfo");
                printInfo(e);
            } catch (IOException ex) {
                Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void error(SAXParseException e) throws SAXException {
            global.insertNewRow(new Object[]{" FEIL!", " Validation: error", ""});
            try {
                try {
                    printInfo(e);
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void fatalError(SAXParseException e) throws SAXException {
            global.insertNewRow(new Object[]{" FEIL!", " Validation: fatal error", ""});
            try {
                try {
                    printInfo(e);
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(DOMValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void printInfo(SAXParseException e) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
            global.insertNewRow(new Object[]{"", "    Linnjenummer: ", e.getLineNumber()});
            global.insertNewRow(new Object[]{"", "    Kolonnenummer: ", e.getColumnNumber()});
            global.insertNewRow(new Object[]{"", "    Melding: ", e.getMessage()});
            getInvalidNode(e.getLineNumber());
        }
    }

    public void getInvalidNode(int l) throws IOException, SAXException, TransformerConfigurationException, TransformerException {
        int line = l;

        String feedPath = global.DATA_DIR + "/tmp/AftenpostenFeed.xml";
        String feed = readFileAsString(feedPath);
        String nextLine = "";
        String nextSubLine = "";
        String level = "";
        int index = 0;
        int indexLevel3 = 0;
        int indexLevel4 = 0;

        Boolean returnDelims = false;
        StringTokenizer tokenizer = new StringTokenizer(feed, "\n", returnDelims);
        for (int i = 0; i < line; i++) {
            nextLine = tokenizer.nextToken().trim();
            nextSubLine = nextLine.substring(1, nextLine.length() - 1);
            if (nextSubLine.equals("level3")) {
                level = "level3";
                indexLevel3 = indexLevel3 + 1;
                index = indexLevel3;
            } else if (nextSubLine.equals("level4")) {
                level = "level4";
                indexLevel4 = indexLevel4 + 1;
                index = indexLevel4;
            }
        }
        errorLevel[j] = level;
        errorIndex[j] = index;
        j = j + 1;
    }

    public static String readFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
