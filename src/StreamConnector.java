import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Copy an InputStream to an OutputStream using a thread.
 * Inspired by code from mysql.
 * 
 * @author ghelmer
 *
 */
public class StreamConnector extends Thread {
    private static int count = 0;

    private InputStream from;
    private OutputStream to;
    private boolean closeFrom;
    private boolean closeTo;

    /**
     * Construct a new StreamConnector to with the specified
     * InputStream and OutputStream.
     * @param from - InputStream
     * @param to - OutputStream
     * @param name - identifying information about the stream
     */
    public StreamConnector(InputStream from, OutputStream to, String name) {
        super("StreamConnector " + count() + ": " + name);
        this.from = from;
        this.to = to;
        closeFrom = false;
        closeTo = false;
    }
    /**
     * Construct a new StreamConnector to with the specified
     * InputStream and OutputStream.
     * @param from - InputStream
     * @param to - OutputStream
     * @param closeFrom - close the input stream when end-of-file encountered
     * @param closeTo - close the output stream when end-of-file encountered on input
     * @param name - identifying information about the stream
     */
    public StreamConnector(InputStream from, OutputStream to,
    		boolean closeFrom, boolean closeTo, String name) {
        super("StreamConnector " + count() + ": " + name);
        this.from = from;
        this.to = to;
        this.closeFrom = closeFrom;
        this.closeTo = closeTo;
    }
    /**
     * Run this thread under the VoidBlock to catch any execptions
     * that occur. 
     */
    public void run() {
        new Exceptions.VoidBlock() {
            public void inner() throws Exception {
                Streams.copy(from, to, false, true, closeFrom, closeTo);
            }
        }.exec();
    }

    /**
     * Assign a monotonic number to each instance of the StreamConnector.
     */
    private static synchronized int count() {
        return count++;
    }

    /**
     * Copy from an InputStream to an OutputStream.
     * @author ghelmer
     *
     */
    public static final class Streams {
    	public static final String RESOURCE_SEPARATOR = "/";
    	private static final int END_OF_STREAM = -1;

    	public static void copy(InputStream from, OutputStream to) throws IOException {
    		copy(from, to, true, false);
    	}
    	public static void copy(InputStream from, OutputStream to, boolean buffer,
    			boolean terminateOnFailure) throws IOException {
    		copy(from, to, buffer, terminateOnFailure, false, false);
    	}
    	static void copy(InputStream from, OutputStream to, boolean buffer,
    			boolean terminateOnFailure, boolean closeFrom,
    			boolean closeTo) throws IOException {
    		if (buffer) {
    			from = new BufferedInputStream(from);
    			to = new BufferedOutputStream(to);
    		}
    		while (true) {
    			int i;
    			try {
    				i = from.read();
    				if (i == END_OF_STREAM) {
    					break;
    				}
    				to.write(i);
    			} catch (Exception e) {
    				if (terminateOnFailure) {
    					break;
    				}
    				if (e instanceof IOException) {
    					throw (IOException) e;
    				}
    				throw Exceptions.toRuntime(e);
    			}
    		}
    		to.flush();
    		if (closeFrom) {
    			from.close();
    		}
    		if (closeTo) {
    			to.close();
    		}
    	}
    }
 }