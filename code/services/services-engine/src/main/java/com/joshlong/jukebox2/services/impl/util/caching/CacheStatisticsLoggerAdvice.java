package com.joshlong.jukebox2.services.impl.util.caching;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.SessionFactory;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * This should simply display statistical information about the cache after each run.
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a> 
 */
public class CacheStatisticsLoggerAdvice implements InitializingBean {
    private static final Logger logger = Logger.getLogger(CacheStatisticsLoggerAdvice.class);
    private boolean enabled;
    private SessionFactory sessionFactory;
    private HibernateTemplate hibernateTemplate;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void afterPropertiesSet() throws Exception {
        this.hibernateTemplate = new HibernateTemplate(this.sessionFactory);
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Inspects the method invocation parameters and finds any arguments of type
     * long and then iterates through all entity classes and runs invalidate on
     * them
     *
     * @param methodInvocation     ProceedingJoinPoint the context for the current method as it is being invoked
     * @return          an object (arbitrary)
     * @throws Throwable if something (<em>anything</em>) should go wrong..
     */
    public Object logStatisticsAboutCache(ProceedingJoinPoint methodInvocation)
        throws Throwable {
        // Method method = methodInvocation.getMethod();
        Object result = null;

        try {
            // Object[] args = methodInvocation.getArgs();
            result = methodInvocation.proceed();

            if (!enabled) {
                return result;
            }

            // now we read cache statistics
            for (String regionName : sessionFactory.getStatistics().getSecondLevelCacheRegionNames()) {
                SecondLevelCacheStatistics stats = sessionFactory.getStatistics().getSecondLevelCacheStatistics(regionName);

                if (stats.getSizeInMemory() > 0) {
                    logger.debug(String.format("regionName:%s, methodToString:%s, entries: %s", regionName, methodInvocation.toLongString(), stats.toString()));
                }
            }
        } catch (Throwable t) {
            logger.debug(t);
        }

        return result;
    }
}
