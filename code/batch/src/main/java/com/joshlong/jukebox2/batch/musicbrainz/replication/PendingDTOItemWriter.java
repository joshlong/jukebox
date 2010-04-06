package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.springframework.batch.item.ItemWriter;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PendingDTOItemWriter implements ItemWriter<PendingDTO> {
    private JdbcTemplate jdbcTemplate;
    private String sql = "INSERT INTO public.pending(seq_id,table_name,op,xid) VALUES( ?,?,?,?)";

    @Required
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void write(final List<?extends PendingDTO> pendingDTOs)
        throws Exception {
        this.jdbcTemplate.batchUpdate(sql,
            new BatchPreparedStatementSetter() {
                public int getBatchSize() {
                    return pendingDTOs.size();
                }

                public void setValues(final PreparedStatement ps, final int i)
                    throws SQLException {
                    PendingDTO pendingDTO = pendingDTOs.get(i);
                    ps.setInt(1, pendingDTO.getSeqId());
                    ps.setString(2, pendingDTO.getTableName());
                    ps.setString(3, pendingDTO.getOp());
                    ps.setInt(4, pendingDTO.getXID());
                }
            });
    }
}
