package transformer2;

import java.util.StringTokenizer;
import java.io.*;

class ReplaceIllegalCharacters {

    String inputFileName = null;
    String outputFileName = null;

    /*
     * Metoden erstatter & med &amp; i alle xml-filene. Det er viktig for at 
     * transformeringen av filene skal fungere.
     */
    public boolean replaceCharacters(int i) throws FileNotFoundException, IOException {
        inputFileName = global.DATA_DIR + "/tmp/" + i + ".xml";
        outputFileName = global.DATA_DIR + "/tmp/" + "clean_" + i + ".xml";

        global.insertNewRow(new Object[]{"  ", " Erstatter ulovlige tegn i XML-fil ", outputFileName});
        FileWriter fileWriter = new FileWriter(outputFileName);
        BufferedWriter buffWriter = new BufferedWriter(fileWriter);

        BufferedReader infile = null;
        String inLine;
        infile = new BufferedReader(new FileReader(inputFileName));
        boolean returnDelims = false;   // Tar ikke med skilletegn i StringTokenizer, dvs. \n (linjeskrift)
        String next;
        while ((inLine = infile.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(inLine, "\n", returnDelims);
            while (tokenizer.hasMoreTokens()) {
                next = tokenizer.nextToken();
                next = next.replaceAll("&", "&amp;");   // Erstatter alle forekomster av "&"
                next = next.replaceAll("<br>", "<br/>");   // Retter opp ukorrekt break-element
                buffWriter.write(next);
            }
        }
        infile.close();
        buffWriter.close();
        return true;
    }
}
