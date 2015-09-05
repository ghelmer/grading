import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * Check and record the results of processing a student's assignment.
 * 
 * @author ghelmer
 *
 */
public class AssignmentResults implements Comparable<AssignmentResults>{
	public final static int MAX_ERROR_OUTPUT_SIZE = 1024 * 1024;
	public final static int MAX_STD_OUTPUT_SIZE = 1024 * 1024;
	private String name;
	private String fullName;
	private File dir;
	private ArrayList<String> userJavaFiles;
	private HashMap<String, String> requestedUserJavaFilesContents;
	private ArrayList<String> javaFiles;
	private ArrayList<String> otherFiles;
	private ArrayList<String> missingFiles;
	private String compilationOutput;
	private HashMap<String,String> programOutputs;
	private Date firstSubmissionDate;
	private double daysLate;
	
	/**
	 * Compare two AssignmentResults instances based first on
	 * submission date and then on student name.
	 * @param other the other AssignmentResults entry to compare with
	 * @return negative, 0, or positive value
	 */
	public int compareTo(AssignmentResults other)
	{
		/*
		int dateCompare = this.firstSubmissionDate.compareTo(other.firstSubmissionDate);
		if (dateCompare != 0)
		{
			return dateCompare;
		}
		return this.name.compareTo(other.name);
		}
		*/
		{
			int fullNameCompare = this.fullName.compareTo(other.fullName);
			if (fullNameCompare != 0)
			{
				return fullNameCompare;
			}
			return this.name.compareTo(other.name);
		}
	}
	
