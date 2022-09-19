package transformer2;

import java.util.StringTokenizer;
import java.io.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

class ParseProgGuide {

    String inputFileName = null;
    String outputFileName = null;

    /*
     * Denne metoden konverterer txt-filer til xml-filer vha. StringTokenizer.
     */
    public boolean parseFile() throws FileNotFoundException, IOException, TransformerConfigurationException, TransformerException {
        for (int i = 1; i <= 8; i++) {
            inputFileName = global.DATA_DIR + "/tmp/" + i + ".txt";
            outputFileName = global.DATA_DIR + "/tmp/" + i + ".xml";

            global.insertNewRow(new Object[]{"  ", " Lagrer nedlastet fil som XML ", outputFileName});
            FileWriter fileWriter = new FileWriter(outputFileName);
            BufferedWriter buffWriter = new BufferedWriter(fileWriter);
            /* Skriver startelementene i xml-filen*/
            buffWriter.write("<");
            buffWriter.write("?xml version='1.0' encoding='ISO-8859-1'?");
            buffWriter.write(">\n");
            buffWriter.write("<Workbook>\n<DocumentProperties>\n<LastAuthor>BoJo AS</LastAuthor>\n</DocumentProperties>\n<Worksheet>\n<Table>\n");

            BufferedReader infile = null;
            String inLine;
            infile = new BufferedReader(new FileReader(inputFileName));
            boolean returnDelims = true;    // Tar med skilletegnet (tabulator) i StringTokenizer
            String element = null;  // Elementet som skal skrives til xml-filen
            String next;    // Neste token
            String previous = null; // Forrige token
            String tab = "\t";  // Delimiter/skilletegn

            /* Leser inn én og én linje i tekstfila og lager et StringTokenizer-objekt av hver av dem*/
            while ((inLine = infile.readLine()) != null) {
                buffWriter.write("<Row>\n");
                StringTokenizer tokenizer = new StringTokenizer(inLine, "\t", returnDelims);
                while (tokenizer.hasMoreTokens()) {
                    next = tokenizer.nextToken();
                    /* Next=Previous oppstår når begge inneholder tabulator-tegnet. Dvs. at vi skal ha en tom celle.*/
                    if (next.equals(previous)) {
                        element = "<Cell> </Cell>\n";
                        buffWriter.write(element);
                        /* Next inneholder tab samtidig som Previous er forskjellig fra tab. Dvs. at Next
                        skal oppfattes som et skilletegn (tab). Derfor hopper vi over denne.*/
                    } else if (next.equals(tab)) {
                        if (previous != tab) {
                        }
                        /* Normaltilfelle der vi skriver verdien av Next.*/
                    } else {
                        element = "<Cell>" + next + "</Cell>\n";
                        buffWriter.write(element);
                    }
                    previous = next;
                }
                /* Lukker elementet Row. Dvs. at vi har lest inn én rad i tekstfila.*/
                buffWriter.write("</Row>\n");
            }
            // Avslutter skriving av xml-fila med å lukke startelementene i xml-fila.
            buffWriter.write("</Table>\n</Worksheet>\n</Workbook>\n");
            infile.close();
            buffWriter.close();

            /* Erstatter ulovlig tegn i XML-filene (eks. & med &amp;)*/
            ReplaceIllegalCharacters replace = new ReplaceIllegalCharacters();
            replace.replaceCharacters(i);

            /* Endrer datoformatet slik at det stemmer overens med den originale XSL-fila.
            Alternativet hadde vært å endre XSL-fila. i er telleren i filnavnet (clean_i.xml).*/
            fixDateFormat(i);
        }
        return true;
    }

    /*
     * Fikser datoformatet slik at det stemmer med de gamle XSL-filene.
     * Dvs. endrer fra YYYY-MM-DD til YYYY-MM-DDTHH:MM:SS.
     */
    private void fixDateFormat(int i) throws TransformerConfigurationException, TransformerException {
        /* Konverterer filnavnene til URI-format (file:///c:\...) for at de skal fungere i transformasjonen.*/

        /*
        String resultFileName = global.DATA_DIR + "/tmp/aviserMergedReady.xml";
        File file = new File(resultFileName);
        String filePath = file.getAbsolutePath();

        String stylesheetURI = new File(global.DATA_DIR + "/xml/removeEmptyElements.xsl").toURI().toString();
        String sourceURI = new File(global.DATA_DIR + "/tmp/aviserMerged.xml").toURI().toString();
        String resultURI = new File(resultFileName).toString();

        global.insertNewRow(new Object[]{"", " Fjerner tomme elementer", filePath});
        transformXSLT10(stylesheetURI, sourceURI, resultURI);

        global.insertNewRow(new Object[]{"", " Åpner sammenslått fil for visning", filePath});
        String feedURI = convertToURI(new File(resultFileName));
        jEditorPane1.setPage(feedURI);
         */

        String resultFileName = global.DATA_DIR + "/tmp/dateOK_" + i + ".xml";
        File file = new File(resultFileName);
        String filePath = file.getAbsolutePath();

        String stylesheetURI = new File(global.DATA_DIR + "/xml/fixDateFormat.xsl").toURI().toString();
        String sourceURI = new File(global.DATA_DIR + "/tmp/" + "clean_" + i + ".xml").toURI().toString();
        String resultURI = new File(resultFileName).toURI().toString();
        global.insertNewRow(new Object[]{"  ", " Endrer datoformat til YYYY-MM-DDTHH:MM:SS", filePath});
        fixDateFormatTVGuiden(stylesheetURI, sourceURI, resultURI);
    }

    /*
     * Konverterer fra relativ filbane (tmp/filnavn.xml) til fult URI-format (file:///C:\.../tmp/filnavn.xml)
     */
    public String convertToURI(File path) {
        String fullPath = path.getAbsolutePath();
        fullPath = fullPath.replace('\\', '/');
        String uri = "file:///" + fullPath;
        return uri;
    }

    /*
     * Prosesserer et xml-dokument (source( med angitt xsl-fil (stylesheet) og skriver resultatet til result
     */
    public void fixDateFormatTVGuiden(String stylesheet, String source, String result) throws TransformerConfigurationException, TransformerException {
        initTransformerXSLT20();
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(new StreamSource(stylesheet));
        transformer.transform(new StreamSource(source), new StreamResult(result));
    }

    /* Initialiserer TransformerFactory slik at den bruker en XSLT 2.0 prosessor, dvs. Saxon
     * xsl-dokumentet er i versjon 2.0 og da må prosessoren også være 2.0
     */
    private void initTransformerXSLT20() {
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
    }
}
