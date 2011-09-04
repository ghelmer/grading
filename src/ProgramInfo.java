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
	private String[] classes;
	private RunConfiguration[] runConfigurations;
	
	/**
	 * Construct a new ProgramInfo object from the name, classes,
	 * and runConfigurations elements under the given program
	 * XML element.
	 * 
	 * @param program - XML node heading the program specification
	 * @throws XPathExpressionException
	 */
	public ProgramInfo(Node program) throws XPathExpressionException
	{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		Node n = (Node)xpath.evaluate("name", program, XPathConstants.NODE);
		name = n.getTextContent();
		
		NodeList nl = (NodeList)xpath.evaluate("class", program, XPathConstants.NODESET);
		classes = new String[nl.getLength()];
		for (int i = 0; i < nl.getLength(); i++)
		{
			classes[i] = nl.item(i).getTextContent();
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
	public String[] getClasses()
	{
		return classes;
	}
}