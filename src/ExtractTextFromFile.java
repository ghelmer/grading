import net.bitform.api.options.AnalyzeOption;
import net.bitform.api.options.Option;
import net.bitform.api.options.ScrubOption;
import net.bitform.api.secure.SecureOptions;
import net.bitform.api.secure.SecureRequest;
import net.bitform.api.secure.SecureResponse;
import java.io.File;
import java.io.IOException;

public class ExtractTextFromFile {
	public static String getText(File inputFile, File outputFile) throws IOException {
		if (!inputFile.exists() || !inputFile.isFile()) {
			throw new IllegalArgumentException(inputFile.toString() + " is not a valid file");
		}

		StringBuffer resultBuffer = new StringBuffer();
		
		/**
		 * Create and setup the SecureRequest object
		 */
		SecureRequest request = new SecureRequest();
		/* ScrubbedDocument: FileOption */
		//File scrubbedFile = new File("/tmp/output.bin");
		request.setOption(SecureOptions.JustAnalyze, true);
		//request.setOption(SecureOptions.ScrubbedDocument, scrubbedFile);
		request.setOption(SecureOptions.SourceDocument, inputFile);
		request.setOption(SecureOptions.ResultDocument, outputFile);
		request.setOption(SecureOptions.OutputType, SecureOptions.OutputTypeOption.ToText);
		request.setOption(SecureOptions.ToTextEncoding, SecureOptions.ToTextEncodingOption.UTF8);

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
			resultBuffer.append("The file " + inputFile.getName() +
					" of format " +
					response.getResult(SecureOptions.SourceFormat).getName() +
					" contains:\n");
			Option[] options = SecureOptions.getInstance().getAllOptions();
			for (int j = 0; j < options.length; j++) {
				if (options[j] instanceof ScrubOption) {
					if (response.getResult((ScrubOption) options[j]) == ScrubOption.Reaction.EXISTS)
					{
						resultBuffer.append('\t');
						resultBuffer.append(options[j].getName());
						resultBuffer.append('\n');
					}
				} else if (options[j] instanceof AnalyzeOption) {
					if (response.getResult((AnalyzeOption) options[j]) == AnalyzeOption.Reaction.EXISTS)
					{
						resultBuffer.append('\t');
						resultBuffer.append(options[j].getName());
						resultBuffer.append('\n');
					}
				}
			}
		} else {
			resultBuffer.append("The file " + inputFile.getName() + " of format " + response.getResult(SecureOptions.SourceFormat).getName() + " could not be processed.\n");
		}
		return resultBuffer.toString();
	}
}

