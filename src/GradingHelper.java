import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

public class GradingHelper {
	String rootDirectory;
	String hwName;
	ProgramInfo[] programs;
	File reportFile;
	PrintWriter reportFileWriter;
	ArrayList<AssignmentResults> results; 
	
	public GradingHelper(String s)
	{
		rootDirectory = s;
		programs = null;
		reportFile = null;
		results = new ArrayList<AssignmentResults>();
	}
	
	/**
	 * Examine each subdirectory of the given directory, and
	 * find all the Java files in the directory.  Compile the
	 * files and report whether the compilation succeeded.
	 */
	public void processDirectory() throws IOException, InterruptedException
	{
		/* Preprocessing: Organize files from BlackBoard assignment download. */
		File rootDir = new File(rootDirectory);
		if (!rootDir.isDirectory())
		{
			throw new IllegalArgumentException(rootDirectory + " is not a directory");
		}
		AssignmentResults.organizeBlackBoardFiles(rootDir);
		
		for (File e : rootDir.listFiles())
		{
			if (e.isDirectory())
			{
				AssignmentResults ar = new AssignmentResults(e.getName(), e);
				results.add(ar);
				ar.findFiles(e);
				ar.findSubmissionDate();
				ar.copyJavaFilesToUser();
				ar.showRequestedJavaFiles(programs);
				ar.stripPackageFromJavaFiles();
				if (ar.checkRequiredJavaFiles(programs))
				{
					if (ar.compileJavaFiles(e))
					{
						ar.runJavaPrograms(programs, e);
					}
				}
			}
		}
		Collections.sort(results);
	}
	
	/**
	 * Read properties from the configuration file.
	 * Properties:
	 *   grading.programs: list of programs expected
	 *   grading.javaFiles: comma-separated list of input files for each program
	 * @param dir - Directory expected to contain the config file
	 * @throws IOException
	 */
	public void readConfiguration() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException
	{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    Document doc = builder.parse(rootDirectory + File.separator + "grading.xml");

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		Node n = (Node)xpath.evaluate("/homework/name", doc, XPathConstants.NODE);
		hwName = n.getTextContent();

		NodeList nl = (NodeList)xpath.evaluate("/homework/program", doc, XPathConstants.NODESET);
		programs = new ProgramInfo[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++)
		{
			programs[i] = new ProgramInfo(nl.item(i));
		}
	}
	
	/**
	 * Create a file in the given root directory to hold the
	 * report for the student submissions.
	 * @throws IOException if any I/O problems occur
	 */
	public void openReportFile() throws IOException
	{
		File rootDir = new File(rootDirectory);
		if (!rootDir.isDirectory())
		{
			throw new IOException(rootDirectory + " is not a directory");
		}
		reportFile = File.createTempFile("GradingReport", ".txt", rootDir);
		reportFileWriter = new PrintWriter(reportFile);
	}

	/**
	 * Close the student submission report file and
	 * display the name of the report file.
	 * @throws IOException
	 */
	public void closeReportFile() throws IOException
	{
		System.out.println("Submission results are in " + reportFile.getName());
		reportFileWriter.close();
		reportFileWriter = null;
	}
	
	/**
	 * Generate the report of the student's submissions.
	 */
	public void reportResults()
	{
		for (AssignmentResults ar : results)
		{
			reportFileWriter.println(ar.toString());
			reportFileWriter.print('\f');
		}
		reportFileWriter.flush();
	}
	
	/**
	 * @param args - Directory to analyze for student submissions
	 */
	public static void main(String[] args)
	{
		if (args.length == 0) {
			System.err.println("Usage: GradingHelper directory-name");
			System.exit(1);
		}
		GradingHelper gh = new GradingHelper(args[0]);
		try
		{
			gh.readConfiguration();
			gh.openReportFile();
			gh.processDirectory();
			gh.reportResults();
			gh.closeReportFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			System.err.println(e.getMessage());
		}
		catch (XPathExpressionException e)
		{
			System.err.println(e.getMessage());
		}
		catch (SAXException e)
		{
			System.err.println(e.getMessage());
		}
		catch (InterruptedException e)
		{
			System.err.println(e.getMessage());
		}
	}

}
