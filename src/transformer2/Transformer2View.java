package transformer2;

import java.awt.Color;
import java.awt.Toolkit;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.jdesktop.application.Task;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.snipecode.reg.RegUtil;

/**
 * The application's main frame.
 */
public class Transformer2View extends FrameView {

    /* Vriable og konstanter for Aftenposten. */
    int feedIndex;
    String[] NEWSPAPER_NAME = {"Aftenposten", "E24", "Fotball"};
    String[] aviser = {"Aftenposten", "E24", ""};  // Aftenposten og E24 produseres som default
    String[] NEWSPAPER_URL = {"http://www.aftenposten.no/eksport/nlb/?key=oopsadaisy", "http://www.aftenposten.no/eksport/nlb/?key=oopsadaisy&utvalg=E24", null};
    String[] NEWSPAPER_LOCAL_FILE_NAME = {"www.aftenposten.no.xml", "www.e24.no.xml", "www.fotball.no.xml"};
    /* Variabel for TVGuiden og RadioGuiden. */
    String progGuideNavn = "";
    /* Andre konstanter. */

    public Transformer2View(SingleFrameApplication app) throws IOException, SAXException, TransformerConfigurationException, TransformerException {
        super(app);

        initRegistry();
        initComponents();
        initTable();    // Initialiserer tabellen som brukes i Statusfeltet.
        initProductions(); // Setter Aftenposten som default produksjon.

        /* Forhindre at programmet husker programstørrelsen (bredde og høyde) som brukeren har valgt. */
        this.getFrame().setResizable(false);

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    /*
     * *************************************************************************
     * ************************** Initialiseringer *****************************
     * *************************************************************************
     */

    /* Initialiserer konstanter som tar vare på register-verdiene for Transformer. Dette er filbanene til de
     * ulike områdene som programmet bruker.
     */
    private void initRegistry() {
        int[] ret = RegUtil.RegOpenKey(RegUtil.HKEY_CURRENT_USER, "SOFTWARE\\Bojo\\Transformer\\2.1\\Settings", RegUtil.KEY_QUERY_VALUE);
        int handle = ret[RegUtil.NATIVE_HANDLE];

        // get the Number of Values in the key
        int[] info = RegUtil.RegQueryInfoKey(handle);
        int count = info[RegUtil.VALUES_NUMBER];
        int maxlen = info[RegUtil.MAX_VALUE_NAME_LENGTH];

        /* Initialiserer de fire verdiene som tomme. */
        String[] stringValue = {"", "", "", ""};

        /* For-løkke som henter registerverdiene. */
        for (int index = 0; index < count; index++) {
            // get the Name of a key
            // Note to use 1 greater than the length returned by query
            byte[] name = RegUtil.RegEnumValue(handle, index, maxlen + 1);

            // Get its Value
            byte[] values = RegUtil.RegQueryValueEx(handle, name);
            stringValue[index] = new String(values).trim();
        }
        // Finally Close the handle
        RegUtil.RegCloseKey(handle);

        /* Plukker ut strengeverdiene som inneholder bane til filområder på lokal maskin/server. */
        global.INSTALL_DIR = stringValue[0];  // Installasjonsområdet.
        global.DATA_DIR = stringValue[1];  // Området for datafiler.
        global.OUTPUT_DATA_DIR = stringValue[2];   // Området hvor outputfiler legges (dvs. input til Pipeline).
        global.UNIQUE_NAMES_DIR = stringValue[3];   // Området hvor uniqueNames.xml leggges.
    }

    /* Initialiserer tabellen som brukes til å vise statusinformasjon. */
    private void initTable() {
        /* Konstruer tabell. Definerer tabellen manuelt. */
        global.tableModel = new DefaultTableModel();
        global.jTable1 = new JTable(global.tableModel);

        /* Opprett kolonner med kolonnetitler */
        global.tableModel.addColumn("Status");
        global.tableModel.addColumn("Melding");
        global.tableModel.addColumn("Detaljer");

        /* Definer bredde på kolonnene */
        global.jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn colFirst = global.jTable1.getColumnModel().getColumn(0);
        colFirst.setPreferredWidth(46);
        TableColumn colSecond = global.jTable1.getColumnModel().getColumn(1);
        colSecond.setPreferredWidth(290);
        TableColumn colThird = global.jTable1.getColumnModel().getColumn(2);
        colThird.setPreferredWidth(640);

        global.jTable1.setGridColor(Color.LIGHT_GRAY);

        /* Legger tabellen i en ScrollPane. */
        jScrollPane1.add(global.jTable1);
        jScrollPane1.setViewportView(global.jTable1);

        /* Fyller ScrollPane-vinduet med tomme rader. */
        int visibleRows = 14;  //Det antall rader det er plass til i ScrollPane-vinduet.
        for (int i = 0; i < visibleRows; i++) {
            /* Legger til rad med tre tomme celler. */
            global.tableModel.addRow(new Object[]{"", "", ""});
        }
    }

    /* Det finnes en TabbedPane som inneholder faner som dekker alle produksjoner, dvs.
     * Aftenposten, TVGuiden og RadioGuiden. Ulike faner for ulike produksjoner. Jeg hhv.
     * skjuler/viser faner avhengig av hvilken produksjon som velges. Aftenposten er
     * standard og vises automatisk ved oppstart. */
    private void initProductions() throws IOException, SAXException, TransformerConfigurationException, TransformerException {
        flushDocWindow();
        jTabbedPane1.removeAll();   // Fjerner først alle faner.
        jTabbedPane1.insertTab("Aftenposten", null, jPanel1, null, 0);
        jTabbedPane1.insertTab("Innstillinger", null, jPanel2, null, 1);
        settAvisInnst(); //Fyller ut feltene i fanen Innstillinger med verdier fra fil.
        /* Lagrer avisinnstillingene her (uten at endringer har skjedd) for å sørge for at settings.xml blir oppdatert
         * med riktig filbane til uniqueNames.xml */
        lagreAvisInnst();
    }

    /*
     * *************************************************************************
     * ************************ Forskjellige metoder ***************************
     * *************************************************************************
     */

    /* Viser hvilke mappeområder som programmet bruker. */
    @Action
    public void visFilBaner() {
        global.insertNewRow(new Object[]{"", " Transformer bruker følgende filbaner:", ""});
        global.insertNewRow(new Object[]{"", "    Programmets installasjonsområde", global.INSTALL_DIR});
        global.insertNewRow(new Object[]{"", "    Programmets dataområde", global.DATA_DIR});
        global.insertNewRow(new Object[]{"", "    Mappe for uniqueNames (felles)", global.UNIQUE_NAMES_DIR});
        global.insertNewRow(new Object[]{"", "    Mappe for output til DAISY Pipeline", global.OUTPUT_DATA_DIR});
    }

    /* Gjør klar for å produsere Aftenposten ved å vise relevante faner i TabbedPane.*/
    @Action
    public void velgAftenposten() throws IOException {
        flushDocWindow();
        jTabbedPane1.removeAll();   // Fjerner først alle faner.
        jTabbedPane1.insertTab("Aftenposten", null, jPanel1, null, 0);
        jTabbedPane1.insertTab("Innstillinger", null, jPanel2, null, 1);
        settAvisInnst(); //Fyller ut feltene i fanen Innstillinger med verdier fra fil.
        initTable();
    }

    /* Gjør klar for å produsere TVGuiden ved å vise relevante faner i TabbedPane.*/
    @Action
    public void velgTVGuiden() throws IOException {
        flushDocWindow();
        progGuideNavn = "TVGuiden";
        jTabbedPane1.removeAll();   // Fjerner først alle faner.
        jTabbedPane1.insertTab("TVGuiden", null, jPanel4, null, 0);
        /* Fanen Innstillinger (under) er ikke ferdig utviklet for TVGuiden. Men grensesnittet er ferdig og kan vises
         * ved å fjerne kommentaren foran linjen under.*/
        //jTabbedPane1.insertTab("Innstillinger", null, jPanel3, null, 1);
        String folderDate = setCurrentDate();   // Henter dagens dato
        jTextField1.setText(folderDate);
        initTable();
    }

    /* Gjør klar for å produsere RadioGuiden ved å vise relevante faner i TabbedPane.*/
    @Action
    public void velgRadioGuiden() throws IOException {
        flushDocWindow();
        progGuideNavn = "RadioGuiden";
        jTabbedPane1.removeAll();   // Fjerner først alle faner.
        jTabbedPane1.insertTab("RadioGuiden", null, jPanel5, null, 0);
        /* Fanen Innstillinger (under) er ikke ferdig utviklet for RadioGuiden. Men grensesnittet er ferdig og kan vises
         * ved å fjerne kommentaren foran linjen under.*/
        //jTabbedPane1.insertTab("Innstillinger", null, jPanel3, null, 1);
        String folderDate = setCurrentDate();   // Henter dagens dato
        jTextField2.setText(folderDate);
        initTable();
    }

    @Action
    public void removeRows() {
        global.tableModel.getDataVector().removeAllElements();
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Transformer2App.getApplication().getMainFrame();
            aboutBox = new Transformer2AboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Transformer2App.getApplication().show(aboutBox);
    }

    /* Åpner lesmeg.txt i Notepad.*/
    @Action
    public void visLesMeg() throws IOException {
        String fileName = global.INSTALL_DIR + "/lesmeg.txt";
        File file = new File(fileName);
        //String fileUri = convertToURI(file);
        String fileUri = file.toURI().toString();
        global.insertNewRow(new Object[]{"", " Viser LesMeg.txt", fileName});
        jEditorPane1.setPage(fileUri);
    }

    /* Brukes for å velge en fil eller mappe fra maskinen. */
    @Action
    public File getFile() throws IOException, SAXException, TransformerConfigurationException, TransformerException {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Velg ny mappe for UniqueNames");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        chooser.setAcceptAllFileFilterUsed(false);

        File file = null;
        int option = chooser.showOpenDialog(getFrame());
        if (option == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            /* Oppdaterer den globale konstanten som tar vare på uniqueNames-pathen.
             * Samtidig oppdateres den samme pathen som vises under Innstillinger-fanen.
             * I tillegg gjør jeg et kall til metoden lagreAvisInnst for å være sikker på 
             * at den nye pathen blir lagret i settings.xml.
             */
            global.UNIQUE_NAMES_DIR = file.getAbsolutePath();
            jTextField7.setText(global.UNIQUE_NAMES_DIR);
            lagreAvisInnst();
            global.insertNewRow(new Object[]{"", " Ny plassering av uniqueNames.xml", global.UNIQUE_NAMES_DIR});

            /* Registeret må oppdateres med den nye filbanen. */
            updateRegistry();
        }
        return file;
    }

    /* Oppdatere registeret etter at filbanen til uniqueNames er endret. */
    public void updateRegistry() {
        int handle = RegUtil.RegOpenKey(RegUtil.HKEY_CURRENT_USER, "SOFTWARE\\Bojo\\Transformer\\2.0\\Settings", RegUtil.KEY_ALL_ACCESS)[RegUtil.NATIVE_HANDLE];

        RegUtil.RegSetValueEx(handle, "UniqueNamesDir", global.UNIQUE_NAMES_DIR);

        RegUtil.RegCloseKey(handle);
    }

    /* Metoden henter en fil fra en URL og lagrer den lokalt.*/
    @Action
    public void saveFile(String u, String f, String a) throws IOException, MalformedURLException {
        URL url = new URL(u);
        String fileName = f;
        String lokalFil = global.DATA_DIR + "/tmp/" + fileName;
        String avis = a;
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(lokalFil));
            conn = url.openConnection();
            in = conn.getInputStream();
            int size = conn.getContentLength();
            //byte[] buffer = new byte[size];
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            global.insertNewRow(new Object[]{"", " Laster ned", convertToURI(new File(fileName))});
            //while ((numRead = in.read(buffer)) != -1) {
            while ((numRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
        } catch (IOException ex) {
            global.insertNewRow(new Object[]{" FEIL!", " Feil ved lagring av", avis});
        }
    }

    public void actionPerformed(ActionEvent e) {
        jButton9.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
    }

    /* Brukes til å lukke en dialog, dvs. klikk på Avbryt-knappen. */
    @Action
    public void avbrytDialog() {
        //KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        //JRootPane rootPane = new JRootPane();
        //rootPane.registerKeyboardAction(jButton9.getAction(), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        jDialog3.setVisible(false);
        jDialog4.setVisible(false);
    }

    /* Fikser utseende på xml-filer slik at det ser strukturert ut. */
    @Action
    public void fixLayout(String fName) throws IOException, SAXException, TransformerConfigurationException, TransformerException {
        String fileName = fName;

        StyleSourceXML styleDoc = new StyleSourceXML();
        styleDoc.fixLayout(fileName);
    }

    /* Finner dagens dato på formen YYYYMMDD.*/
    public String setCurrentDate() {
        /* Det opprinnelig formatet ser slik ut: String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";*/
        String DATE_FORMAT_NOW = "yyyyMMdd";
        Calendar cal = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String currentDate = sdf.format(cal.getTime()).toString();
        return currentDate;
    }

    /* FIXME NB! DENNE BØR ERSTATTES AV EN ANNEN MÅTE! (eks. String fileUri = file.toURI().toString();
     * Konverterer fra relativ filbane (tmp/filnavn.xml) til fult URI-format (file:///C:\.../tmp/filnavn.xml)
     */
    public String convertToURI(File path) {
        String fullPath = path.getAbsolutePath();
        fullPath = fullPath.replace('\\', '/');
        String uri = "file:///" + fullPath;
        return uri;
    }

    public void flushDocWindow() throws IOException {
        String emptyFileName = global.DATA_DIR + "/xml/emptyPage.xml";
        File emptyFile = new File(emptyFileName);
        String emptyFileUri = emptyFile.toURI().toString();
        jEditorPane1.setPage(emptyFileUri);
    }

    /* Sletter filer når programmet avsluttes for at det ikke skal bli liggende igjen gamle filer.*/
    public void deleteFilesOnExit() {
        File folder = new File(global.DATA_DIR + "/tmp");
        String[] children = folder.list();
        for (int i = 0; i < children.length; i++) {
            File file = new File(global.DATA_DIR + "/tmp/" + children[i]);
            try {
                file.deleteOnExit();
            } catch (Exception e) {
                global.insertNewRow(new Object[]{" FEIL!", e.getMessage(), file});
            }
        }
    }

    /* Finner navnene på de 8 tekstfilene som skal lastes ned.*/
    public String getFileNames() {
        String fileNames = "";
        String DATE_FORMAT_NOW = "yyyyMMdd";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);

        String currentDate = "20010101"; // Setter en initialiseringsverdi.
        if (progGuideNavn.equals("TVGuiden")) {
            currentDate = jTextField1.getText(); //Leser inn dato fra tekstfelt i dialog.
        }
        if (progGuideNavn.equals("RadioGuiden")) {
            currentDate = jTextField2.getText(); //Leser inn dato fra tekstfelt i dialog.
        }
        /* Splitter innlest dato i år, mnd og dag, og konverterer til heltall.*/
        int year = Integer.valueOf(currentDate.substring(0, 4));
        int month = Integer.valueOf(currentDate.substring(4, 6));
        int day = Integer.valueOf(currentDate.substring(6, 8));
        month = month - 1;  //Av en eller annen grunn blir månedsnummeret for høyt, så jeg må trekke fra én.

        cal.set(year, month, day);
        currentDate = sdf.format(cal.getTime()).toString();

        /* Henter 8 filer for TVGuiden*/
        if (progGuideNavn.equals("TVGuiden")) {
            fileNames = "NLB" + currentDate + ".txt\n";   //Legger til første filnavn
            for (int i = 0; i < 7; i++) {
                cal.add(Calendar.DAY_OF_WEEK, 1);   //Øker med 1 til neste dato.
                String newDate = sdf.format(cal.getTime()).toString();
                fileNames = fileNames + "NLB" + newDate + ".txt\n";  //Legge til neste filnavn.
            }
            /* Henter 8 filer for RadioGuiden*/
        } else if (progGuideNavn.equals("RadioGuiden")) {
            fileNames = currentDate + "_tv.txt\n";   //Legger til første filnavn
            for (int i = 0; i < 7; i++) {
                cal.add(Calendar.DAY_OF_WEEK, 1);   //Øker med 1 til neste dato.
                String newDate = sdf.format(cal.getTime()).toString();
                fileNames = fileNames + newDate + "_tv.txt\n";  //Legge til neste filnavn.
            }
        }
        return fileNames;
    }

