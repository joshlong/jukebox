package com.joshlong.jukebox2.bus.musicbrainz.replication;

import com.joshlong.jukebox2.batch.musicbrainz.replication.ReplicationProcessor;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.integration.core.Message;

import java.io.File;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class InboundReplicationUpdateProcessor {
    @Autowired
    private ReplicationProcessor replicationProcessor;

    private String replicationBundleFromFile(File f) {
        return null;
    }

    public void handleNewReplicationUpdate(Message<File> msg)
        throws Throwable {
        this.replicationProcessor.processReplicationBundle(replicationBundleFromFile(msg.getPayload()));
    }
}
