package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

/**
 *
 * Essentially, this class will handle:
 *      - manipulating and loading the contents of a directory
 *      - processing the contents of that directory
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ReplicationProcessor implements InitializingBean {


    private File replicatonDirectory ;
    private JdbcTemplate jdbcTemplate;
    


    public ReplicationProcessor(){ }


    public void afterPropertiesSet() throws Exception {

    }
}