	/**
	 * Create the object to store all the results of
	 * processing a student's assignment submission.
	 * @param _name - Name of student
	 * @param _dir - directory containing student submission files
	 */
	public AssignmentResults(String _name, String _fullName, File _dir)
	{
		name = _name;
		if (_fullName == null)
		{
			fullName = _name;
		}
		else
		{
			fullName = _fullName;
		}
		dir = _dir;
		missingFiles = new ArrayList<String>();
		programOutputs = new HashMap<String,String>();
		userJavaFiles = new ArrayList<String>();
		requestedUserJavaFilesContents = new HashMap<String, String>();
		daysLate = 0;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String toString()
	{
		String separator = "--------------------------------------------------------------------------------\n";
		StringBuffer r = new StringBuffer();
		r.append("Name: " + name + " (" + fullName + ")\n");
		r.append("Submitted: " + firstSubmissionDate);
		if (daysLate > 0)
		{
			r.append(String.format(" (%.2f days late)", daysLate));
		}
		r.append("\n");
		r.append(separator);
		r.append("Java files found:\n");
		if (userJavaFiles != null)
		{
			for (String jf : userJavaFiles)
			{
				r.append("\t" + jf + "\n");
			}
		} else
		{
			r.append("\tNONE\n");
		}
		if (requestedUserJavaFilesContents.size() > 0)
		{
			r.append("Requested contents of Java files:\n");
			for (String key : requestedUserJavaFilesContents.keySet())
			{
				r.append(requestedUserJavaFilesContents.get(key));
			}
		}
		r.append("Other files found:\n");
		if (otherFiles != null)
		{
			for (String of : otherFiles)
			{
				r.append("--- Contents of " + of + " ---\n");
				try
				{
					r.append(getDocumentText(of));
				}
				catch (IOException e)
				{
					StackTraceElement[] sts = e.getStackTrace();
					StringBuffer sb = new StringBuffer();
					sb.append("Exception:\n");
					for (StackTraceElement st : sts)
					{
						sb.append('\t');
						sb.append(st.toString());
						sb.append('\n');
					}
					r.append("Exception: " + sb.toString());
				} catch (SAXException e) {
					StackTraceElement[] sts = e.getStackTrace();
					StringBuffer sb = new StringBuffer();
					sb.append("Exception:\n");
					for (StackTraceElement st : sts)
					{
						sb.append('\t');
						sb.append(st.toString());
						sb.append('\n');
					}
					r.append("SAXException: " + sb.toString());
				} catch (TikaException e) {
					StackTraceElement[] sts = e.getStackTrace();
					StringBuffer sb = new StringBuffer();
					sb.append("Exception:\n");
					for (StackTraceElement st : sts)
					{
						sb.append('\t');
						sb.append(st.toString());
						sb.append('\n');
					}
					r.append("TikaException: " + sb.toString());
				}
				r.append("\n--- End Contents of " + of + " ---\n");
			}
		}
		if (compilationOutput != null)
		{
			r.append("Compilation Output:\n-----\n" + compilationOutput + "\n-----\n");
		}
		else
		{
			r.append("*** Did not attempt to compile code. ***\n");
		}
		Object[] programOutputKeysObjects = programOutputs.keySet().toArray();
		String[] programOutputKeys = new String[programOutputKeysObjects.length];
		for (int i = 0; i < programOutputKeys.length; i++)
		{
			programOutputKeys[i] = (String) programOutputKeysObjects[i];
		}
		Arrays.sort(programOutputKeys);
		for (Object _pn : programOutputKeys)
		{
			String pn = (String)_pn;
			String programOutput = programOutputs.get(pn);
			if (programOutput != null)
			{
				r.append(String.format("Output from program %s:\n", pn));
				r.append(programOutput);
			}
			else
			{
				r.append(String.format("No output from program %s\n", pn));
			}
		}
		return r.toString();
	}
	
	/**
	 * Get the contents of the given file as a String.
	 */
	public String getFileAsString(File f) throws FileNotFoundException
	{
		StringBuffer sb = new StringBuffer();
		Scanner in = new Scanner(f);
		while (in.hasNextLine())
		{
			sb.append(in.nextLine());
			sb.append('\n');
		}
		in.close();
		return sb.toString();
	}
	
	/**
	 * Return the text from a document in the submission.
	 * @throws TikaException 
	 * @throws SAXException 
	 */
	public String getDocumentText(String docFilename) throws IOException, SAXException, TikaException
	{
		File inputFile = new File(dir.getAbsolutePath() + File.separator + docFilename);
		if (docFilename.endsWith(".txt"))
		{
			return getFileAsString(inputFile);
		}
		/* Otherwise: Use CleanContent to get text from file. */
		File outputFile = File.createTempFile(inputFile.getName(), ".out", inputFile.getParentFile());
		String about = ExtractTextFromFile.getText(inputFile, outputFile);
		String s = getFileAsString(outputFile);
		outputFile.delete();
		return about + "::\n" + s;
	}

	/**
	 * In the user directory, collect the Java files in the submissions (ordered by
	 * date) in the user's directory for compilation and execution.
	 * 
	 */
	public void copyJavaFilesToUser() throws IOException
	{
		Collections.sort(javaFiles);
		for (String jFile : javaFiles)
		{
			/* Copy to user directory. */
			File srcFile = new File(dir.getAbsolutePath() + File.separator + jFile);
			File destFile = new File(dir.getAbsolutePath() + File.separator + srcFile.getName());
			CopyFile.copy(srcFile, destFile);
			boolean found = false;
			for (int i = 0; i < userJavaFiles.size() && !found; i++)
			{
				if (userJavaFiles.get(i).equals(destFile.getName()))
				{
					found = true;
				}
			}
			if (!found)
			{
				userJavaFiles.add(destFile.getName());
			}
		}
	}

	/**
	 * Find earliest date of submission in the files.
	 * If submission date is after the due date, compute the
	 * number of days late.
	 */
	public void findSubmissionDate(Date dueDate) throws IOException
	{
		/* Date Submitted:Friday, October 21, 2011 4:22:36 PM CDT */
		/* Date Submitted: Friday, February 28, 2014 12:46:42 AM CST */
		Pattern dsPattern = Pattern.compile("^Date Submitted:\\s*\\S+, (\\S+ \\d+, \\d+ \\d+:\\d+:\\d+ \\S+ \\S+)$");
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.US);
		@SuppressWarnings("unchecked")
		ArrayList<String> otherFilesCopy = (ArrayList<String>)otherFiles.clone();
		Collections.sort(otherFilesCopy);
		for (String oFn : otherFilesCopy)
		{
			/* Build absolute pathname. */
			File oFile = new File(dir.getAbsolutePath() + File.separator + oFn);
			if (oFile.getName().equalsIgnoreCase("submission.txt"))
			{
				/* Open and read file until Date Submitted line is found. */
				Scanner in = new Scanner(oFile);
				boolean found = false;
				while (in.hasNextLine() && !found)
				{
					String line = in.nextLine();
					Matcher m = dsPattern.matcher(line);
					if (m.matches())
					{
						found = true;
						String dateString = m.group(1);
						try
						{
							Date parsedDate = df.parse(dateString);
							if (firstSubmissionDate == null || parsedDate.compareTo(firstSubmissionDate) < 0)
							{
								firstSubmissionDate = parsedDate;
							}
						}
						catch (ParseException pe)
						{
							pe.printStackTrace();
						}
					}
				}
				in.close();
			}
		}
		if (firstSubmissionDate == null)
		{
			try
			{
				firstSubmissionDate = df.parse("January 1, 2000 00:00:00 AM CST");
			}
			catch (ParseException pe)
			{
				pe.printStackTrace();
			}
		}
		else
		{
			if (firstSubmissionDate.after(dueDate))
			{
				daysLate = (firstSubmissionDate.getTime() - dueDate.getTime()) /
						(double)(24 * 60 * 60 * 1000);
			}
		}
	}