    /*
     * *************************************************************************
     * ************************* XSL Transformasjon ****************************
     * *************************************************************************
     */

    /* Transformasjon med en XSL-prosessor som støtter XSLT versjon 2.0*/
    public void transformXSLT20(String stylesheet, String source, String result) throws TransformerConfigurationException, TransformerException {
        initTransformerXSLT20();
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(new StreamSource(stylesheet));
        transformer.transform(new StreamSource(source), new StreamResult(result));
    }

    /* Transformasjon med en XSL-prosessor som støtter XSLT versjon 1.0*/
    public void transformXSLT10(String stylesheet, String source, String result) throws TransformerConfigurationException, TransformerException {
        initTransformerXSLT10();
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(new StreamSource(stylesheet));
        transformer.transform(new StreamSource(source), new StreamResult(result));
    }

    /* Velger Saxon som XSL-prosessor*/
    private void initTransformerXSLT20() {
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
    }

    /* Velger Xalan som XSL-prosessor*/
    private void initTransformerXSLT10() {
        System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
    }

    /*
     * *************************************************************************
     * ************************** Radio- og TVGuiden ***************************
     * *************************************************************************
     */

    /*
     * Brukes til Radio og TVGuiden. Her brukes en Task som kjøres i bakgrunnen. Det er en
     * fordel siden nedlasting av filer og konvertering til XML tar litt tid.
     * http://jakarta.apache.org/commons
     */
    @Action
    public Task jakarta() {
        return new JakartaTask(getApplication());
    }

