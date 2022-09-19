package transformer2;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class UrlRetrieve {

    /* Denne metoden laster ned en fil fra en URL og lagrer den lokalt.*/
    public void saveFile(URL u, String f) throws Exception {
        URL url = u;    // URL til aktuell avis
        String fileName = f;
        InputStream urlstream = url.openStream();
        byte[] buffer = new byte[0];
        byte[] chunk = new byte[4096];
        int count;

        while ((count = urlstream.read(chunk)) >= 0) {
            byte[] t = new byte[buffer.length + count];
            System.arraycopy(buffer, 0, t, 0, buffer.length);
            System.arraycopy(chunk, 0, t, buffer.length, count);
            buffer = t;
        }

        FileOutputStream out = null;
        out = new FileOutputStream(fileName);
        out.write(buffer);
        out.close();
    }
}
