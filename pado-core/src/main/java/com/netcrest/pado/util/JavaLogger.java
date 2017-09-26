package com.netcrest.pado.util;

import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.netcrest.pado.log.ILogger;

/**
 * JavaLogger uses the Java Logger facility to log messages. To invoke this implementation, set the system property as follows:
 * <pre>
 * pado.class.logger=com.netcrest.pado.util.JavaLogger
 * </pre>
 * <b>LIMITATIONS:</b>
 * <p>
 * This implementation uses the same name for all loggers.
 * @author dpark
 *
 */
public class JavaLogger implements ILogger
{
	private final static JavaLogger logger = new JavaLogger();
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS zzz");

	public static ILogger getLogger()
	{
		return logger;
	}

	@Override
	public void warning(String message, Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.WARNING, message, th);
	}

	@Override
	public void warning(String message)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.WARNING, message);
	}

	@Override
	public void warning(Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.WARNING, null, th);
	}

	@Override
	public boolean isWarningEnabled()
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		return logger.isLoggable(Level.WARNING);
	}

	@Override
	public void error(String message, Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.SEVERE, message, th);
	}

	@Override
	public void error(String message)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.SEVERE, message);
	}

	@Override
	public void error(Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.SEVERE, null, th);
	}

	@Override
	public boolean isErrorEnabled()
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		return logger.isLoggable(Level.SEVERE);
	}

	@Override
	public void severe(String message, Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.SEVERE, message, th);
	}

	@Override
	public void severe(Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.SEVERE, null, th);
	}

	@Override
	public void severe(String message)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.SEVERE, message);
	}

	@Override
	public boolean isSevereEnabled()
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		return logger.isLoggable(Level.SEVERE);
	}

	@Override
	public void info(String message, Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.INFO, message, th);
	}

	@Override
	public void info(String message)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.INFO, message);
	}

	@Override
	public void info(Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.INFO, null, th);
	}

	@Override
	public boolean isInfoEnabled()
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		return logger.isLoggable(Level.INFO);
	}

	@Override
	public void config(String message, Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.CONFIG, message, th);
	}

	@Override
	public void config(String message)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.CONFIG, message);
	}

	@Override
	public void config(Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.CONFIG, null, th);
	}

	@Override
	public boolean isConfigEnabled()
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		return logger.isLoggable(Level.CONFIG);
	}

	@Override
	public void fine(String message, Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.FINE, message, th);
	}

	@Override
	public void fine(String message)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.FINE, message);
	}

	@Override
	public void fine(Throwable th)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.FINE, null, th);
	}

	@Override
	public boolean isFineEnabled()
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		return logger.isLoggable(Level.FINE);
	}

	@Override
	public void log(String message)
	{
		Logger logger = Logger.getLogger(JavaLogger.class.getSimpleName());
		logger.log(Level.INFO, message);
	}

}
