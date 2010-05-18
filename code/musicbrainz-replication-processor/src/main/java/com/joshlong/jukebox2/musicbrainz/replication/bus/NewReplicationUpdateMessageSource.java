package com.joshlong.jukebox2.musicbrainz.replication.bus;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.integration.message.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class NewReplicationUpdateMessageSource implements MessageSource<File> {

    private String replicationUrlMask = "%s/replication-%s.tar.bz2";
    private String replicationBaseUri;
    private String findNextReplicationSequenceSQL;
    private Resource downloadDirectory;

    @Required
    public void setDownloadDirectory(final Resource downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }

    @Required
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private JdbcTemplate jdbcTemplate;

    private File sync() throws Throwable {
        // todo select current repl schema from replication_control
        // todo  check remote file system for next iteration
        // todo if next iteraton exists, download it

        int currentReplSeq = this.jdbcTemplate.queryForInt(this.findNextReplicationSequenceSQL);
        int newSeq = currentReplSeq += 1;

        String newUrl = String.format(this.replicationUrlMask, replicationBaseUri, Integer.toString(newSeq));

        System.out.println("url: " + newUrl); // todo download the bastich

        return null;

    }

    public Message<File> receive() {
        try {
            File payload = sync();
            if(payload == null)
            return null ;

            return MessageBuilder.withPayload( payload).build();
        }
        catch (Throwable throwable) {
            throw new RuntimeException(
                    "error occurred in downloading the latest replication update", throwable);
        }
    }

    @Required
    public void setReplicationBaseUri(final String replicationBaseUri) {
        this.replicationBaseUri = replicationBaseUri;
    }

    @Required
    public void setFindNextReplicationSequenceSQL(final String findNextReplicationSequenceSQL) {
        this.findNextReplicationSequenceSQL = findNextReplicationSequenceSQL;
    }

}
