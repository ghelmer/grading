grading
=======

Grading Helper Project

Guy Helmer <ghelmer@iastate.edu>

This is a project to assist with grading assignments downloaded from BlackBoard. I started writing it several years ago when times were so “tight” that I had needed to streamline grading. It organizes the files downloaded from Blackboard into per-student directories, compiles & executes specified classes, and extracts text from any submitted text documents (PDFs, Word docs, etc). It writes a plain text output file named “GradingReport.txt” in the grading directory. 

It needs the Apache Tika (text extraction) library to extract text from documents. You can change the class path setting in the Eclipse project to point to your Tika app JAR tika-app-1.13.jar

Some useful features of the grading helper program include:
* using a text file for input to student’s programs (because of Scanner's buffering, this breaks programs when students open multiple Scanners on System.in, so I warn students to only use *one* Scanner object to read from System.in)
* limiting the amount of time the student’s program is allowed to run (which handles infinite loops or infinitely-waiting for unplanned user input),
* limiting the amount of output to 4KB or so, and
* computing Levenshtein edit distance between text extracted from documents to find near-duplicates
* applying security policy to prevent unexpected access to files or other problems

Here is how I use it:
1. I export the student user-ids and names (tab-separated) into the Students.txt file (this enables showing the student name in the grading report).
1. I download student’s submissions from Blackboard as a Zip archive (click the arrow in the assignment header in the Gradebook, select assignment download, select all the student(s) to download submissions from, select “all submissions” at the bottom, and click OK — then click the link that appears to actually download the Zip archive.
1. I unpack the zip into a directory. Also in the directory, I place two files: the grading.xml specification that defines which classes to compile, which class contains the main method, and the run specifications that specify any command-line parameters or input files to provide via standard input. The other file, security.policy, specifies the Java security properties that are applied when executing the student’s program — this protects against any programming mistakes or malicious code. 
1. Then I run the GradingHelper program and, for the command arguments, specify the directory containing the extracted students submissions, grading.xml file, and security.policy file. 
1. If all goes well, the program will write the output to the GradingReport.txt file. Review the GradingReport.txt file to determine if any common problems occur frequently, like:
   * Exceptions from Scanner while reading input — these may indicate problems with the input files
   * Exceptions resulting from security.policy settings — the policy may need to be adjusted to enable successful execution
1. Then, I create a new Zip archive of the directory (including the GradingReport.txt file and the organized student submission files) and share with the TA for grading.
