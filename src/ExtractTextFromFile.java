import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ExtractTextFromFile {
	public static String getText(File inputFile, File outputFile) throws IOException {
		if (!inputFile.exists() || !inputFile.isFile()) {
			throw new IllegalArgumentException(inputFile.toString() + " is not a valid file");
		}
		
		// Use LibreOffice + unoconv to convert the document
		// /Applications/LibreOffice.app/Contents/MacOS/python ~/Downloads/unoconv-0.6/unoconv --format=text --output=testing.txt ~/Documents/testing.odt
		String args[] = new String[5];
		args[0] = "/Applications/LibreOffice.app/Contents/MacOS/python";
		args[1] = "/Users/ghelmer/Downloads/unoconv-0.6/unoconv";
		args[2] = "--format=text";
		args[3] = "--output=" + outputFile.getAbsolutePath();
		args[4] = inputFile.getAbsolutePath();
		Runtime r = Runtime.getRuntime();
		Process result = r.exec(args);
		Scanner in = new Scanner(result.getErrorStream());
		StringBuffer output = new StringBuffer();
		while (in.hasNextLine())
		{
			output.append(in.nextLine() + "\n");
		}
		in.close();
		try {
			result.waitFor();
			if (output.length() != 0)
			{
				System.out.println("Output form unoconv " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath() + ":");
				System.out.println(output);
			}
			else if (result.exitValue() != 0)
			{
				System.out.println("unoconv exit code: " + result.exitValue());
			}
		} catch (InterruptedException e) {
			System.err.println("unoconv Interrupted Exception: " + e.getMessage());
		}
		return "The file " + inputFile.getName() + " contains:\n";
	}
}

