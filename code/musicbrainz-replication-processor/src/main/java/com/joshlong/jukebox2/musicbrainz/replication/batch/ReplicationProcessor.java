package com.joshlong.jukebox2.musicbrainz.replication.batch;

import com.joshlong.jukebox2.musicbrainz.replication.ReplicationContext;
import com.joshlong.jukebox2.musicbrainz.replication.ReplicationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Essentially, this class will handle: - manipulating and loading the contents of a directory - processing the contents
 * of that directory
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ReplicationProcessor {
    private static final Logger logger = Logger.getLogger(ReplicationProcessor.class);
    private ReplicationUtils replicationUtils;
    private JdbcTemplate jdbcTemplate;
    private Job loadPendingJob;
    private JobLauncher jobLauncher;

    public void processReplicationBundle(String bundleKey)
            throws Throwable {
        // ok lets do some tests before we run: for example, we should be smart enough to block processing if this batch has already been processed
        //
        ReplicationContext replicationContext = this.replicationUtils.buildReplicationContextForBundle(bundleKey);
        final long schemaSeq = replicationContext.getSchemaSequence();
        final long replicationSeq = replicationContext.getReplicationSequence();
        final Date timestamp = replicationContext.getTimestamp();

        int lineWidth = 150;
        String title = String.format("Processing TimeStamp: %s, Schema Sequence: %s, Replication Sequence: %s ...",
                                     timestamp, schemaSeq, replicationSeq);
        System.out.println(StringUtils.repeat("=", lineWidth));
        System.out.println(title);

        String lockSql = "select current_schema_sequence, current_replication_sequence, last_replication_date from replication_control";
        boolean canAndShouldProceed = this.jdbcTemplate.query(lockSql, new ResultSetExtractor<Boolean>() {
            public Boolean extractData(final ResultSet rs)
                    throws SQLException, DataAccessException {

                if (rs.next()) {
                    long currentSchemaSeq = rs.getLong("current_schema_sequence");
                    long currentReplicationSeq = rs.getLong("current_replication_sequence");
                    Date currentTimestamp = rs.getDate("last_replication_date");

                    boolean canProceed = (currentSchemaSeq == (schemaSeq - 1)) && (currentReplicationSeq < replicationSeq) && currentTimestamp.before(
                            timestamp);

                    return canProceed;
                }
                return false;
            }
        });

        if (canAndShouldProceed) {
            File pendingFile = replicationContext.getPending();
            File pendingDataFile = replicationContext.getPendingData();

            System.out.println("Reading pending file: " + pendingFile.getAbsolutePath());

            Map<String, JobParameter> parameterMap = new HashMap<String, JobParameter>();
            parameterMap.put("bundleName", new JobParameter(bundleKey));
            parameterMap.put("pendingDataFile", new JobParameter(pendingDataFile.getAbsolutePath()));
            parameterMap.put("pendingFile", new JobParameter(pendingFile.getAbsolutePath()));
            parameterMap.put("now", new JobParameter(System.currentTimeMillis()));

            // this job wil have 3 steps: 'pending','pendingdata', and updating the schema/sequence/whatver so we have a way of checking for dupes
            JobExecution jobExecution = this.jobLauncher.run(loadPendingJob, new JobParameters(parameterMap));
            System.out.println(String.format("Finished loading data from 'Pending.' The exit status is %s",
                                             jobExecution.getExitStatus().getExitCode()));

            this.jdbcTemplate.update(
                    "UPDATE replication_control SET current_schema_sequence =?, current_replication_sequence =?, last_replication_date=?",
                    schemaSeq, replicationSeq, timestamp);

        } else   {
            System.out.println(String.format(
                    "Did not proceed because the replication bundle %s is older than the currently applied replication bundle",
                    bundleKey));
        }

        System.out.println(StringUtils.repeat("=", lineWidth));
    }

    public static void main(String[] args) throws Throwable {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "musicbrainz-replication.xml");
        applicationContext.start();
        applicationContext.registerShutdownHook();

        ReplicationProcessor replicationProcessor = applicationContext.getBean(ReplicationProcessor.class);
        replicationProcessor.processReplicationBundle("replication-37828");
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
