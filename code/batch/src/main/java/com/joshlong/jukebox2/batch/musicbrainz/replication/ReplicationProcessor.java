package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Essentially, this class will handle: - manipulating and loading the contents of a directory - processing the contents
 * of that directory
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ReplicationProcessor implements InitializingBean {
    private ReplicationUtils replicationUtils;
    private JdbcTemplate jdbcTemplate;
    private Job loadPendingJob; 
    private JobLauncher jobLauncher ;
 
    public void processReplicationBundle(String bundleKey) throws Throwable {
        File pendingFile = this.replicationUtils.resolvePending(bundleKey);
        System.out.println( "Reading pending file: "+ pendingFile.getAbsolutePath() ) ;
        Map<String, JobParameter> parameterMap   = new HashMap<String, JobParameter>() ;
        parameterMap.put( "bundleName", new JobParameter(bundleKey));
        parameterMap.put( "pendingFile" ,  new JobParameter(pendingFile.getAbsolutePath() ));
        JobExecution jobExecution = this.jobLauncher.run(  loadPendingJob, new JobParameters(parameterMap));
    }

    public void afterPropertiesSet() throws Exception {
    }

    public static void main(String[] args) throws Throwable {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("musicbrainz-replication.xml");
        ReplicationProcessor replicationProcessor = applicationContext.getBean(ReplicationProcessor.class);
        replicationProcessor.processReplicationBundle("replication-37827");
    }

    @Required
    public void setLoadPendingJob(final Job loadPendingJob) {
        this.loadPendingJob = loadPendingJob;
    }

    @Required
    public void setReplicationUtils(ReplicationUtils replicationUtils) {
        this.replicationUtils = replicationUtils;
    }

    @Required
    public void setJobLauncher(final JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    @Required
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}