    private class JakartaTask extends org.jdesktop.application.Task<Object, Void> {

        JakartaTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() throws IOException, TransformerConfigurationException, TransformerException, URISyntaxException {
            flushDocWindow(); //Tømmer programvindu.

            JakartaFtpWrapper ftp = new JakartaFtpWrapper();
            String serverName = "193.75.33.253";    //IP til Universums ftp-server
            String user = "nlb";    //Brukernavn
            String pwd = "rtv"; //Passord

            String folder = "20000101"; // Setter en initialiseringsverdi.
            if (progGuideNavn.equals("TVGuiden")) {
                folder = jTextField1.getText(); // Henter mappenavn fra datofeltet i arkfanen.
            }
            if (progGuideNavn.equals("RadioGuiden")) {
                folder = jTextField2.getText(); // Henter mappenavn fra datofeltet i arkfanen.
            }
            global.insertNewRow(new Object[]{"", " " + progGuideNavn + " produseres for åtte dager ", folder.substring(6, 8) + "." + folder.substring(4, 6) + "." + folder.substring(0, 4) + " (startdato)"});

            exist:  //Label for break-statement
            try {
                if (ftp.connectAndLogin(serverName, user, pwd)) {
                    global.insertNewRow(new Object[]{"", " Tilkoblet Universums server ", serverName});
                    try {
                        ftp.setPassiveMode(true);
                        ftp.ascii();
                        // Filer ligger under mappen rtvFeedNLB (TVGuiden) eller rtvFeedStart (RadioGuiden)
                        if (progGuideNavn.equals("TVGuiden")) {
                            ftp.changeWorkingDirectory("rtvFeedNLB/tablaa");
                        } else if (progGuideNavn.equals("RadioGuiden")) {
                            if (!ftp.changeWorkingDirectory("rtvFeedStart/" + folder)) {
                                global.insertNewRow(new Object[]{"  FEIL!", " Program for denne perioden eksisterer ennå ikke! ", ""});
                                break exist;    //Break-statement som bryter ved label "exist", dvs. try-løkka
                            }
                        }
                        global.insertNewRow(new Object[]{"  ", " Velger riktig mappe på server ", ftp.printWorkingDirectory()});
                        /* Lager en liste over filnavnene i mappen*/
                        String inputFiles = getFileNames();
                        int fileNumber = 0;
                        String nextFile = null;
                        StringTokenizer tokenizer = new StringTokenizer(inputFiles);
                        /* Laster ned alle filene i mappen */
                        while (tokenizer.hasMoreTokens()) {
                            fileNumber = fileNumber + 1;
                            nextFile = tokenizer.nextToken();
                            try {
                                global.insertNewRow(new Object[]{"  ", " Laster ned fil ", nextFile});
                                if (ftp.downloadFile(nextFile, global.DATA_DIR + "/tmp/" + fileNumber + ".txt")) {
                                } else {
                                    global.insertNewRow(new Object[]{"  FEIL!", " Filen finnes ikke ", nextFile});
                                }
                            } catch (FTPConnectionClosedException fTPConnectionClosedException) {
                                global.insertNewRow(new Object[]{"  ", " Forbindelsen ble lukket ", serverName});
                            }
                        }
                    } catch (Exception ftpe) {
                        ftpe.printStackTrace();
                    } finally {
                        ftp.logout();
                        ftp.disconnect();
                    }

                    /* Lagrer de nedlastede filene som XML.*/
                    ParseProgGuide parse = new ParseProgGuide();
                    try {
                        if (parse.parseFile()) {
                            /* Ingen statements i denne if-løkka. */
                        }
                    } catch (MalformedURLException e) {
                        //logger.log(Level.WARNING, "File.toURI().toURL() failed", e);
                    }

                    /* Merger alle XML-filene. */
                    String resultFileName = global.DATA_DIR + "/tmp/" + progGuideNavn + "Merged.xml";
                    File file = new File(resultFileName);
                    String filePath = file.getAbsolutePath();

                    String stylesheetURI = new File(global.DATA_DIR + "/xml/mergeDocs.xsl").toURI().toString();
                    String sourceURI = new File(global.DATA_DIR + "/xml/mergeDocs.xml").toURI().toString();
                    String resultURI = new File(resultFileName).toURI().toString();

                    global.insertNewRow(new Object[]{"  ", " Slår sammen XML-filer", filePath});
                    transformXSLT20(stylesheetURI, sourceURI, resultURI);

                    /* Forbereder grunnlagsfil som skal brukes i DAISY Pipeline. */
                    String resultFileNameXHTML = global.OUTPUT_DATA_DIR + "/" + progGuideNavn + "Feed.xhtml";
                    File fileXHTML = new File(resultFileNameXHTML);
                    String filePathXHTML = fileXHTML.getAbsolutePath();

                    String xslFileURI = new File(global.DATA_DIR + "/xml/process" + progGuideNavn + ".xsl").toURI().toString();
                    String mergedFileURI = new File(global.DATA_DIR + "/tmp/" + progGuideNavn + "Merged.xml").toURI().toString();
                    String feedURI = new File(resultFileNameXHTML).toURI().toString().replace("%20", " ");
                    String feedURItmp = new File(global.DATA_DIR + "/tmp/" + progGuideNavn + "Feed.html").toURI().toString();

                    global.insertNewRow(new Object[]{"  ", " Transformerer til XHTML", filePathXHTML});
                    transformXSLT10(xslFileURI, mergedFileURI, feedURI); //For produksjon i Pipeline
                    transformXSLT10(xslFileURI, mergedFileURI, feedURItmp); //For visning på skjerm

                    /* Åpner grunnlagsfil for visning. NB! Har egentlig ingen praktisk betydning
                    annet enn å vise resultatet av transformasjonen.*/
                    jEditorPane1.setPage(feedURItmp);
                    //jTextArea1.append("DAISY Pipeline input-fil: \\output\\" + progGuideNavn + "Feed.xhtml\n");
                    global.insertNewRow(new Object[]{"", " Klar for produksjon i DAISY Pipeline", filePathXHTML});
                    global.insertNewRow(new Object[]{"", " Fullført!", ""});

                    /* Sletter filene i mappen tmp når programmet avsluttes.*/
                    deleteFilesOnExit();

                } else {
                    //jTextArea1.append("Ikke mulig å koble opp mot" + serverName + "\n");
                    global.insertNewRow(new Object[]{"  ", " Ikke mulig å koble opp mot ", serverName});

                }
            } catch (IOException e) {
                //jTextArea1.append("Ikke mulig å koble opp mot" + serverName + "\n");
                global.insertNewRow(new Object[]{"  ", " Ikke mulig å koble opp mot ", serverName});
                e.printStackTrace();
            }

            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    /*
     * *************************************************************************
     * ****************************** Aftenposten *******************************
     * *************************************************************************
     */

    /*
     * Fyller ut de ulike feltene under fanen Innstillinger.
     */
    public void settAvisInnst() throws IOException {
        ModifySettings mod = new ModifySettings();
        //jTextField1.setText(mod.showSettings("title")); "Title" brukes ikke lengre.
        jEditorPane4.setText(mod.showSettings("about"));
        jTextField7.setText(global.UNIQUE_NAMES_DIR);
    }

    /*
     * Lagrer endringer i feltene under fanen Innstillinger.
     */
    @Action
    public void lagreAvisInnst() throws IOException, SAXException, TransformerConfigurationException, TransformerException {
        String fileName = global.DATA_DIR + "/xml/settings.xml";
        File file = new File(fileName);
        ModifySettings mod = new ModifySettings();
        mod.saveSettings(jEditorPane4.getText(), jTextField7.getText());
        global.insertNewRow(new Object[]{"", " Lagrer innstillinger", file.getAbsolutePath()});

        StyleSourceXML styleDoc = new StyleSourceXML();
        styleDoc.fixLayout(fileName);
    }

    /* FIXME Åpner uniqueNames.xml i programvinduet. */
    @Action
    public void visUniqueNames() throws IOException, TransformerConfigurationException, TransformerException {
        /* Henter filbanen fra tekstfeltet og kobler det med filnavnet. */
        String filePath = global.UNIQUE_NAMES_DIR + "/uniqueNames.xml";

        File file = new File(filePath);
        if (file.exists()) {
            /* Fikser utseende på xml-fil slik at det ser strukturert ut.*/
            StyleSourceXML styleDoc = new StyleSourceXML();
            //String resultURI = new File(resultFileName).toString();
            //String resultURI = new File(resultFileName).toURI().toString();
            styleDoc.fixLayout(filePath);

            global.insertNewRow(new Object[]{"", " Viser lagrede UniqueNames", file.getAbsolutePath()});
            String feedURI = new File(filePath).toURI().toString();
            jEditorPane1.setPage(feedURI);
        } else {
            global.insertNewRow(new Object[]{"", " Finner ingen uniqueNames.xml i denne mappen", file.getAbsolutePath()});
            flushDocWindow();
        }
    }

    /* Task for Aftenposten. Først åpnes en dialog der man velger hvilke aviser som skal produseres. Deretter
     * lastes de valgte avisene ned vha. UrlRetrieve-klassen. Etter nedlastingen kjøres noen sjekker for å
     * sjekke at filene er fullstendig nedlastet i tillegg til at filene forberedes for transformering.
     */
    @Action
    public Task lastNedAftenposten() throws MalformedURLException, IOException, TransformerConfigurationException, TransformerException {
        return new AftenpostenTask(getApplication());
    }

    private class AftenpostenTask extends org.jdesktop.application.Task<Object, Void> {

        AftenpostenTask(org.jdesktop.application.Application app) throws MalformedURLException, IOException, TransformerConfigurationException, TransformerException {
            super(app);
        }

        @Override
        protected Object doInBackground() throws MalformedURLException, IOException, TransformerConfigurationException, TransformerException, SAXException, Exception {
            initAviser();

            lastNedAviser();

            deleteFilesOnExit();    //Sletter filene i tmp-mappa når programmet avsluttes
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            global.insertNewRow(new Object[]{"  OK", " Nedlasting fullført. Søk etter nye uniqueNames.",});
        }
    }

    /* Metoden forbereder Aftenposten-task med å slette filer og tømme grensesnittet for tekst. */
    public void initAviser() throws IOException {
        flushDocWindow();   // Tømmer dokumentvinduet.
        settAvisInnst();

        /* Sletter fil under output for å unngå spørsmål om den skal overskrives. */
        File oldXmlFile = new File(global.DATA_DIR + "/tmp/AftenpostenFeed.xml");
        String oldXmlFileFullPath = oldXmlFile.getAbsolutePath();
        global.insertNewRow(new Object[]{"", " Sletter fil fra forrige kjøring", oldXmlFileFullPath});
        try {
            oldXmlFile.delete();
        } catch (Exception e) {
            global.insertNewRow(new Object[]{" FEIL!", e.getMessage(), oldXmlFileFullPath});
        }

        /* Sletter først gamle ready-filene i tmp-mappa slik at de ikke ødelegger for ny produksjon.
         * NB! Telleren i teller bare fra 0 til 1 (ikke 2). Det betyr at Fotballen ikke tas med.
         */
        for (int i = 0; i <= 1; i++) {
            File file = new File(global.DATA_DIR + "/tmp/ready." + NEWSPAPER_LOCAL_FILE_NAME[i]);
            String fileFullPath = file.getAbsolutePath();
            global.insertNewRow(new Object[]{"", " Sletter fil fra forrige kjøring", fileFullPath});
            try {
                file.delete();
            } catch (Exception e) {
                global.insertNewRow(new Object[]{" FEIL!", e.getMessage(), fileFullPath});
            }
        }
    }

    /* Laster ned og sjekker nedlastede filer. */
    public void lastNedAviser() throws IOException, TransformerConfigurationException, TransformerException, SAXException {
        /* Laster ned valgte aviser. aviser[i] skal inneholde avis-navnet hvis checkboksen i dialogen er
         * valgt (i motsatt fall er den null). Hvis f.eks. aviser[1] er lik "Aftenposten" vil if-testen bli oppfylt.
         * I motsatt fall opprettes bare en tom fil for at merge-metoden skal fungere (den er avhengig av å finne alle filene).
         * NB! Telleren i teller bare fra 0 til 1 (ikke 2). Det betyr at Fotballen ikke tas med.
         */
        for (int i = 0; i <= 1; i++) {   //Løkke for alle avisene.
            if (aviser[i].equals(NEWSPAPER_NAME[i])) {
                global.insertNewRow(new Object[]{"", " Laster ned fra " + NEWSPAPER_NAME[i], NEWSPAPER_URL[i]});
                try {
                    hentAviser(i);
                } catch (Exception e) {
                    global.insertNewRow(new Object[]{" FEIL!", e.getMessage(), NEWSPAPER_URL[i]});
                }

            } else {
                /* Oppretter en tom fil emptyFile med innhold fileBody. Nødvendig for at merge-metoden skal virke.*/
                String fileBody = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><feed><artikkel></artikkel></feed>";
                File emptyFile = new File(global.DATA_DIR + "/tmp/ready." + NEWSPAPER_LOCAL_FILE_NAME[i]);
                try {
                    FileWriter fileWriter = new FileWriter(emptyFile);
                    fileWriter.write(fileBody);
                    fileWriter.close();
                } catch (Exception e) {
                    global.insertNewRow(new Object[]{" FEIL!", e.getMessage(), emptyFile});
                }
            }
        }

        /* Sjekker om nedlastingen var vellykket og forbereder filene for transformasjon.
         * NB! Telleren i teller bare fra 0 til 1 (ikke 2). Det betyr at Fotballen ikke tas med.*/
        for (int i = 0; i <= 1; i++) {
            /* aviser[i] skal inneholde avis-navnet hvis checkboksen i dialogen er
             * valgt (i motsatt fall er den null). Hvis f.eks. aviser[1] er lik "Aftenposten"
             * vil if-testen bli oppfylt. Aftenposten er forresten alltid avkrysset.
             */
            if (aviser[i].equals(NEWSPAPER_NAME[i])) {
                if (checkIfCompleteDownload(i)) {    //Sjekker om filene ble fullstendig lastet ned.
                    File file = new File(global.DATA_DIR + "/tmp/" + NEWSPAPER_LOCAL_FILE_NAME[i]);
                    global.insertNewRow(new Object[]{"", " Kontrollerer nedlasting", file.getAbsolutePath()});

                    /* Feeden har hver artikkel inne i et CDATA-element. De må fjernes for at
                    transformasjonen skal kunne behandle det som er inne i disse.*/
                    FixCdataAndEntities fix = new FixCdataAndEntities();
                    File cleanFile = new File(global.DATA_DIR + "/tmp/clean." + NEWSPAPER_LOCAL_FILE_NAME[i]);
                    String cleanFilePath = cleanFile.getAbsolutePath();
                    global.insertNewRow(new Object[]{"", " Fikser tegnkodingsfeil i nedlastet fil", cleanFilePath});
                    try {
                        fix.fixFormat(NEWSPAPER_LOCAL_FILE_NAME[i]);    //Fjerner CDATA og div annet.
                    } catch (Exception e) {
                        global.insertNewRow(new Object[]{" FEIL!", e.getMessage(), ""});
                    }

                    /* Fjerner elementer som ikke kan/skal transformeres (<preform>).*/
                    try {
                        removeElements(i);
                    } catch (IOException iOException) {
                        global.insertNewRow(new Object[]{" FEIL!", iOException.getMessage(), ""});
                    }

                } else {
                    global.insertNewRow(new Object[]{"", " Feil ved nedlasting av fil. Prøv på nytt", NEWSPAPER_URL[i]});
                }
            }
        }

        /* Kaller metoden som merger avisfeedene. Hvis aviser[] er null betyr det at avisen ikke
         * skal produseres. Sjekker bare om aviser[0] er null (Aftenposten). Hvis denne er tom,
         * skal ikke avisen produseres i det hele tatt.
         */
        if (aviser[0].equals("Aftenposten")) {
            mergeFeeds();
            /* Fjerner tomme elementer i den mergede feeden. De kan skape problemer for transformeringen.*/
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
        }
    }

    /* XXX NB! KUN FOR TESTING! */
    @Action
    public void testStyleDoc2() throws IOException, TransformerConfigurationException, TransformerException {
        StyleSourceXML styleDoc = new StyleSourceXML();
        String file = "xml/uniqueNames.xml";
        styleDoc.fixLayout(file);

        String feedURI = convertToURI(new File(file));
        jEditorPane1.setPage(feedURI);
    }

    /* Laster ned avisene som ble valgt. Kaller metoden saveFile i klassen UrlRetrieve.*/
    public void hentAviser(int i) throws MalformedURLException, IOException, TransformerConfigurationException, TransformerException, Exception {
        int j = i;
        String fileName = global.DATA_DIR + "/tmp/" + NEWSPAPER_LOCAL_FILE_NAME[j];
        File file = new File(fileName);
        String filePath = file.getAbsolutePath();
        URL sourceURL = new URL(NEWSPAPER_URL[j]);
        UrlRetrieve get = new UrlRetrieve();
        global.insertNewRow(new Object[]{"", " Lagrer fil lokalt", filePath});
        get.saveFile(sourceURL, filePath);
    }

    /* Sjekker om du ulike avis-feedene er fullstendig lastet ned.*/
    public boolean checkIfCompleteDownload(int i) throws FileNotFoundException, IOException {
        int j = i;
        String inputFileName = global.DATA_DIR + "/tmp/" + NEWSPAPER_LOCAL_FILE_NAME[j];
        String stopTag = "/feed>";
        String inLine;

        BufferedReader infile = null;
        String next = null;
        Boolean complete = false;
        infile = new BufferedReader(new FileReader(inputFileName));
        while ((inLine = infile.readLine()) != null) {
            /* Lager et StringTokenizer-objekt av hver linje i feeden. Sjekker så at /feed faktisk finnes
            og konkluderer  dermed at nedlastingen av fila er fullstendig.*/
            StringTokenizer tokenizer = new StringTokenizer(inLine, "<");
            while (tokenizer.hasMoreTokens()) {
                next = tokenizer.nextToken();
                if (next.equals(stopTag)) { // Sammenligner hvert token med stopTag (/feed).
                    complete = true;
                }
            }
        }
        return complete;
    }

    /* Metoden fjerner elementer fra feeden som ikke kan transformeres. Resultatet skriver jeg til
     * en ny fil som starter med ready. Det er disse filene som brukes videre i prosessen.*/
    public void removeElements(int i) throws IOException {
        int j = i;
        String inputFileName = global.DATA_DIR + "/tmp/clean." + NEWSPAPER_LOCAL_FILE_NAME[j];
        String outputFileName = global.DATA_DIR + "/tmp/ready." + NEWSPAPER_LOCAL_FILE_NAME[j];
        File file = new File(outputFileName);
        String filePath = file.getAbsolutePath();

        FileWriter fileWriter = new FileWriter(outputFileName);
        BufferedWriter buffWriter = new BufferedWriter(fileWriter);
        BufferedReader infile = null;
        String inLine;

        infile = new BufferedReader(new FileReader(inputFileName));
        inLine = infile.readLine();

        /* Det som ligger i preform-elementene skaper problemer for transformasjonen.
        Jeg fjerner dem vha. replaceAll med et Regular Expression.*/
        global.insertNewRow(new Object[]{"", " Fjerner elementer som ikke skal/kan transformeres", filePath});
        String ny = inLine.replaceAll("<preform>" + "(.*?)" + "</preform>", "\n\n");

        buffWriter.write(ny);
        infile.close();
        buffWriter.close();
    }

    /*
     * Merger alle XML-filene, dvs. filene som har filnavn som starter med "ready". Merger filene for
     * Aftenposten og E24 (og evt. Fotball) uansett om de er lastet ned eller om de bare er tomme. Det er
     * nødvendig for at xsl-fila som merger skal fungere.
     */
    public void mergeFeeds() throws TransformerConfigurationException, TransformerException {
        String resultFileName = global.DATA_DIR + "/tmp/aviserMerged.xml";
        File file = new File(resultFileName);
        String filePath = file.getAbsolutePath();

        //String stylesheetURI = convertToURI(new File(global.DATA_DIR.replace(" ", "%20") + "/xml/mergeDocs.xsl"));
        String stylesheetURI = new File(global.DATA_DIR + "/xml/mergeDocs.xsl").toURI().toString();
        String sourceURI = new File(global.DATA_DIR + "/xml/mergeDocsAviser.xml").toURI().toString();
        String resultURI = new File(resultFileName).toURI().toString();

        global.insertNewRow(new Object[]{"", " Slår sammen nedlastede filer", filePath});
        transformXSLT20(stylesheetURI, sourceURI, resultURI);
    }

    /* Task for oppdatering av nye uniqueNames. Først finner den ett og ett uniqueName i hver av feedene.
     * For hvert uniqueName som den finner, sammenligner den med uniqueNames.xml for å se om det allerede
     * finnes der. Hvis det ikke finnes fra før, skrives det til uniqueNames.xml.
     */
    @Action
    public Task UniqueNames() {
        return new UniqueNamesTask(getApplication());
    }

    private class UniqueNamesTask extends org.jdesktop.application.Task<Object, Void> {

        UniqueNamesTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() throws SAXException, IOException, InterruptedException {
            flushDocWindow();
            File file = new File(global.DATA_DIR + "/tmp/aviserMergedReady.xml");
            if (file.exists()) { // bare hvis feeder er lastet ned
                /* Indexen går bare fra 0 til 1 (ikke 2) som betyr at Fotballen ikke tas med.*/
                for (feedIndex = 0; feedIndex <= 1; feedIndex++) {
                    File localFile = new File(global.DATA_DIR + "/tmp/" + NEWSPAPER_LOCAL_FILE_NAME[feedIndex]);
                    String localFilePath = localFile.getAbsolutePath();
                    global.insertNewRow(new Object[]{"", " Søker etter nye uniqueNames i " + NEWSPAPER_NAME[feedIndex], localFilePath});
                    String feed = global.DATA_DIR + "/tmp/ready." + NEWSPAPER_LOCAL_FILE_NAME[feedIndex];
                    findUniqueNames(feed, feedIndex);  // Søker etter nye uniqueNames
                    global.newUniqueNames = "";
                }
            } else {
                global.insertNewRow(new Object[]{"", " Finner ingen filer. Last ned avisfeedene først.", ""});
            }

            deleteFilesOnExit();    //Sletter filene i tmp-mappa når programmet avsluttes
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            global.insertNewRow(new Object[]{"  OK", " Fullført søk etter nye uniqueNames. Start produksjon.", ""});
        }
    }

    /*
     * Kaller parse-metoden som finner nye uniqueNames og åpner dialog som spør hvilken kategori de tilhører.
     */
    public void findUniqueNames(String f, int index) throws SAXException, IOException {
        String feed = f;
        int i = index;
        findUniqueNamesInFeed(feed);

        /* Etter at jeg har søkt gjennom feedene etter nye uniqueNames, må jeg knytte hvert uniqueName til
         * de tilgjengelige kategoriene. Bruker et globalt StringTokenizer-objekt for å oppbevare nye uniqueNames.
         * Da har jeg tilgang til dette objektet hvor som helst fra.*/
        global.token = new StringTokenizer(global.newUniqueNames);
        if (global.token.hasMoreElements()) {
            global.nextUniqueName = global.token.nextToken();   // nextUniqueName tar vare på  nye uniqueNames etter tur.
            /* Kalle searchForArticle som leter etter en eksempelartikkel for aktuelt uniqueName.*/
            try {
                searchForArticle(global.nextUniqueName);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(Transformer2View.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(Transformer2View.class.getName()).log(Level.SEVERE, null, ex);
            }
            /* Åpner dialogen der man velger hvilken kategori de nye uniqueNames skal ligge i.*/
            uniqueNameDialog();

        } else {
            global.insertNewRow(new Object[]{"", " Finner ingen nye uniqueNames i " + aviser[i], ""});
        }

    }

    /*
     * Brukes til å finne uniqueNames i feeden. Setter opp en parser som leter i hver av feedene.
     */
    public void findUniqueNamesInFeed(String f) throws SAXException, IOException {
        /* String feedURI = "tmp/aviserMergedReady.xml"; // Den mergede feeden.*/
        String feedURI = f; // Den mergede feeden.

        /* Setter opp en parser som skal finne uniqueName-elementene i feeden. Parseren
        ligger i klassen GetFeedUniqueName.java.*/
        XMLReader reader = XMLReaderFactory.createXMLReader();
        GetFeedUniqueName get = new GetFeedUniqueName();
        reader.setContentHandler(get);
        InputSource inputSource = new InputSource(feedURI);

        /* Parser feeden. Parseren i klassen GetFeedUniqueName.java setter opp en ny parser
        som sjekker om aktuelt uniqueName eksisterer fra før (klassen CheckUniqueNameExist).*/
        reader.parse(inputSource);

        /* Vi har nå en global string global.newUniqueNames som inneholder alle nye uniqueNames som er funnet.
         * Setter opp en StringTokenizer som går gjennom global.newUniqueNames. Lager først en
         * StringBuffer-representasjon av feeden. Denne brukes når vi skal sjekke om et nytt uniqueName
         * faktisk finnes i feeden. Viser en liste på skjermen over alle nye uniqueNames.
         */
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader read = new BufferedReader(new FileReader(feedURI));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = read.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }

        read.close();

        String verifiedNewUniqueNames = ""; // Nye uniqueNames som er verifisert
        String next = ""; // Neste element i StringTokenizer, dvs. neste nye uniqueName
        String nextElement; // next skrevet om på formen: <uniqueName>next</uniqueName>

        /* StringTokenizer som går gjennom nye uniqueNames */
        StringTokenizer tokenizer = new StringTokenizer(global.newUniqueNames);
        while (tokenizer.hasMoreTokens()) {
            next = tokenizer.nextToken();
            nextElement = "<uniqueName>" + next + "</uniqueName>";
            /* Sjekker om neste element i global.newUniqueNames faktisk finnes i feeden. */
            if (fileData.indexOf(nextElement) > -1) {
                global.insertNewRow(new Object[]{"", " Nytt uniqueName funnet ", next});
                /* Bygger opp en streng som skal inneholde alle nye uniqueNames som er verifisert. */
                verifiedNewUniqueNames = verifiedNewUniqueNames + next + "\n";
                //System.out.println("Next: " + next);
                //System.out.println("VerifiedNewUniqueNames:\n" + verifiedNewUniqueNames);
            } else if (fileData.indexOf(nextElement) < 0) {
                /* UniqueName finnes ikke i feed. Ikke gjør noe i dette tilfellet. */
                //System.out.println(next + " UniqueName EKSISTERER IKKE!");
            }

        }
        /* Listen over nye uniqueNames settes lik listen over verifiserte uniqueNames. */
        global.newUniqueNames = verifiedNewUniqueNames;
    }

    /*
     * Viser suksessivt de nye uniqueNames med komboboks for å velge hvilken kategori de tilhører.
     */
    @Action
    public void uniqueNameDialog() throws SAXException, IOException {
        jDialog3.setIconImage(Toolkit.getDefaultToolkit().getImage("res/gear.gif"));
        jDialog3.setSize(330, 200);
        jDialog3.getRootPane().setDefaultButton(jButton8);
        //int noModifiers = 0;
        //KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, noModifiers, false);
        if (feedIndex == 0) {
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Innenriks", "Utenriks", "Kultur", "Meninger", "Sport", "Sportsresultater", "Fakta", "Si-D", "Kongelige", "Miljo", "Okonomi", "Oslo", "Nett og IT", "Dyr", "Vaer", "Mat og vin", "Helse", "Forbruker", "A-magasinet", "Natur og vitenskap"}));
        } else if (feedIndex == 1) {
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Nyheter: Bors og finans", "Nyheter: Kvartalsresultater", "Nyheter: Makro og politkk", "Nyheter: Naeringsliv", "Nyheter: Utenriks", "Nyheter: Lov og Rett", "Nyheter: Eiendom", "Nyheter: Medier og Reklame", "Nyheter: Eksklusiv livsstil", "Boers: Oslo boers", "Boers: Europeiske boerser", "Boers: Verdens boerser", "Boers: Aksjer", "Boers: Fond", "Boers: Valuta", "Boers: Renter", "Boers: Olje og andre raavarer", "Boers: Indexer", "Boers: Derivater", "Kommentar"}));
        }

        jLabel16.setText(global.nextUniqueName);

        /* Kobler mnemonic i label med tilhørende combobox.*/
        jLabel17.setDisplayedMnemonic('T');
        jLabel17.setLabelFor(jComboBox1);

        jDialog3.setLocationRelativeTo(mainPanel);
        jDialog3.setModal(true);
        jDialog3.setVisible(true);
        /* OK-knappen har en actionPerformed-event knyttet til seg. Ved valg av knappen kalles
         * metoden jButton5ActionPerformed som fullfører koblingen mellom uniqueNames og kategori.*/
    }

    /*
     * Metoden tar kategorinavn og uniqueName-navn som input-parametre. Oppretter en instans
     * av klassen AddUniqueNameItem som legger aktuelt uniqueName til i valgt kategori.
     */
    public void addUniqueNameItem(String c, String u) throws IOException {
        DomParser add = new DomParser();
        String category = c;    // Kategorien som det nye uniqueName skal legges i.
        String uniqueName = u;  // Aktuelt uniqueName som skal legges til.
        add.addNewUniqueName(category, uniqueName);
    }

    /*
     * Metoden leter i den mergede feeden etter en artikkel med aktuelt uniqueName. Viser denne
     * artikkelen i grensesnittet for at det skal være enklere å velge riktig kategori til aktuelt uniqueName.
     */
    public void searchForArticle(String uName) throws TransformerConfigurationException, TransformerException, IOException {
        String uniqueName = uName;  //Aktuelt uniqueName
        /* Lager en currentUN-fil for hvert uniqueName som brukes av xsl-fila som leter opp en eksempelartikkel.
        currentUN inneholder navnet på det aktuelle uniqueName.*/
        String currentUN = global.DATA_DIR + "/xml/currentUniqueName.xml";
        /* Innholdet i currentUN-fila, dvs. navnet på aktuelt uniqueName.*/
        String fileBody = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><uniqueName>" + uniqueName + "</uniqueName>";
        try {
            FileWriter fileWriter = new FileWriter(currentUN);
            fileWriter.write(fileBody);
            fileWriter.close();
        } catch (Exception e) {
        }
        /* Html-fil som skal inneholde en eksempelartikkel for aktuelt uniqueName.*/
        File showArticles = new File(global.DATA_DIR + "/tmp/" + uniqueName + ".htm");

        /* Konverterer filene til URI-format og transformerer vha. showArticle.xsl
        Denne transformeringen lager html-fila som inneholder eksempelartikkelen.*/
        //String xslFileURI = convertToURI(new File(global.DATA_DIR.replace(" ", "%20") + "/xml/showArticle.xsl"));
        String xslFileURI = new File(global.DATA_DIR + "/xml/showArticle.xsl").toURI().toString();
        //String inputFileURI = convertToURI(new File(global.DATA_DIR.replace(" ", "%20") + "/tmp/aviserMergedReady.xml"));
        String inputFileURI = new File(global.DATA_DIR + "/tmp/aviserMergedReady.xml").toURI().toString();
        //String feedURI = convertToURI(showArticles).replace(" ", "%20");
        String feedURI = showArticles.toURI().toString();

        global.insertNewRow(new Object[]{"", " Viser eksempelartikkel for nytt uniqueName ", uniqueName});
        transformXSLT20(xslFileURI, inputFileURI, feedURI);
        jEditorPane1.setPage(feedURI);
    }

    /*
     * Tilslutt transformeres den mergede feeden. Resultatet blir input i den videre
     * Daisy-produksjonen.
     */
    @Action
    public Task transformerAviser() {
        return new transformerAviserTask(getApplication());
    }

    private class transformerAviserTask extends org.jdesktop.application.Task<Object, Void> {

        transformerAviserTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() throws SAXException, IOException, TransformerConfigurationException, TransformerException {
            File file = new File(global.DATA_DIR + "/tmp/aviserMergedReady.xml");
            if (file.exists()) {
                /* Programmet transformerer nå bare til DTBook, ikke til HTML.*/
                transformAviserDTBook();
                //transformAviserHTML();
                validateAvisFeed(); // Kaller metoden som validerer DTBook-fila og retter evt. feil.
            } else {
                global.insertNewRow(new Object[]{"", " Finner ingen filer. Last ned avisfeedene først.", ""});
            }

            deleteFilesOnExit();    //Sletter filene i tmp-mappa når programmet avsluttes
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            global.insertNewRow(new Object[]{"", " Fullført!", ""});
        }
    }

    /*
     * NB! Programmet transformerer ikke lenger til HTML, bare DTBook. Metoden er ikke vedlikeholdt.
     * XSL-transformering av avisene til HTML. Transformerer to ganger, både til tmp- og output-mappene.
     * Fila i tmp brukes til å vise fila i Transformer-grensesnittet (uten stilark). Fila i output skal
     * brukes videre i produksjonen.
     */
    public void transformAviserHTML() throws TransformerConfigurationException, TransformerException, IOException {
        String xslFileURI = convertToURI(new File("xml/processAftenposten.xsl"));
        String inputFileURI = convertToURI(new File(global.DATA_DIR + "/tmp/aviserMergedReady.xml"));
        /* String feedURI = convertToURI(new File("output/AftenpostenFeed.html"));*/
        String feedURItmp = convertToURI(new File("tmp/AftenpostenFeed.html"));
        /* jTextArea1.append("Transformerer Aftenposten til HTML...");
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
        transformXSLT20(xslFileURI, inputFileURI, feedURI);*/
        transformXSLT20(xslFileURI, inputFileURI, feedURItmp);
        //jEditorPane1.setPage(feedURItmp);
        /* jTextArea1.append("Fullført\n");
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());*/
    }

    /*
     * XSL-transformering av avisene til DTBook.
     */
    public void transformAviserDTBook() throws TransformerConfigurationException, TransformerException, IOException {
        File file = new File(global.DATA_DIR + "/tmp/AftenpostenFeed.xml");
        String filePath = file.getAbsolutePath();
        global.insertNewRow(new Object[]{"", " Transformerer til DTBook", filePath});
        //String xslFileURI = convertToURI(new File(global.DATA_DIR.replace(" ", "%20") + "/xml/processAftenpostenDTBook.xsl"));
        String xslFileURI = new File(global.DATA_DIR + "/xml/processAftenpostenDTBook.xsl").toURI().toString();
        //String inputFileURI = convertToURI(new File(global.DATA_DIR.replace(" ", "%20") + "/tmp/aviserMergedReady.xml"));
        String inputFileURI = new File(global.DATA_DIR + "/tmp/aviserMergedReady.xml").toURI().toString();
        String feedURI = file.toURI().toString();

        transformXSLT20(xslFileURI, inputFileURI, feedURI);
        jEditorPane1.setPage(feedURI);
    }

    /*
     * Validerer avisFeeden (DTBook) etter transformeringen. Artikler som
     * ikke validerer, blir fjernet.
     */
    @Action
    public Task validerAvisFeed() {
        return new validerAvisFeedTask(getApplication());
    }

    private class validerAvisFeedTask extends org.jdesktop.application.Task<Object, Void> {

        validerAvisFeedTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() throws SAXException, IOException, TransformerConfigurationException, TransformerException {
            validateAvisFeed();

            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            global.insertNewRow(new Object[]{"  OK!", " Fullført validering. Klar for produksjon i DAISY Pipeline", ""});
        }
    }

    @Action
    public void validateAvisFeed() throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        String fileName = global.DATA_DIR + "/tmp/AftenpostenFeed.xml"; //Fil som skal valideres
        File file = new File(fileName);
        String filePath = file.getAbsolutePath();

        DOMValidator v = new DOMValidator();
        v.validate(fileName);

        jEditorPane1.setPage(convertToURI(file));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jEditorPane4 = new javax.swing.JEditorPane();
        jPanel8 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jTextField7 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jButton13 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jEditorPane5 = new javax.swing.JEditorPane();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jEditorPane6 = new javax.swing.JEditorPane();
        jButton10 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jLabel2 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jDialog3 = new javax.swing.JDialog();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jDialog4 = new javax.swing.JDialog();
        jLabel13 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jEditorPane2 = new javax.swing.JEditorPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jEditorPane3 = new javax.swing.JEditorPane();
        jLabel14 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();

        mainPanel.setMaximumSize(new java.awt.Dimension(1000, 500));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(1017, 687));

