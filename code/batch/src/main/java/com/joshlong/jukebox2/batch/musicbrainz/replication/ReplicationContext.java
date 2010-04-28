package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ReplicationContext {
    private static final Logger logger = Logger.getLogger(ReplicationContext.class);
    private String bundleName;
    private ReplicationUtils replicationUtils;
    private File pending;
    private File pendingData;
    private long replicationSequence;
    private long schemaSequence;
    private String dateFormatString;
    private String timestamp;
    private Date dateTimestamp;
    private DateFormat dateFormat;

    public ReplicationContext(ReplicationUtils replicationUtils, String bundleName) {
        try {
            this.setup(replicationUtils, bundleName);
        }
        catch (Throwable throwable) {
            System.out.println(ExceptionUtils.getFullStackTrace(throwable));
        }
    }

    private String slurp(File f) throws Throwable {
        Reader fileReader = new FileReader(f);
        String contentsOfFile = StringUtils.defaultString(IOUtils.toString(fileReader)).trim();
        IOUtils.closeQuietly(fileReader);
        return contentsOfFile;
    }

    private void setup(ReplicationUtils replicationUtils, String bundleName)
            throws Throwable {
        this.dateFormatString = "yyyy-MM-dd kk:mm:ss.S";
        this.dateFormat = new SimpleDateFormat(this.dateFormatString);

        this.replicationUtils = replicationUtils;
        this.bundleName = bundleName;

        this.replicationSequence = Long.parseLong(slurp(this.replicationUtils.resolveReplicationSequence(bundleName)));
        this.schemaSequence = Long.parseLong(slurp(this.replicationUtils.resolveSchemaSequence(bundleName)));
        this.timestamp = slurp(this.replicationUtils.resolveTimestamp(bundleName));
        this.dateTimestamp = synchronizedDateParse(timestamp);
        this.pending = replicationUtils.resolvePending(bundleName);
        this.pendingData = replicationUtils.resolvePendingData(bundleName);
    }

    private synchronized Date synchronizedDateParse(String inputDate)
            throws Throwable {
        return this.dateFormat.parse(inputDate);
    }

    public File getPending() {
        return pending;
    }

    public File getPendingData() {
        return pendingData;
    }

    public long getReplicationSequence() {
        return replicationSequence;
    }

    public long getSchemaSequence() {
        return schemaSequence;
    }

    public Date getTimestamp() {
        return this.dateTimestamp;
    }
}
