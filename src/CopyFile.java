import java.io.*;

/**
 * Utility class to copy a file.
 * @author ghelmer
 *
 */
public class CopyFile {
	public static final int BUFFER_SIZE = 4096;

	public static void copy(File srFile, File dtFile) throws IOException
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(srFile));
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dtFile));

		byte[] buf = new byte[BUFFER_SIZE];
		int len;
		while ((len = in.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		long modTime = srFile.lastModified();
		if (modTime != 0L)
		{
			dtFile.setLastModified(modTime);
		}
	}
}
