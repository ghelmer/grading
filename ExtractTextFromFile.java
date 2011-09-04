import net.bitform.api.options.AnalyzeOption;
import net.bitform.api.options.Option;
import net.bitform.api.options.ScrubOption;
import net.bitform.api.secure.SecureOptions;
import net.bitform.api.secure.SecureRequest;
import net.bitform.api.secure.SecureResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ExtractTextFromFile {
	public void getText(String inputFile) {

		/**
		 * Check the command line
		 */

		File file = null;

		file = new File(inputFile);

		if (!file.exists() || !file.isFile()) {
			System.out.println(file.toString() + " is not a valid file");
			return;
		}

		/**
		 * Create and setup the SecureRequest object
		 */
		SecureRequest request = new SecureRequest();
		/* ScrubbedDocument: FileOption */
		//File scrubbedFile = new File("/tmp/output.bin");
		File outputFile = new File("/tmp/output.txt");
		request.setOption(SecureOptions.JustAnalyze, true);
		//request.setOption(SecureOptions.ScrubbedDocument, scrubbedFile);
		request.setOption(SecureOptions.SourceDocument, file);
		request.setOption(SecureOptions.ResultDocument, outputFile);
		request.setOption(SecureOptions.OutputType, SecureOptions.OutputTypeOption.ToText);
		request.setOption(SecureOptions.ToTextEncoding, SecureOptions.ToTextEncodingOption.UTF8);
		try	{
			/**
			 * Execute the request
			 */

			request.execute();

			/**
			 * Get the response
			 */

			SecureResponse response = request.getResponse();

			if (response.getResult(SecureOptions.WasProcessed)) {
				/**
				 * Prints all the scrub and analyze targets in the file.
				 * Note that in a production version a developer would probably
				 * want to cache the list scrub/analyse targets to improve
				 * performance.
				 */
				System.out.println("The file " + file.getName() +
						" of format " +
						response.getResult(SecureOptions.SourceFormat).getName() +
						" contains:");
				Option[] options = SecureOptions.getInstance().getAllOptions();
				for (int j = 0; j < options.length; j++) {
					if (options[j] instanceof ScrubOption) {
						if (response.getResult((ScrubOption) options[j]) == ScrubOption.Reaction.EXISTS)
							System.out.println("   " + options[j].getName());
					} else if (options[j] instanceof AnalyzeOption) {
						if (response.getResult((AnalyzeOption) options[j]) == AnalyzeOption.Reaction.EXISTS)
							System.out.println("   " + options[j].getName());
					}
				}
			} else {
				System.out.println("The file " + file.getName() + " of format " + response.getResult(SecureOptions.SourceFormat).getName() + " could not be processed.");
			}
		} catch (IOException e) {
			System.out.println("IOException in file " + file.getName());
			e.printStackTrace();
		} catch (RuntimeException e) {
			System.out.println("RuntimeException in file " + file.getName());
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			System.out.println("OutOfMemoryError in file " + file.getName());
			e.printStackTrace();
		} catch (StackOverflowError e) {
			System.out.println("StackOverflowError in file " + file.getName());
			e.printStackTrace();
		}
	}
}

