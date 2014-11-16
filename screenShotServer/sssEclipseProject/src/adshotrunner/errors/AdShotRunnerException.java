package adshotrunner.errors;

/**
 * Base runtime error for AdShotRunner system. All other system specific exceptions should
 * inherit from here.
 */
public class AdShotRunnerException extends RuntimeException {

	public AdShotRunnerException(String message) {super(message);}
	public AdShotRunnerException(Throwable cause) {super(cause);}
	public AdShotRunnerException(String message, Throwable cause) {super(message, cause);}

}
