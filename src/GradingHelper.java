import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	HashMap<String,String> usersToFullName;
	DateFormat df;
	Date dueDate;
	
	public GradingHelper(String s) throws ParseException
	{
		rootDirectory = s;
		programs = null;
		reportFile = null;
		results = new ArrayList<AssignmentResults>();
		usersToFullName = new HashMap<String,String>();
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		dueDate = df.parse("2100-01-01 00:00");
	}
	
	/**
	 * Load the list of usernames and full names from the Students.txt
	 * file in the directory above the given subdirectory.
	 */
	public void loadStudentsNames() throws IOException
	{
		File studentsFile = new File(rootDirectory + "/../Students.txt");
		if (!studentsFile.exists())
		{
			System.out.println("Warning: Students file " + studentsFile.toString() + " does not exist");
			return;
		}
		/* username: Last, First */
		Pattern linePattern = Pattern.compile("^(\\S+)\t(.*)$");
		int lineNumber = 0;
		Scanner in = new Scanner(studentsFile);
		while (in.hasNextLine())
		{
			String line = in.nextLine();
			lineNumber++;
			if (line.length() == 0)
				continue;
			Matcher m = linePattern.matcher(line);
			if (m.matches())
			{
				String user = m.group(1);
				String name = m.group(2);
				usersToFullName.put(user, name.trim());
			}
			else
			{
				System.out.println(studentsFile.toString() + ": Line " + lineNumber + ": Did not match 'username<TAB>Last, First' pattern");
			}
		}
		in.close();
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
				AssignmentResults ar = new AssignmentResults(e.getName(), usersToFullName.get(e.getName()), e);
				results.add(ar);
				ar.findFiles(e);
				ar.findSubmissionDate(dueDate);
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
	 * @throws ParseException 
	 * @throws DOMException 
	 */
	public void readConfiguration() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, DOMException, ParseException
	{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    Document doc = builder.parse(rootDirectory + File.separator + "grading.xml");

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		Node n = (Node)xpath.evaluate("/homework/name", doc, XPathConstants.NODE);
		hwName = n.getTextContent();
		
		n = (Node)xpath.evaluate("/homework/dueDate", doc, XPathConstants.NODE);
		if (n != null)
		{
			dueDate = df.parse(n.getTextContent());
		}

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
		reportFileWriter.close();
		reportFileWriter = null;
		File renameReportFile = new File(rootDirectory + File.separator + "GradingReport.txt");
		reportFile.renameTo(renameReportFile);
		reportFile = renameReportFile;
		System.out.println("Submission results are in " + reportFile.getAbsolutePath());
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
		try
		{
			GradingHelper gh = new GradingHelper(args[0]);
			gh.loadStudentsNames();
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
		catch (ParseException e)
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
