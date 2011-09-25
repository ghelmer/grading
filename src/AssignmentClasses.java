/**
 * Maintain information about a class to be graded.
 * @author ghelmer
 *
 */
public class AssignmentClasses {
	private String className;
	private boolean showClass;
	
	/**
	 * Initialize a new Java class in the assignment.
	 * @param name - Name of the Java class
	 * @param show - Whether to show the contents of the Java file in the report
	 */
	public AssignmentClasses(String name, boolean show)
	{
		className = name;
		showClass = show;
	}
	
	public String getClassName()
	{
		return className;
	}
	
	public boolean showClass()
	{
		return showClass;
	}
}
