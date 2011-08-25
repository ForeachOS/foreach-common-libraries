package com.foreach.web.logging;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * <p>
 * The MethodExecutionTimeLogger class contains utility method which can be used to log the method execution time.
 * To use, inherit this class in your actual AspectJ class where you define advice method and point-cut methods.
 * The advice method of your AspectJ class should use/call method MethodExecutionTimeLogger.logExecutionTime for logging
 * the method execution time in log files.
 * </p>
 * <p/>
 * <p>To use this class, perform following steps
 * <ul>
 * <li>Inherit this class in your actual AspectJ class where you define advice and point-cut methods</li>
 * <li>Define point-cut methods for which you wants method execution time to be logged</li>
 * <li>In your advice method call the method MethodExecutionTimeLogger.logExecutionTime. This will log the method execution time</li>
 * <li>All methods with duration of > 75 milliseconds will be logged by default.
 * You can change this minimum duration time by method MethodExecutionTimeLogger.setMinimumDuration</li>
 * </ul>
 * </p>
 *
 * @version 1.0
 */
public class MethodExecutionTimeLogger
{

    private Logger logger = Logger.getLogger(MethodExecutionTimeLogger.class);

    private long minimumDuration = 75;

    public MethodExecutionTimeLogger()
    {
    }

    /**
     * Specify your own logger where you want the method duration time to be logged
     *
     * @param log
     */
    public final void setLogger(Logger log)
    {
        this.logger = log;
    }

    /**
     * Specify the minimum duration time of methods in milliseconds. Default is 75 milliseconds.
     * Methods whose processing time is greater then specified minimum duration time will be logged.
     *
     * @param time minimum duration time of methods in milliseconds (exclusive)
     */
    public final void setMinimumDuration(long time)
    {
        this.minimumDuration = time;
    }

    /**
     * This method will log the method duration time if the execution time exceeds specified minimum duration time.
     * Log format - class-name.method-name  duration-time(in milliseconds)
     *
     * @param point ProceedingJoinPoint reference from your advice method
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("all")
    public final Object logExecutionTime(ProceedingJoinPoint point) throws Throwable
    {
        long startTime = System.currentTimeMillis();
        Object result = point.proceed();
        long duration = System.currentTimeMillis() - startTime;

        if (duration > minimumDuration)
        {
            String method = "";
            try
            {
                method = point.getSignature().getDeclaringType().getName() + "." + point.getSignature().getName();
                logger.info("" + method + "\t" + duration);
            } catch (Exception e)
            {
                logger.warn("unable-to-get-method-signature\n" + duration);
            }

        }
        return result;
    }
}
