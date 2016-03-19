import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import javax.xml.xpath.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * Manage information about how to run a program.
 * 
 */
public class RunConfiguration {
	public static final int FILE_SUBMITTED = 1;
	private String name;
	private String[] arguments;
	private String[] properties; // Optional properties to set when running the program. Can be null.
	private int inputFileFlags;
	private String inputFile;
	private String outputFile;
	
	public RunConfiguration()
	{
		arguments = new String[0];
		properties = null;
	}
	
	/**
	 * Create a new run configuration for a program.
	 * @param e - root XML node for this run configuration
	 * @throws XPathExpressionException
	 */
	public RunConfiguration(Node e) throws XPathExpressionException
	{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		Node n = (Node)xpath.evaluate("name", e, XPathConstants.NODE);
		if (n != null)
			name = n.getTextContent();

		NodeList nl = (NodeList)xpath.evaluate("argument", e, XPathConstants.NODESET);
		arguments = new String[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++)
		{
			arguments[i] = nl.item(i).getTextContent();
		}

		nl = (NodeList)xpath.evaluate("property", e, XPathConstants.NODESET);
		if (nl.getLength() > 0)
		{
			properties = new String[nl.getLength()];
			for (int i = 0; i < nl.getLength(); i++)
			{
				properties[i] = nl.item(i).getTextContent();
			}
		}

		n = (Node)xpath.evaluate("inputFile", e, XPathConstants.NODE);
		if (n != null)
		{
			inputFile = n.getTextContent();
			inputFileFlags = 0;
			if (n.hasAttributes())
			{
				NamedNodeMap attrs = n.getAttributes();
				Node an = attrs.getNamedItem("type");
				if (an.getTextContent().equalsIgnoreCase("submitted"))
				{
					inputFileFlags = FILE_SUBMITTED;
				}
			}
		}

		n = (Node)xpath.evaluate("outputFile", e, XPathConstants.NODE);
		if (n != null)
			outputFile = n.getTextContent();
	}
	
	/**
	 * Set the array of arguments to be passed to a program.
	 * @param newArgs
	 */
	public void setArguments(String[] newArgs)
	{
		arguments = newArgs;
	}
	
	/**
	 * Set the name of an input file for the program's stdin.
	 * @param newInputFile
	 */
	public void setInputFile(String newInputFile)
	{
		inputFile = newInputFile;
	}
	
	/**
	 * Set the name of the output file.
	 * @param newOutputFile filename to use for output
	 */
	public void setOutputFile(String newOutputFile)
	{
		outputFile = newOutputFile;
	}

	/**
	 * Obtain the arguments array to use for the program.
	 * @return arguments array
	 */
	public String[] getArguments()
	{
		return arguments;
	}
	
	/**
	 * Set the array of properties to be used when running a program.
	 * @param newArgs
	 */
	public void setProperties(String[] newProps)
	{
		properties = newProps;
	}
	
	/**
	 * Obtain the arguments array to use for the program.
	 * Will be null if no properties were configured.
	 * @return arguments array
	 */
	public String[] getProperties()
	{
		return properties;
	}
		
	/**
	 * Get the name of the program.
	 * @return program name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Open the input file to be used for standard input when the program is run.
	 * @param subdir If inputFileFlags is FILE_SUBMITTED, the student's directory
	 *  containing the input file
	 * @return input stream from the input file
	 * @throws FileNotFoundException
	 */
	public FileInputStream openInputFile(String subdir) throws FileNotFoundException
	{
		if (inputFile == null)
			return null;
		String path = inputFile;
		if (inputFileFlags == FILE_SUBMITTED)
			path = subdir + File.separator + inputFile;
		return new FileInputStream(path);
	}
		
	/**
	 * Open the output file to contain standard output from the program.
	 * @param subdir The student's directory to contain the output file.
	 * @return output stream to the output file
	 * @throws FileNotFoundException
	 */
	public FileOutputStream openOutputFile(String subdir) throws FileNotFoundException
	{
		if (outputFile == null)
			return null;
		return new FileOutputStream(subdir + File.separator + outputFile);
	}
}
