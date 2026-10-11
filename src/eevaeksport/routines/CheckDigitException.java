/*
 * Created on 05.06.2008
 */
package eevaeksport.routines;

public class CheckDigitException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Construct an Exception with no message.
     */
    public CheckDigitException() {
    }

    /**
     * Construct an Exception with a message.
     *
     * @param msg The error message.
     */
    public CheckDigitException(String msg) {
        super(msg);
    }

    /**
     * Construct an Exception with a message and
     * the underlying cause.
     *
     * @param msg The error message.
     * @param cause The underlying cause of the error
     */
    public CheckDigitException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
