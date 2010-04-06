package com.joshlong.jukebox2.batch.musicbrainz.replication;

import java.io.File;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ReplicationUtils {
    private File replicatonDirectory;
    private String intermediaryFolder = "mbdump";
    private String pending = "Pending";
    private String pendingData = "PendingData";
    private String replSequence = "REPLICATION_SEQUENCE";
    private String schemaSequence = "SCHEMA_SEQUENCE";
    private String timestamp = "TIMESTAMP";

    public ReplicationUtils(File f) {
        this.replicatonDirectory = f;
    }

    public ReplicationContext buildReplicationContextForBundle(String bundleName) {
        ReplicationContext replicationContext = new ReplicationContext(this, bundleName);

        return replicationContext;
    }

    public File resolveSchemaSequence(String bn) {
        return new File(this.resolveReplicationBundle(bn), schemaSequence);
    }

    public File resolveTimestamp(String bundleName) {
        return new File(this.resolveReplicationBundle(bundleName), timestamp);
    }

    public File resolveReplicationSequence(String bundleName) {
        return new File(this.resolveReplicationBundle(bundleName), replSequence);
    }

    public File resolveMbDumpFolder(String bundleName) {
        return new File(this.resolveReplicationBundle(bundleName), intermediaryFolder);
    }

    public File resolvePendingData(String bundleName) {
        return new File(resolveMbDumpFolder(bundleName), pendingData);
    }

    public File resolvePending(String bundleName) {
        return new File(resolveMbDumpFolder(bundleName), pending);
    }

    public File resolveReplicationBundle(String bundleName) {
        return new File(this.replicatonDirectory, bundleName);
    }
}
