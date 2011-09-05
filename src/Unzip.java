import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip {
	final static int BUFFER = 2048;

	/**
	 * Unzip a zip archive file into a directory by the same
	 * name as the original zip file (minus the extension);
	 * @param f - File to unarchive
	 * @return Array of file names that were unpacked
	 */
	public static ArrayList<String> unzip(File f) throws IOException
	{
		ArrayList<String> unpackedFiles = new ArrayList<String>();
		String subdir = f.getName();
		int dotOffset = subdir.lastIndexOf('.');
		if (dotOffset != -1)
		{
			subdir = subdir.substring(0, dotOffset);
		}
		File subdirFile = new File(f.getParent() + File.separator + subdir);
		if (!subdirFile.isDirectory() && !subdirFile.mkdir())
		{
			throw new IOException("Could not create directory: " + subdirFile.getAbsolutePath());
		}
		/*
		 * From:
		 * http://java.sun.com/developer/technicalArticles/Programming/compression/
		 */
		BufferedOutputStream dest = null;
		FileInputStream fis = new 
				FileInputStream(f);
		ZipInputStream zis = new 
				ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (entry.isDirectory())
			{
				String newDirName = subdir + File.separator + entry.getName();
				File newDir = new File(f.getParent() + File.separator + newDirName);
				if (!newDir.exists())
				{
					newDir.mkdirs();
				}
			}
			else
			{
				//System.out.println("Extracting: " +entry);
				int count;
				byte data[] = new byte[BUFFER];
				// write the files to the disk
				String newFileName = subdir + File.separator + entry.getName();
				if (!entry.getName().startsWith("."))
				{
					unpackedFiles.add(newFileName);
				}
				File newFile = new File(f.getParent() + File.separator + newFileName);
				newFile.getParentFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				dest = new 
						BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
		}
		zis.close();
		return unpackedFiles;
	}
}
