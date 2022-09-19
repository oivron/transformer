package transformer2;

import java.awt.Rectangle;
import java.io.File;
import java.util.StringTokenizer;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/* Klasse som brukes til å ta vare på variable som skal være globale. Hensikten er å kunne ha tilgang til dem
hvor som helst fra i programmet.*/
public class global {
    /* Det aktuelle uniqueName vi jobber med for øyeblikket. Det er det vi har funnet i feeden og
    som vi sjekker om finnes i uniqueNames.xml.*/

    public static String INSTALL_DIR; // Området for datafiler som programmet oppretter eller endrer underveis.
    public static String DATA_DIR; // Området for datafiler som programmet oppretter eller endrer underveis.
    public static String OUTPUT_DATA_DIR; // Området hvor outputfiler legges (input til Pipeline).
    public static String UNIQUE_NAMES_DIR; // Området hvor uniqueNames.xml leggges.

    public static String currentUniqueName = "";
    /* Inneholder en liste over nye uniqueNames (adskilt med linjeskift). Altså uniqueNames
    som finnes i feeden, men som ikke finnes i uniqueNames.xml*/
    public static String newUniqueNames = "";
    /* Brukes for å ta vare på nye uniqueNames etter tur.*/
    public static String nextUniqueName = "";
    /* Tokenizer som brukes til å lage en StringTokenizer som inneholder alle
    nye uniqueNames som ble funnet.*/
    public static StringTokenizer token;
    /* Tabellen som brukes til å vise statusinformasjon */
    public static DefaultTableModel tableModel;
    public static JTable jTable1;

    /* Setter inn en ny rad i tabellen for hver gang statusfeltet skal skrive en ny melding. */
    public static void insertNewRow(Object[] obj) {
        Object[] object = obj;
        global.tableModel.insertRow(global.tableModel.getRowCount() - 14, object);
        setTableFocus();
    }

    /* Oppdaterer fokus i tabellen etter at vi har satt inn en ny rad: */
    public static void setTableFocus() {
        int lastVisibleRowCount = 0;    // Verdi kun for initialisering.

        /* Sørger for at fokus settes riktig i tabellen. Så lenge programmet fyller ut
         * de første 14 radene, endres ikke fokus. Men etter det settes fokus til den
         * siste synlige raden. NB! Husk at det alltid er 14 tomme rader nederst i tabellen. */

        /* lastVisibleRowCount er den siste synlige raden i ScrollPane-vinduet. Dvs. totalt
         * antall rader minus de 14 tomme nederst i tabellen. */
        lastVisibleRowCount = global.tableModel.getRowCount() - 14;
        if (global.tableModel.getRowCount() > 25) {
            Rectangle rect = global.jTable1.getCellRect(lastVisibleRowCount - 24, 0, false);
            Transformer2View.jScrollPane1.getViewport().setViewPosition(rect.getLocation());
            global.jTable1.changeSelection(lastVisibleRowCount, 0, true, true);
            //global.jTable1.doLayout();
            //global.jTable1.repaint();
            global.jTable1.setFillsViewportHeight(true);
            global.jTable1.setOpaque(true);
        //global.jTable1.updateUI();
        }
    }

    /* Konverterer en relativ filbane til komplett filbane på Windows-notasjon (med backslash). */
    public static String convertFileNameWindows(File f) {
        File file = f;
        String fullPath = file.getAbsolutePath();

        return fullPath;
    }
}
