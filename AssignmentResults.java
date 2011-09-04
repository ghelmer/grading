import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check and record the results of processing a student's assignment.
 * 
 * @author ghelmer
 *
 */
public class AssignmentResults {
	private String name;
	private ArrayList<String> javaFiles;
	private ArrayList<String> otherFiles;
	private ArrayList<String> missingFiles;
	private String compilationOutput;
	private HashMap<String,String> programOutputs;
	
	/**
	 * Create the object to store all the results of
	 * processing a student's assignment submission.
	 * @param name - Name of student
	 */
	public AssignmentResults(String name)
	{
		this.name = name;
		missingFiles = new ArrayList<String>();
		programOutputs = new HashMap<String,String>();
	}
	
	public String getName()
	{
		return name;
	}
	
	public String toString()
	{
		StringBuffer r = new StringBuffer();
		r.append("Name: " + name + "\n");
		r.append("Java files found:\n");
		if (javaFiles != null)
		{
			for (String jf : javaFiles)
			{
				r.append("\t" + jf + "\n");
			}
		} else
		{
			r.append("\tNONE\n");
		}
		r.append("Other files found:\n");
		if (otherFiles != null)
		{
			for (String of : otherFiles)
			{
				r.append("\t" + of + "\n");
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
				r.append("Output from program " + pn + ":\n-----\n" + programOutput + "\n-----\n");
			}
			else
			{
				r.append("*** No output from program " + pn + " ***\n");
			}
		}
		return r.toString();
	}
	
	/**
	 * Organize BlackBoard download.
	 * For each file in the directory, if it is in the form
	 * Homework201_njvang_attempt_2011-09-02-20-09-43_BankAccount.java
	 * then move the file into a directory <user>/<date>/<file>.
	 * Then, for each user directory, find the Java files in the submissions (ordered by
	 * date) and collect them in the user's directory for compilation and execution.
	 * 
	 */
	public static void organizeBlackBoardFiles(File dir) throws IOException
	{
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir.getName() + " is not a directory");
		}
		
		/* First step: organize files into per-user / per-submission directories. */
		File[] allFiles = dir.listFiles();
		Pattern hwFilePattern = Pattern.compile("Homework[0-9]*_(.*)_attempt_([0-9-]*)[_.](.*)$");
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

		allFiles = dir.listFiles();
		Pattern submitPattern = Pattern.compile("^[0-9]+-");
		for (File userDir : allFiles)
		{
			if (userDir.isDirectory())
			{
				ArrayList<String> submitDirs = new ArrayList<String>();
				for (File subdir : userDir.listFiles())
				{
					Matcher m = submitPattern.matcher(subdir.getName());
					if (m.matches())
					{
						submitDirs.add(subdir.getName());
					}
				}
				Collections.sort(submitDirs);
				for (String _submitDir : submitDirs)
				{
					File submitDir = new File(userDir.getAbsolutePath() + File.separator + _submitDir);
					for (File f : submitDir.listFiles())
					{
						if (f.getName().endsWith(".java"))
						{
							/* Copy to user directory. */
							File destFile = new File(userDir.getAbsolutePath() + File.separator + f.getName());
							CopyFile.copy(f, destFile);
						}
					}
				}
				
			}
		}

	}
	
	/**
	 * Find the files in the directory.
	 * Build a list of Java files and other files.
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
		ArrayList<String> allFiles = findFiles(dir, "");
		for (String e : allFiles)
		{
			if (e.endsWith(".java"))
			{
				/* Only add files in the "root" to the list of Java files. */
				if (e.indexOf(File.separator) == -1)
				{
					jfs.add(e);
				}
			}
			else
			{
				ofs.add(e);
			}
		}
		for (String s : jfs)
		{
			System.out.println(name + ": Found Java file " + s);
		}
		for (String s : ofs)
		{
			// Extract text from other files
			System.out.println(name + ": Found other file " + s);
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
	public ArrayList<String> findFiles(File dir, String subdirName) throws IOException
	{
		ArrayList<String> foundFiles = new ArrayList<String>();
		
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
				ArrayList<String> subdirFiles = findFiles(e, subdirName + e.getName() + File.separator);
				for (String sf : subdirFiles)
				{
					foundFiles.add(sf);
				}
			}
			else if (e.isFile())
			{
				if (e.getName().endsWith(".zip"))
				{
					/*
					 * Extract and add files to list.
					 */
					ArrayList<String> unzippedFiles = Unzip.unzip(e);
					for (String s : unzippedFiles)
					{
						foundFiles.add(subdirName + s);
					}
				}
				else
				{
					foundFiles.add(subdirName + e.getName());
				}
			}
			else
			{
				System.err.println(subdirName + e.getName() + " is not a directory or a file");
			}
		}
		return foundFiles;
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
			for (String clss : pi.getClasses())
			{
				String className = clss + ".java";
				boolean fileFound = false;
				for (int i = 0; i < javaFiles.size() && !fileFound; i++)
				{
					String foundFile = javaFiles.get(i);
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
		
		Runtime r = Runtime.getRuntime();
		String[] cmd = new String[javaFiles.size() + 1];
		cmd[0] = "javac";
		for (int i = 1; i <= javaFiles.size(); i++)
			cmd[i] = javaFiles.get(i - 1);
		Process result = r.exec(cmd, null, dir);
		Scanner in = new Scanner(result.getErrorStream());
		while (in.hasNextLine())
		{
			//System.out.println("Output from javac: " + in.nextLine());
			output.append(in.nextLine() + "\n");
		}
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
			for (RunConfiguration rc : pi.getRunConfigurations())
			{
				BufferedInputStream programInput = null;
				BufferedOutputStream programOutput = null;
				
				StringBuffer output = new StringBuffer();
				String[] args = rc.getArguments();
				String[] cmd = new String[args.length + 2];
				cmd[0] = "java";
				cmd[1] = program;
				System.arraycopy(args, 0, cmd, 2, args.length);

				try
				{
					FileInputStream inputFile = rc.openInputFile(dir.getAbsolutePath());
					StreamConnector stdin = null;

					FileOutputStream outputFile = rc.openOutputFile(dir.getAbsolutePath());
					StreamConnector stdout = null;

					Process process = r.exec(cmd, null, dir);

					if (inputFile != null)
					{
						programInput = new BufferedInputStream(inputFile);
						stdin = new StreamConnector(programInput, process.getOutputStream(),"StdIn");
						stdin.start();
					}

					if (outputFile != null)
					{
						programOutput = new BufferedOutputStream(outputFile);
						stdout = new StreamConnector(process.getInputStream(), programOutput, "StdOut");
						stdout.start();
					}

					Scanner in = new Scanner(process.getInputStream());
					while (in.hasNextLine())
					{
						//System.out.println("Output from javac: " + in.nextLine());
						output.append(in.nextLine() + "\n");
					}
					process.waitFor();
					if (process.exitValue() != 0)
					{
						output.append("*** Exit code: " + process.exitValue());
					}
					programOutputs.put(program + '.' + rc.getName(), output.toString());
				}
				catch (IOException e)
				{
					programOutputs.put(program + '.' + rc.getName(), "IOException: " + e.getMessage());
				}
				catch (InterruptedException e)
				{
					programOutputs.put(program + '.' + rc.getName(), "InterruptedException: " + e.getMessage());
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
