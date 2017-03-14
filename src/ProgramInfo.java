import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.xpath.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Keep the information about a program, its components, and
 * how to run it.
 * 
 * @author ghelmer
 *
 */
public class ProgramInfo {
	private String name;
	private String classpath;
	private String securityPolicyFile;
	private AssignmentClasses[] classes;
	private RunConfiguration[] runConfigurations;
	private CopyFile[] filesToCopy;
	
	/**
	 * Construct a new ProgramInfo object from the name, classes,
	 * and runConfigurations elements under the given program
	 * XML element.
	 * 
	 * @param program - XML node heading the program specification
	 * @throws XPathExpressionException
	 * @throws FileNotFoundException 
	 */
	public ProgramInfo(Node program) throws XPathExpressionException, FileNotFoundException
	{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		Node n = (Node)xpath.evaluate("name", program, XPathConstants.NODE);
		name = n.getTextContent();
		
		n = (Node)xpath.evaluate("classpath", program, XPathConstants.NODE);
		if (n != null)
			classpath = n.getTextContent();

		n = (Node)xpath.evaluate("securityPolicyFile", program, XPathConstants.NODE);
		if (n != null)
		{
			securityPolicyFile = n.getTextContent();
			File spf = new File(securityPolicyFile);
			if (!spf.exists())
			{
				throw new FileNotFoundException("Security policy file " + securityPolicyFile + " does not exist");
			}
		}

		NodeList nl = (NodeList)xpath.evaluate("class", program, XPathConstants.NODESET);
		classes = new AssignmentClasses[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++)
		{
			String className = nl.item(i).getTextContent();
			Node showClassAttribute = nl.item(i).getAttributes().getNamedItem("showClass");
			boolean showClass = false;
			if (showClassAttribute != null)
			{
				showClass = showClassAttribute.getTextContent().equalsIgnoreCase("yes");
			}
			classes[i] = new AssignmentClasses(className, showClass);
		}

		// Get list of files to copy into place, if any.
		nl = (NodeList)xpath.evaluate("copyFile", program, XPathConstants.NODESET);
		filesToCopy = new CopyFile[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++)
		{
			String srcPath = (String)xpath.evaluate("srcPath", nl.item(i), XPathConstants.STRING);
			String destBase = (String)xpath.evaluate("destBase", nl.item(i), XPathConstants.STRING);
			if (srcPath != null && destBase != null)
			{
				File srcPathFile = new File(srcPath);
				if (!srcPathFile.exists())
				{
					throw new FileNotFoundException("copyFile/srcPath " + srcPath + " does not exist");
				}
				filesToCopy[i] = new CopyFile(srcPathFile, destBase);
			}
		}

		nl = (NodeList)xpath.evaluate("runConfiguration", program, XPathConstants.NODESET);
		runConfigurations = new RunConfiguration[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++)
		{
			runConfigurations[i] = new RunConfiguration(nl.item(i));
		}
	}
	
	/**
	 * Get the name of the program.
	 * 
	 * @return program
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Get the classpath to use when running the program.
	 * @return classpath
	 */
	public String getClasspath()
	{
		return classpath;
	}
	
	/**
	 * Get the name of the security policy file, or null if none.
	 * @return securityPolicyFile
	 */
	public String getSecurityPolicyFile()
	{
		return securityPolicyFile;
	}

	/**
	 * Get the list of run configurations for this program.
	 * 
	 * @return run configurations
	 */
	public RunConfiguration[] getRunConfigurations()
	{
		return runConfigurations;
	}
	
	/**
	 * Get the list of class names for this program.
	 * 
	 * @return class names
	 */
	public AssignmentClasses[] getClasses()
	{
		return classes;
	}
	
	/**
	 * Get the list of files to copy, if any.
	 */
	public CopyFile[] getFilesToCopy()
	{
		return filesToCopy;
	}
}
