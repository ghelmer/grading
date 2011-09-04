import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

public class GradingHelper {
	String rootDirectory;
	String hwName;
	ProgramInfo[] programs;
	
	public GradingHelper(String s)
	{
		rootDirectory = s;
		programs = null;
	}
	
	/**
	 * Examine each subdirectory of the given directory, and
	 * find all the Java files in the directory.  Compile the
	 * files and report whether the compilation succeeded.
	 */
	public ArrayList<AssignmentResults> processDirectory() throws IOException, InterruptedException
	{
		ArrayList<AssignmentResults> results = new ArrayList<AssignmentResults>();
		File rootDir = new File(rootDirectory);
		if (!rootDir.isDirectory())
		{
			throw new IllegalArgumentException(rootDirectory + " is not a directory");
		}
		for (File e : rootDir.listFiles())
		{
			if (e.isDirectory())
			{
				AssignmentResults ar = new AssignmentResults(e.getName());
				results.add(ar);
				ar.findJavaFiles(e);
				if (ar.checkRequiredJavaFiles(programs))
				{
					if (ar.compileJavaFiles(e))
					{
						ar.runJavaPrograms(programs, e);
					}
				}					
			}
		}
		return results;
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
	 * @param args
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
			ArrayList<AssignmentResults> results = gh.processDirectory();
			for (AssignmentResults ar : results)
			{
				System.out.println(ar.toString());
			}
		}
		catch (IOException e)
		{
			System.err.println("Warning: Exception " + e.getMessage() + " while reading grading.conf");
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
