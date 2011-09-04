import java.io.PrintStream;

/**
 * Class to execute functions and handle/re-throw
 * exceptions.
 * 
 * @author ghelmer
 *
 */
public class Exceptions {
	public static RuntimeException toRuntime(Exception e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		String msg = e.getMessage();
		if (msg != null) {
			return new RuntimeException(msg, e);
		}
		return new RuntimeException(e);
	}

	public static abstract class VoidBlock extends Exceptions {
		abstract protected void inner() throws Exception;

		public void exec() {
			try {
				inner();
			} catch (Exception e) {
				//log(e);
				throw toRuntime(e);
			}
		}

		public void execSwallowingException(PrintStream err) {
			try {
				inner();
			} catch (Exception e) {
				e.printStackTrace(err);
			}
		}
	}
}
