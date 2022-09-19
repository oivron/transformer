package transformer2;

import java.util.StringTokenizer;
import java.io.*;

class FixCdataAndEntities {

    /* Utgangsverdier på inn- og ut-fil.*/
    String inputFileName = null;
    String outputFileName = null;

    /*
     * Metoden fjerner <![CDATA[ ]]> fra feeden. Grunnen er at jeg ønsker å beholde tags mellom <![CDATA[ og ]]> 
     * slik at jeg kan bruke dem i transformeringen.
     * I tillegg legger metoden inn DOCTYPE som inneholder en del character entities slik at spesialtegn som 
     * &ndash; kjennes igjen.
     * Dessuten fjerner den enkelte atributter som skaper problemer og fikser problemet med frittstående & (som burde
     * vært skrevet som &amp;.
     */

    /* Metoden sjekker om & innleder en Character Entity eller om det bare er en frittstående & som i M&M,
     * Se&Hør osv. Metoden genererer en ny fil på grunnlag av den nedlastede (eks. clean.www.aftenposten.no.xml. */
    public boolean fixFormat(String file) throws FileNotFoundException, IOException {
        String avisLocalFile = file;
        String inputFile = global.DATA_DIR + "/tmp/" + avisLocalFile;
        String outputFile = global.DATA_DIR + "/tmp/clean." + avisLocalFile;

        FileWriter fileWriter = new FileWriter(outputFile);
        BufferedWriter buffWriter = new BufferedWriter(fileWriter);

        BufferedReader infile = null;
        String inLine;
        infile = new BufferedReader(new FileReader(inputFile));
        boolean returnDelims = true;   // Tar med skilletegn i StringTokenizer, dvs. &.
        String next = "";
        String previous = "";
        while ((inLine = infile.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(inLine, "&", returnDelims);
            while (tokenizer.hasMoreTokens()) {
                next = tokenizer.nextToken();
                /* Legger til Doctype etter xml-deklarasjonen. NB! To versjoner forekommer under pga. litt ulikhet
                 * i hvordan XML-deklarasjonen skrives. Noen ganger med og noen ganger uten blank foran siste ?
                 * Jeg tar hensyn til følgende Character Entities:  Latin1, Latin2, Numeric and Special Graphic og
                 * Publishing.*/
                next = next.replace("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>", "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> <!DOCTYPE feed [<!ENTITY % iso-lat1 PUBLIC \"ISO 8879:1986//ENTITIES Added Latin 1//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-lat1.ent\">  %iso-lat1; <!ENTITY % iso-pub PUBLIC \"ISO 8879:1986//ENTITIES Publishing//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-pub.ent\">  %iso-pub; <!ENTITY % iso-num PUBLIC \"ISO 8879:1986//ENTITIES Numeric and Special Graphic//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-num.ent\">  %iso-num; <!ENTITY % iso-lat2 PUBLIC \"ISO 8879:1986//ENTITIES Added Latin 2//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-lat2.ent\">  %iso-lat2;]>");
                next = next.replace("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>", "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> <!DOCTYPE feed [<!ENTITY % iso-lat1 PUBLIC \"ISO 8879:1986//ENTITIES Added Latin 1//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-lat1.ent\">  %iso-lat1; <!ENTITY % iso-pub PUBLIC \"ISO 8879:1986//ENTITIES Publishing//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-pub.ent\">  %iso-pub; <!ENTITY % iso-num PUBLIC \"ISO 8879:1986//ENTITIES Numeric and Special Graphic//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-num.ent\">  %iso-num; <!ENTITY % iso-lat2 PUBLIC \"ISO 8879:1986//ENTITIES Added Latin 2//EN//XML\" \"http://www.oasis-open.org/docbook/xmlcharent/0.3/iso-lat2.ent\">  %iso-lat2;]>");

                /* Tar bort <![CDATA[" og ]]>*/
                next = next.replace("<![CDATA[", "");
                next = next.replace("]]>", "");

                /* Tar bort noen attributter som brukes noen steder i feeden. Disse forårsaker at transformeringen feiler.*/
                next = next.replace("&lon", "");
                next = next.replace("&m", "");
                next = next.replace("&sted", "");
                next = next.replace("\u0002", "");
                next = next.replace("ahref", "a href");

                /* Sjekker bruken av & i feedene. Frittstående & erstattes av &amp; (som i M&M), 
                 * men konstruksjoner som for eksempel &laquo; ikke blir berørt.*/
                int pos = next.indexOf(";"); //pos er -1 hvis ; ikke forekommer.
                /* Første test: ingen ; forekommer, men forekomst av & */
                if (pos < 0 && !previous.equals("&")) {
                    buffWriter.write(next);
                    /* Andre test: verken ; eller & */
                } else if (pos < 0 && previous.equals("&")) {
                    buffWriter.write("amp;" + next);
                    /* Tredje test: ; forekommer */
                } else if (pos > 0) {
                    buffWriter.write(next);
                }
                previous = next;
            }
        }
        infile.close();
        buffWriter.close();
        return true;
    }
}