        jTabbedPane1.setMaximumSize(new java.awt.Dimension(328, 438));
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(328, 438));

        jPanel1.setMaximumSize(new java.awt.Dimension(463, 300));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(463, 300));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(transformer2.Transformer2App.class).getContext().getResourceMap(Transformer2View.class);
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel7.border.title"))); // NOI18N
        jPanel7.setName("jPanel7"); // NOI18N

        jRadioButton1.setMnemonic('E');
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(resourceMap.getString("jRadioButton1.text")); // NOI18N
        jRadioButton1.setToolTipText(resourceMap.getString("jRadioButton1.toolTipText")); // NOI18N
        jRadioButton1.setName("jRadioButton1"); // NOI18N
        jRadioButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton1ItemStateChanged(evt);
            }
        });

        jRadioButton2.setMnemonic('A');
        jRadioButton2.setText(resourceMap.getString("jRadioButton2.text")); // NOI18N
        jRadioButton2.setName("jRadioButton2"); // NOI18N
        jRadioButton2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton2ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addContainerGap(168, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(transformer2.Transformer2App.class).getContext().getActionMap(Transformer2View.class, this);
        jButton1.setAction(actionMap.get("lastNedAftenposten")); // NOI18N
        jButton1.setFont(resourceMap.getFont("jButton1.font")); // NOI18N
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setMnemonic('1');
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setContentAreaFilled(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setRolloverIcon(resourceMap.getIcon("jButton1.rolloverIcon")); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jButton2.setAction(actionMap.get("UniqueNames")); // NOI18N
        jButton2.setFont(resourceMap.getFont("jButton2.font")); // NOI18N
        jButton2.setIcon(resourceMap.getIcon("jButton2.icon")); // NOI18N
        jButton2.setMnemonic('2');
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
        jButton2.setContentAreaFilled(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setRolloverIcon(resourceMap.getIcon("jButton2.rolloverIcon")); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jButton3.setAction(actionMap.get("transformerAviser")); // NOI18N
        jButton3.setFont(resourceMap.getFont("jButton3.font")); // NOI18N
        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setMnemonic('3');
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setToolTipText(resourceMap.getString("jButton3.toolTipText")); // NOI18N
        jButton3.setContentAreaFilled(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setRolloverIcon(resourceMap.getIcon("jButton3.rolloverIcon")); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                        .addGap(0, 0, 0))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addContainerGap(12, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addGap(64, 64, 64)
                .addComponent(jLabel11)
                .addGap(321, 321, 321))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jButton5.setAction(actionMap.get("lagreAvisInnst")); // NOI18N
        jButton5.setMnemonic('L');
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setToolTipText(resourceMap.getString("jButton5.toolTipText")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel6.border.title"))); // NOI18N
        jPanel6.setName("jPanel6"); // NOI18N

        jLabel9.setDisplayedMnemonic('B');
        jLabel9.setLabelFor(jEditorPane4);
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setToolTipText(resourceMap.getString("jLabel9.toolTipText")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jScrollPane5.setHorizontalScrollBar(null);
        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jEditorPane4.setMinimumSize(new java.awt.Dimension(80, 20));
        jEditorPane4.setName("jEditorPane4"); // NOI18N
        jEditorPane4.setPreferredSize(new java.awt.Dimension(80, 20));
        jScrollPane5.setViewportView(jEditorPane4);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel9)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel8.border.title"))); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        jButton7.setAction(actionMap.get("visUniqueNames")); // NOI18N
        jButton7.setMnemonic('V');
        jButton7.setText(resourceMap.getString("jButton7.text")); // NOI18N
        jButton7.setToolTipText(resourceMap.getString("jButton7.toolTipText")); // NOI18N
        jButton7.setName("jButton7"); // NOI18N

        jButton4.setAction(actionMap.get("getFile")); // NOI18N
        jButton4.setMnemonic('E');
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setToolTipText(resourceMap.getString("jButton4.toolTipText")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N

        jTextField7.setText(resourceMap.getString("jTextField7.text")); // NOI18N
        jTextField7.setName("jTextField7"); // NOI18N

        jLabel12.setDisplayedMnemonic('P');
        jLabel12.setLabelFor(jTextField7);
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setToolTipText(resourceMap.getString("jLabel12.toolTipText")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                        .addComponent(jButton7))
                    .addComponent(jLabel12)
                    .addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel8Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton4, jButton7});

        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addGap(4, 4, 4)
                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(jButton5)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jButton13.setAction(actionMap.get("jakarta")); // NOI18N
        jButton13.setFont(resourceMap.getFont("jButton13.font")); // NOI18N
        jButton13.setIcon(resourceMap.getIcon("jButton13.icon")); // NOI18N
        jButton13.setMnemonic('P');
        jButton13.setText(resourceMap.getString("jButton13.text")); // NOI18N
        jButton13.setToolTipText(resourceMap.getString("jButton13.toolTipText")); // NOI18N
        jButton13.setContentAreaFilled(false);
        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.setName("jButton13"); // NOI18N
        jButton13.setRolloverIcon(resourceMap.getIcon("jButton13.rolloverIcon")); // NOI18N
        jButton13.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel10.border.title"))); // NOI18N
        jPanel10.setName("jPanel10"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setDisplayedMnemonic('S');
        jLabel7.setLabelFor(jTextField1);
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(88, 88, 88))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addContainerGap())))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel19)
                    .addComponent(jButton13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(jButton13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
                .addComponent(jLabel19)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel11.border.title"))); // NOI18N
        jPanel11.setName("jPanel11"); // NOI18N

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        jLabel22.setDisplayedMnemonic('S');
        jLabel22.setLabelFor(jTextField2);
        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
        jTextField2.setName("jTextField2"); // NOI18N

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel23)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        jButton6.setAction(actionMap.get("jakarta")); // NOI18N
        jButton6.setFont(resourceMap.getFont("jButton6.font")); // NOI18N
        jButton6.setIcon(resourceMap.getIcon("jButton6.icon")); // NOI18N
        jButton6.setMnemonic('P');
        jButton6.setText(resourceMap.getString("jButton6.text")); // NOI18N
        jButton6.setToolTipText(resourceMap.getString("jButton6.toolTipText")); // NOI18N
        jButton6.setContentAreaFilled(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setName("jButton6"); // NOI18N
        jButton6.setRolloverIcon(resourceMap.getIcon("jButton6.rolloverIcon")); // NOI18N
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel24)
                    .addComponent(jButton6, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20)
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                .addComponent(jLabel24)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel9.border.title"))); // NOI18N
        jPanel9.setName("jPanel9"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        jEditorPane5.setName("jEditorPane5"); // NOI18N
        jScrollPane6.setViewportView(jEditorPane5);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane7.setName("jScrollPane7"); // NOI18N

        jEditorPane6.setName("jEditorPane6"); // NOI18N
        jScrollPane7.setViewportView(jEditorPane6);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addContainerGap())
            .addComponent(jScrollPane6)
            .addComponent(jScrollPane7)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addContainerGap(224, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton10.setText(resourceMap.getString("jButton10.text")); // NOI18N
        jButton10.setName("jButton10"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton10))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton10)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jScrollPane1.setMaximumSize(new java.awt.Dimension(997, 247));
        jScrollPane1.setName("jScrollPane1"); // NOI18N
        jScrollPane1.setPreferredSize(new java.awt.Dimension(997, 247));

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setMaximumSize(new java.awt.Dimension(659, 383));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jEditorPane1.setMaximumSize(new java.awt.Dimension(657, 381));
        jEditorPane1.setName("jEditorPane1"); // NOI18N
        jEditorPane1.setPreferredSize(new java.awt.Dimension(657, 381));
        jScrollPane2.setViewportView(jEditorPane1);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(1115, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 659, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))))
                .addGap(153, 153, 153))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 408, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setMnemonic('F');
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setMnemonic('A');
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setMnemonic('J');
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem2.setAction(actionMap.get("velgAftenposten")); // NOI18N
        jMenuItem2.setIcon(resourceMap.getIcon("jMenuItem2.icon")); // NOI18N
        jMenuItem2.setMnemonic('A');
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setToolTipText(resourceMap.getString("jMenuItem2.toolTipText")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenu1.add(jMenuItem2);

        jMenuItem3.setAction(actionMap.get("velgTVGuiden")); // NOI18N
        jMenuItem3.setIcon(resourceMap.getIcon("jMenuItem3.icon")); // NOI18N
        jMenuItem3.setMnemonic('T');
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setToolTipText(resourceMap.getString("jMenuItem3.toolTipText")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenu1.add(jMenuItem3);

        jMenuItem4.setAction(actionMap.get("velgRadioGuiden")); // NOI18N
        jMenuItem4.setIcon(resourceMap.getIcon("jMenuItem4.icon")); // NOI18N
        jMenuItem4.setMnemonic('R');
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setToolTipText(resourceMap.getString("jMenuItem4.toolTipText")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenu1.add(jMenuItem4);

        menuBar.add(jMenu1);

        helpMenu.setMnemonic('H');
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        jMenuItem5.setAction(actionMap.get("visLesMeg")); // NOI18N
        jMenuItem5.setIcon(resourceMap.getIcon("jMenuItem5.icon")); // NOI18N
        jMenuItem5.setMnemonic('L');
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setToolTipText(resourceMap.getString("jMenuItem5.toolTipText")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        helpMenu.add(jMenuItem5);

        jMenuItem1.setAction(actionMap.get("visFilBaner")); // NOI18N
        jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
        jMenuItem1.setMnemonic('V');
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setToolTipText(resourceMap.getString("jMenuItem1.toolTipText")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        helpMenu.add(jMenuItem1);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setToolTipText(resourceMap.getString("aboutMenuItem.toolTipText")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        helpMenu.add(jSeparator2);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new java.awt.Dimension(1000, 30));

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 990, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1160, Short.MAX_VALUE)
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        buttonGroup1.add(jRadioButton1);
        buttonGroup1.add(jRadioButton2);

        jDialog3.setTitle(resourceMap.getString("jDialog3.title")); // NOI18N
        jDialog3.setName("jDialog3"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jLabel16.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jLabel17.setDisplayedMnemonic('T');
        jLabel17.setLabelFor(jComboBox1);
        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        jComboBox1.setName("jComboBox1"); // NOI18N

        jButton8.setMnemonic('L');
        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setMaximumSize(new java.awt.Dimension(65, 23));
        jButton8.setMinimumSize(new java.awt.Dimension(65, 23));
        jButton8.setName("jButton8"); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setAction(actionMap.get("avbrytDialog")); // NOI18N
        jButton9.setMnemonic('A');
        jButton9.setText(resourceMap.getString("jButton9.text")); // NOI18N
        jButton9.setToolTipText(resourceMap.getString("jButton9.toolTipText")); // NOI18N
        jButton9.setName("jButton9"); // NOI18N

        javax.swing.GroupLayout jDialog3Layout = new javax.swing.GroupLayout(jDialog3.getContentPane());
        jDialog3.getContentPane().setLayout(jDialog3Layout);
        jDialog3Layout.setHorizontalGroup(
            jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialog3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel16))
                    .addComponent(jLabel15)
                    .addGroup(jDialog3Layout.createSequentialGroup()
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(175, 175, 175)
                        .addComponent(jButton9))
                    .addGroup(jDialog3Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(330, Short.MAX_VALUE))
        );
        jDialog3Layout.setVerticalGroup(
            jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addGap(18, 18, 18)
                .addGroup(jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9))
                .addContainerGap(173, Short.MAX_VALUE))
        );

        jDialog4.setTitle(resourceMap.getString("jDialog4.title")); // NOI18N
        jDialog4.setName("jDialog4"); // NOI18N

        jLabel13.setDisplayedMnemonic('T');
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jButton11.setAction(actionMap.get("lagreAvisInnst")); // NOI18N
        jButton11.setText(resourceMap.getString("jButton11.text")); // NOI18N
        jButton11.setName("jButton11"); // NOI18N

        jButton12.setAction(actionMap.get("avbrytKnapp")); // NOI18N
        jButton12.setText(resourceMap.getString("jButton12.text")); // NOI18N
        jButton12.setName("jButton12"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jEditorPane2.setName("jEditorPane2"); // NOI18N
        jScrollPane4.setViewportView(jEditorPane2);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jEditorPane3.setName("jEditorPane3"); // NOI18N
        jScrollPane3.setViewportView(jEditorPane3);

        jLabel14.setDisplayedMnemonic('O');
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        javax.swing.GroupLayout jDialog4Layout = new javax.swing.GroupLayout(jDialog4.getContentPane());
        jDialog4.getContentPane().setLayout(jDialog4Layout);
        jDialog4Layout.setHorizontalGroup(
            jDialog4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialog4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addGroup(jDialog4Layout.createSequentialGroup()
                        .addComponent(jButton11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 286, Short.MAX_VALUE)
                        .addComponent(jButton12))
                    .addComponent(jLabel18))
                .addContainerGap())
        );
        jDialog4Layout.setVerticalGroup(
            jDialog4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addGap(23, 23, 23)
                .addGroup(jDialog4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jButton12))
                .addGap(36, 36, 36))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        try {
            /* Ved valg av knappen Lagre i uniqueName-dialogen. Tar det valgte elementet i comboboksen i
            uniqueName-dialogen og legger det inn i strengen selectedCategory.*/
            Object selected = jComboBox1.getSelectedItem();
            String selectedCategory = selected.toString();
            global.insertNewRow(new Object[]{"", " Legger til nytt uniqueName ", global.nextUniqueName + " [" + selectedCategory + "]"});
            addUniqueNameItem(selectedCategory, global.nextUniqueName);

            /* Sjekker om flere nye uniqueNames og gjentar prosessen ved å oppdatere uniqueName i dialogen.*/
            if (global.token.hasMoreTokens()) {
                global.nextUniqueName = global.token.nextToken();
                /* Kalle searchForArticle som leter etter en eksempelartikkel for aktuelt uniqueName.*/
                try {
                    searchForArticle(global.nextUniqueName);
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(Transformer2View.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(Transformer2View.class.getName()).log(Level.SEVERE, null, ex);
                }
                uniqueNameDialog(); //Dialogen åpnes for hvert nytt uniqueName som skal legges til.

            } else {
                /*  Hvis det ikke er flere nye uniqueNames, lukkes dialogen.*/
                jDialog3.setVisible(false);
            }

        } catch (SAXException ex) {
            Logger.getLogger(Transformer2View.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Transformer2View.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_jButton8ActionPerformed

    private void jRadioButton2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton2ItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            aviser[0] = "Aftenposten";
            aviser[1] = "";
        }
}//GEN-LAST:event_jRadioButton2ItemStateChanged

    private void jRadioButton1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton1ItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            aviser[0] = "Aftenposten";
            aviser[1] = "E24";
        }
}//GEN-LAST:event_jRadioButton1ItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JDialog jDialog3;
    private javax.swing.JDialog jDialog4;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JEditorPane jEditorPane2;
    private javax.swing.JEditorPane jEditorPane3;
    private javax.swing.JEditorPane jEditorPane4;
    private javax.swing.JEditorPane jEditorPane5;
    private javax.swing.JEditorPane jEditorPane6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    public static javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