	/**
	 * Strip the "package ...;" statement, if any, from Java files in the user's
	 * directory. 
	 */
	public void stripPackageFromJavaFiles() throws IOException
	{
		for (String jFile : userJavaFiles)
		{
			File srcFile = new File(dir.getAbsolutePath() + File.separator + jFile);
			File destFile = new File(dir.getAbsolutePath() + File.separator + jFile + ".new");
			
			Scanner in = new Scanner(srcFile);
			PrintWriter out = new PrintWriter(destFile);
			boolean replace = false;
			while (in.hasNextLine())
			{
				String s = in.nextLine();
				if (!s.startsWith("package "))
				{
					out.println(s);
				}
				else
				{
					replace = true;
				}
			}
			in.close();
			out.close();
			if (replace)
			{
				long modTime = srcFile.lastModified();
				if (modTime != 0L)
				{
					destFile.setLastModified(modTime);
				}
				destFile.renameTo(srcFile);
			}
			else
			{
				destFile.delete();
			}
		}
	}

	/**
	 * Organize BlackBoard download.
	 * For each file in the directory, if it is in the form
	 * Homework201_njvang_attempt_2011-09-02-20-09-43_BankAccount.java
	 * then move the file into a directory <user>/<date>/<file>.
	 * 
	 */
	public static void organizeBlackBoardFiles(File dir) throws IOException
	{
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir.getName() + " is not a directory");
		}
		
		/* First step: organize files into per-user / per-submission directories. */
		File[] allFiles = dir.listFiles();
		Pattern hwFilePattern = Pattern.compile("[A-Z][ a-zA-Z0-9]*_(.*)_attempt_([0-9-]*)[_.](.*)$");
		for (File d : allFiles)
		{
			Matcher m = hwFilePattern.matcher(d.getName());
			if (m.matches())
			{
				String user = m.group(1);
				String date = m.group(2);
				String fn = m.group(3);
				if (fn.equals("txt"))
				{
					fn = "submission.txt";
				}
				File userDir = new File(dir.getAbsolutePath() + File.separator + user);
				if (!userDir.isDirectory())
				{
					userDir.mkdir();
				}
				File submissionDir = new File(dir.getAbsolutePath() + File.separator + user + File.separator + date);
				if (!submissionDir.isDirectory())
				{
					submissionDir.mkdir();
				}
				File destFile = new File(submissionDir.getAbsolutePath() + File.separator + fn);
				d.renameTo(destFile);
			}
		}
	}
	
	/**
	 * Find the files in the directory.
	 * Build a list of the files.
	 * 
	 * @param dir - Directory to examine
	 * @return true if successful
	 * @throws IOException
	 */
	public void findFiles(File dir) throws IOException
	{
		ArrayList<String> jfs = new ArrayList<String>();
		ArrayList<String> ofs = new ArrayList<String>();
		
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir.getName() + " is not a directory");
		}
		ArrayList<String> allFiles = new ArrayList<String>();
		findFiles(dir, "", allFiles);
		for (String e : allFiles)
		{
			File f = new File (e);
			if (f.getName().startsWith("."))
			{
				/* Ignore names starting with '.'. */
				continue;
			}
			if (e.endsWith(".classpath"))
			{
				/* Ignore. */
				continue;
			}
			if (e.endsWith(".DS_Store"))
			{
				/* Ignore. */
				continue;
			}
			if (e.endsWith(".prefs"))
			{
				/* Ignore. */
				continue;
			}
			if (e.endsWith(".class"))
			{
				/* Ignore. */
				continue;
			}
			if (e.endsWith(".jar"))
			{
				/* Ignore. */
				continue;
			}
			else if (e.endsWith(".java"))
			{
				jfs.add(e);
			}
			else
			{
				ofs.add(e);
			}
		}
		javaFiles = jfs;
		otherFiles = ofs;
	}

	/**
	 * Recursively find all the files in the subdirectory.
	 * 
	 * @param dir - Directory to examine
	 * @param subdirName - relative pathname for this directory
	 * @return The list of all files found, including the
	 * relative path starting from the top-level directory.
	 * @throws IOException
	 */
	public void findFiles(File dir, String subdirName, ArrayList<String> foundFiles) throws IOException
	{
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(subdirName + dir.getName() + " is not a directory");
		}
		for (File e : dir.listFiles())
		{
			if (e.getName().startsWith("."))
			{
				/* Ignore names starting with '.'. */
				continue;
			}
			if (e.isDirectory())
			{
				/*
				 * Enter the subdirectory and find its files.
				 */
				findFiles(e, subdirName + e.getName() + File.separator, foundFiles);
			}
			else if (e.isFile())
			{
				if (e.getName().endsWith(".zip"))
				{
					/*
					 * Extract and add files to list.
					 */
					try
					{
						ArrayList<String> unzippedFiles = Unzip.unzip(e);
						for (String s : unzippedFiles)
						{
							foundFiles.add(subdirName + s);
						}
					}
					catch (ZipException ze)
					{
						ze.printStackTrace();
					}
				}
				else
				{
					if (subdirName.length() == 0 && e.getName().endsWith(".java"))
					{
						System.err.println("Skipping Java file " + subdirName + e.getName() + " in student " + name + " root directory");
					}
					else
					{
						foundFiles.add(subdirName + e.getName());
					}
				}
			}
			else
			{
				System.err.println(subdirName + e.getName() + " is not a directory or a file");
			}
		}
	}

	/**
	 * Check the list of java files found in the student's directory.
	 * Set any missing java files in the missingJavaFiles list.
	 * 
	 * @param foundFiles - List of Java files found
	 * @param missingFiles - List of required files that were not found
	 * @return true if all required files were found, or false otherwise.
	 */
	public boolean checkRequiredJavaFiles(ProgramInfo[] programs)
	{
		boolean result = true;
		for (ProgramInfo pi : programs)
		{
			for (AssignmentClasses clss : pi.getClasses())
			{
				String className = clss.getClassName() + ".java";
				boolean fileFound = false;
				for (int i = 0; i < userJavaFiles.size() && !fileFound; i++)
				{
					String foundFile = userJavaFiles.get(i);
					int lastSlash = foundFile.lastIndexOf(File.separator);
					if (lastSlash != -1)
					{
						foundFile = foundFile.substring(lastSlash + 1);
					}
					if (foundFile.equals(className))
					{
						fileFound = true;
					}
				}
				if (!fileFound)
				{
					missingFiles.add(className);
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * Obtain the contents of specified java files copied to the student's root
	 * directory for inclusion in the report.
	 * 
	 * @param programs - array of programs classes
	 */
	public void showRequestedJavaFiles(ProgramInfo[] programs)
	{
		for (ProgramInfo pi : programs)
		{
			for (AssignmentClasses clss : pi.getClasses())
			{
				if (clss.showClass())
				{
					StringBuffer r = new StringBuffer();
					String className = clss.getClassName() + ".java";
					String foundFilename = null;
					for (int i = 0; i < userJavaFiles.size() && foundFilename == null; i++)
					{
						String foundFile = userJavaFiles.get(i);
						int lastSlash = foundFile.lastIndexOf(File.separator);
						if (lastSlash != -1)
						{
							foundFile = foundFile.substring(lastSlash + 1);
						}
						if (foundFile.equals(className))
						{
							foundFilename = dir.getAbsolutePath() + File.separator + foundFile;
						}
					}
					if (foundFilename == null)
					{
						r.append("---- Java file " + className + " NOT FOUND! ----\n");
					}
					else
					{
						r.append("---- Java file " + className + " ----\n");
						try
						{
							r.append(getFileAsString(new File(foundFilename)));
						}
						catch (IOException e)
						{
							r.append("Exception " + e.getMessage() + " while reading file " + foundFilename + "\n");
							StackTraceElement[] sts = e.getStackTrace();
							for (StackTraceElement st : sts)
							{
								r.append('\t');
								r.append(st.toString());
								r.append('\n');
							}
						}
						r.append("\n---- End of Java file " + foundFilename + " ----\n");
					}
					requestedUserJavaFilesContents.put(className, r.toString());
				}
			}
		}
	}

	/**
	 * Compile the Java source files under the given directory.
	 * Return a string describing the results.
	 * 
	 * @param dir - working directory
	 * @param sourceFiles - Array of files to compile
	 * @return status
	 */
	public boolean compileJavaFiles(File dir) throws IOException, InterruptedException
	{
		StringBuffer output = new StringBuffer();
		
		if (userJavaFiles.size() == 0)
			return true;
		Runtime r = Runtime.getRuntime();
		String[] cmd = new String[userJavaFiles.size() + 1];
		cmd[0] = "javac";
		for (int i = 1; i <= userJavaFiles.size(); i++)
			cmd[i] = userJavaFiles.get(i - 1);
		Process result = r.exec(cmd, null, dir);
		Scanner in = new Scanner(result.getErrorStream());
		while (in.hasNextLine())
		{
			//System.out.println("Output from javac: " + in.nextLine());
			output.append(in.nextLine() + "\n");
		}
		in.close();
		result.waitFor();
		if (output.length() != 0)
		{
			compilationOutput = output.toString();
			return false;
		}
		else if (result.exitValue() != 0)
		{
			compilationOutput = "Compiler exit code: " + result.exitValue();
			return false;
		}
		compilationOutput = "OK";
		return true;
	}

	/**
	 * Run each of the programs that should have been submitted.
	 * 
	 * @param programs - Programs and run configurations
	 * @param dir - working directory in which to run the programs
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void runJavaPrograms(ProgramInfo[] programs, File dir) throws IOException, InterruptedException
	{
		Runtime r = Runtime.getRuntime();
		
		for (ProgramInfo pi : programs)
		{
			String program = pi.getName();
			String classpath = pi.getClasspath();
			URI securityPolicyURI = null;
			String securityPolicyFileStr = pi.getSecurityPolicyFile();
			if (securityPolicyFileStr != null)
			{
				File securityPolicyFile = new File(securityPolicyFileStr);
				securityPolicyURI = securityPolicyFile.toURI();
			}
			for (RunConfiguration rc : pi.getRunConfigurations())
			{
				StringBuffer output = new StringBuffer();
				String[] args = rc.getArguments();
				int maxArgs = args.length + 2;
				if (classpath != null)
				{
					maxArgs += 2;
				}
				if (securityPolicyURI != null)
				{
					maxArgs += 3;
				}
				int numArgs = 0;
				String[] cmd = new String[maxArgs];
				cmd[numArgs++] = "java";
				if (classpath != null)
				{
					cmd[numArgs++] = "-classpath";
					cmd[numArgs++] = classpath;
				}
				if (securityPolicyURI != null)
				{
					// Set parameter grading.base to the directory containing the directory
					// that contains the homework submissions.
					cmd[numArgs++] = "-Dgrading.base=" + dir.getParentFile().getParent();
					// System.out.println("grading.base=" + cmd[numArgs - 1]);
					cmd[numArgs++] = "-Djava.security.manager";
					cmd[numArgs++] = "-Djava.security.policy=" + securityPolicyURI.toString();
				}
				cmd[numArgs++] = program;
				System.arraycopy(args, 0, cmd, numArgs, args.length);
				numArgs += args.length;
				System.out.println("Running " + name + " program " + program);
				try
				{
					/* Setup input for the program, if any. */
					BufferedInputStream programInput = null;
					FileInputStream inputStream = rc.openInputFile(dir.getAbsolutePath());
					if (inputStream != null)
					{
						programInput = new BufferedInputStream(inputStream);
					}
					StreamConnector stdinConnector = null;

					/*
					 * Setup output for the program, either to an output file or
					 * memory buffer.
					 */
					OutputStream outputStream = rc.openOutputFile(dir.getAbsolutePath());
					ByteArrayOutputStream storedOutputStream = null;
					if (outputStream == null)
					{
						/* Collect output in a ByteArrayOutputStream. */
						storedOutputStream = new ByteArrayOutputStream(MAX_STD_OUTPUT_SIZE);
						outputStream = storedOutputStream;
					}
					BufferedOutputStream programOutput = new BufferedOutputStream(outputStream);
					StreamConnector stdoutConnector = null;

					/* Setup error output for the program. */
					ByteArrayOutputStream storedErrorStream = new ByteArrayOutputStream(MAX_ERROR_OUTPUT_SIZE);
					BufferedOutputStream programError = new BufferedOutputStream(storedErrorStream);
					StreamConnector stderrConnector = null;
					
					try
					{
						final Process process = r.exec(cmd, null, dir);

						/*
						 * Connect and start the threads to copy stdin, stdout,
						 * and stderr for the child process.
						 */
						if (programInput != null)
						{
							stdinConnector = new StreamConnector(programInput, process.getOutputStream(), false, true, "StdIn");
							stdinConnector.start();
						}

						stdoutConnector = new StreamConnector(process.getInputStream(), programOutput, "StdOut");
						stdoutConnector.start();

						stderrConnector = new StreamConnector(process.getErrorStream(), programError, "StdErr");
						stderrConnector.start();

						/*
						 *  Setup a timer to expire and terminate the child
						 *  process after a reasonable delay.
						 */
					    Timer t = new Timer();
					    t.schedule(new TimerTask() {

					        @Override
					        public void run() {
					            process.destroy();
					        }
					    }, 15000);   // it will kill the process after 15 seconds (if it's not finished yet).
					    process.waitFor();
					    t.cancel();
					    
					    stdoutConnector.join();
					    stderrConnector.join();

						/* Read stdout and stderr into output arraylist. */
						if (storedOutputStream != null)
						{
							if (storedOutputStream.size() == 0)
							{
								output.append(String.format("----- No output from %s java %s run %s ------\n", name, program, rc.getName()));
							}
							else
							{
								output.append(String.format("----- Start of output from %s java %s run %s ------\n", name, program, rc.getName()));
								ByteArrayInputStream conv = new ByteArrayInputStream(storedOutputStream.toByteArray());
								Scanner in = new Scanner(conv);
								while (in.hasNextLine())
								{
									output.append(in.nextLine());
									output.append('\n');
								}
								in.close();
								output.append(String.format("----- End of output from %s java %s run %s ------\n", name, program, rc.getName()));
							}
						}
						if (storedErrorStream != null)
						{
							if (storedErrorStream.size() != 0)
							{
								output.append(String.format("----- Start of error output from %s java %s run %s ------\n", name, program, rc.getName()));
								ByteArrayInputStream conv = new ByteArrayInputStream(storedErrorStream.toByteArray());
								Scanner errIn = new Scanner(conv);
								while (errIn.hasNextLine())
								{
									output.append(errIn.nextLine());
									output.append('\n');
								}
								errIn.close();
								output.append(String.format("----- End of error output from %s java %s run %s ------\n", name, program, rc.getName()));
							}
						}

						if (process.exitValue() != 0)
						{
							output.append(String.format("*** %s java %s run %s exit code %d\n", name, program, rc.getName(), process.exitValue()));
						}
						programOutputs.put(program + '.' + rc.getName(), output.toString());
					}
					finally
					{
						if (programInput != null)
						{
							programInput.close();
						}
						if (programOutput != null)
						{
							programOutput.close();
						}
					}
				}
				catch (IOException e)
				{
					programOutputs.put(program + '.' + rc.getName(), "IOException: " + e.getMessage());
				}
				catch (InterruptedException e)
				{
					programOutputs.put(program + '.' + rc.getName(), "InterruptedException: " + e.getMessage());
				}
			}	
		}
	}
	
	public int hashCode()
	{
		return name.hashCode();
	}
	
	public boolean equals(AssignmentResults ar)
	{
		return name.equals(ar.name);
	}
}
