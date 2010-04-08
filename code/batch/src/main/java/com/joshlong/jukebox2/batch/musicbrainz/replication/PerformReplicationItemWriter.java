package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PerformReplicationItemWriter implements ItemWriter<PendingWorkDTO> {
    private String selectOperationsForTransactionSql;
    private JdbcTemplate jdbcTemplate;
    private RowMapper<WorkDTO> workDTORowMapper;

    @Required
    public void setWorkDTORowMapper(final RowMapper<WorkDTO> workDTORowMapper) {
        this.workDTORowMapper = workDTORowMapper;
    }

    @Required
    public void setSelectOperationsForTransactionSql(final String selectOperationsForTransactionSql) {
        this.selectOperationsForTransactionSql = selectOperationsForTransactionSql;
    }

    @Required
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void applyInsert( WorkDTO workDTO) throws Exception{

    }

    void processPendingWorkDTO(PendingWorkDTO pendingWorkDTO)
        throws Exception {
        this.jdbcTemplate.query(this.selectOperationsForTransactionSql,new RowCallbackHandler(){
            public void processRow(final ResultSet rs) throws SQLException {



                WorkDTO workDTO = workDTORowMapper.mapRow( rs,0);

                 System.out.println(workDTO ) ;

            }
        },pendingWorkDTO.getXid());
    }

    public void write(final List<?extends PendingWorkDTO> pendingWorkDTOs)
        throws Exception {
        for (PendingWorkDTO pendingWorkDTO : pendingWorkDTOs) {
            processPendingWorkDTO(pendingWorkDTO);
        }
    }
}
