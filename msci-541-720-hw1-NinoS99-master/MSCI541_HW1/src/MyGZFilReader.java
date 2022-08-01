import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class MyGZFilReader extends Reader {

    private GZIPInputStream gzipInputStream = null;
    char[] buf = new char[1024];

    @Override
    public void close() throws IOException {
        gzipInputStream.close();
    }

    public MyGZFilReader(String filename)
               throws FileNotFoundException, IOException {
        gzipInputStream = new GZIPInputStream(new FileInputStream(filename));
    }

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}

