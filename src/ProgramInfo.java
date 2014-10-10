import java.io.File;
import java.io.FileNotFoundException;
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
}
