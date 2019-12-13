package com.netcrest.pado.tools.hazelcast;

/**
 * Thrown if the worng type-casting occurs.
 * @author dpark
 *
 */
public class WrongTypeException extends RuntimeException {
	
	/**
	 * {@inheritDoc}
	 */
	public WrongTypeException() {
        super();
    }
	
	/**
	 * {@inheritDoc}
	 */
	public WrongTypeException(String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public WrongTypeException(Throwable cause) {
		super(cause);
	}

}
