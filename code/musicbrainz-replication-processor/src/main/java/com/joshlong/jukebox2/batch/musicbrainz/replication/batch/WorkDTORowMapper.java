package com.joshlong.jukebox2.batch.musicbrainz.replication.batch;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class WorkDTORowMapper implements RowMapper<WorkDTO> {
    public WorkDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new WorkDTO(rs.getLong("seq_id"), rs.getString("table_name"), rs.getString("op"), rs.getString("data"),
                           rs.getBoolean("is_key"));
    }
}
