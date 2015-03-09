import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class ExtractTextFromFile {
	public static String getText(File inputFile, File outputFile) throws IOException, SAXException, TikaException {
		if (!inputFile.exists() || !inputFile.isFile()) {
			throw new IllegalArgumentException(inputFile.toString() + " is not a valid file");
		}
		FileInputStream stream = new FileInputStream(inputFile);
		String output;
		
		BodyContentHandler handler = new BodyContentHandler();
	    AutoDetectParser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
	    try {
	        parser.parse(stream, handler, metadata);
	        output = handler.toString();
	    } finally {
	        stream.close();
	    }
	    PrintWriter pw = new PrintWriter(outputFile);
	    try
	    {
	    	pw.print(output);
	    }
	    finally
	    {
	    	pw.close();
	    }
		return "The file " + inputFile.getName() + " contains:\n";
	}
}

