package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PendingWorkDTORowMapper implements RowMapper<PendingWorkDTO> {
    public PendingWorkDTO mapRow(final ResultSet rs, final int rowNum)
        throws SQLException {
        return new PendingWorkDTO(rs.getInt("xid"), rs.getInt("max"));
    }
}
