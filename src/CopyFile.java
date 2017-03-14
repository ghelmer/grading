import java.io.*;

/**
 * Utility class to copy a file.
 * @author ghelmer
 *
 */
public class CopyFile {
	private File srcFile;
	private String destBase;
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
	
	/**
	 * Save the source path and destination base to use when distributing
	 * source files into test program directories.
	 * @param _srcFile - Source file (full pathname)
	 * @param _destBase - Destination basename (partial path)
	 */
	public CopyFile(File _srcFile, String _destBase)
	{
		srcFile = _srcFile;
		destBase = _destBase;
	}

	/**
	 * Copy this object's source file to the given destination path.
	 * @param destDir - Directory to contain copied file
	 * @throws IOException
	 */
	public void doCopy(String destDir) throws IOException
	{
		File destFile = new File(destDir + File.separatorChar + destBase);
		copy(srcFile, destFile);
	}
}
