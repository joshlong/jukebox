package com.joshlong.jukebox2.musicbrainz.replication.bus;

import com.joshlong.jukebox2.musicbrainz.replication.batch.ReplicationProcessor;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.integration.core.Message;

import java.io.File;


/**
 *
 * Reacts to new replication bundles arriving. 
 *
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
