package cuanto.user;

import java.util.List;
import java.util.Arrays;

/**
 * Represents the result of executing a TestCase, or perhaps an unexecuted TestCase. 
 */
public class TestResult {
	private final String result;

	public static final TestResult Pass = new TestResult("Pass");
	public static final TestResult Fail = new TestResult("Fail");
	public static final TestResult Error = new TestResult("Error");
	public static final TestResult Ignore = new TestResult("Ignore");
	public static final TestResult Skip = new TestResult("Skip");
	public static final TestResult Unexecuted = new TestResult("Unexecuted");


	TestResult(String result) {
		this.result = result;
	}


	public String toString() {
		return result;
	}

	public static List<TestResult> getResultList() {
		return Arrays.asList(Pass, Fail, Error, Ignore, Skip, Unexecuted);
	}

	/**
	 * Create a TestResult for a custom result. This probably isn't what you want -- you should favor the static
	 * TestResult members on this class. This method is here for the rare case when a Cuanto server has non-default
	 * TestResults that aren't named the same as the static members on this class. If you specify a TestResult
	 * that doesn't exist, you will get an error when you attempt to create a TestOutcome with that result.
	 * Consider yourself warned.
	 * @param result The custom result name -- should match a TestResult on the server.
	 * @return A TestResult corresponding to the specified name.
	 */
	public static TestResult forResult(String result) {
		return new TestResult(result);
	}


	/**
	 * Create a TestResult for this result name. This only works for the static values defined on this TestResult object,
	 * not custom TestResults -- see forResult() if you wish to use custom TestResults.
	 *
	 * @param result The string value of a TestResult. Case-insensitive.
	 * @throws IllegalArgumentException if the result is not one of the defaults available.
	 * @return The TestResult corresponding to the specified string.
	 */
	public static TestResult valueOf(String result) throws IllegalArgumentException {

		for (TestResult testResult : getResultList() )
		{
			if (testResult.toString().equalsIgnoreCase(result)) {
				return testResult;
			}
		}
		throw new IllegalArgumentException("Unrecognized TestResult: " + result);
	}
}