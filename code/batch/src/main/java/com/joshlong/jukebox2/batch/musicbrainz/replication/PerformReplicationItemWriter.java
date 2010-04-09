package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.log4j.Logger;

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
    private static final Logger logger = Logger.getLogger(PerformReplicationItemWriter.class);
    private String selectOperationsForTransactionSql;
    private JdbcTemplate jdbcTemplate;
    private RowMapper<WorkDTO> workDTORowMapper;
    private DataTupleUnpacker dataTupleUnpacker;

    @Required
    public void setDataTupleUnpacker(final DataTupleUnpacker dataTupleUnpacker) {
        this.dataTupleUnpacker = dataTupleUnpacker;
    }

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

    void applyDelete(WorkDTO workDTO) throws Exception {
    }

    void applyUpdate(WorkDTO workDTO) throws Exception {
    }

    void applyInsert(WorkDTO workDTO) throws Exception {
    }

   // todo - you have a parser that can unpack the data in the {@link WorkDTO}'s data field
     // todo now u should be able to create a where clause and implement the prepare_delete,prepare_insert,prepare_update from dbmirror.pm

    void processPendingWorkDTO(PendingWorkDTO pendingWorkDTO)
        throws Exception {
        this.jdbcTemplate.query(this.selectOperationsForTransactionSql,
            new RowCallbackHandler() {
                public void processRow(final ResultSet rs)
                    throws SQLException {
                    WorkDTO workDTO = workDTORowMapper.mapRow(rs, 0);
                    System.out.println(StringUtils.repeat("=", 100));
                    System.out.println(workDTO);

                    try {
                        if (workDTO.isInsert()) {
                            applyInsert(workDTO);
                        }

                        if (workDTO.isDelete()) {
                            applyDelete(workDTO);
                        }

                        if (workDTO.isUpdate()) {
                            applyUpdate(workDTO);
                        }
                    } catch (Exception e) {
                        logger.debug("Exception occurred: " + ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }, pendingWorkDTO.getXid());
    }

    public void write(final List<?extends PendingWorkDTO> pendingWorkDTOs)
        throws Exception {
        for (PendingWorkDTO pendingWorkDTO : pendingWorkDTOs) {
            processPendingWorkDTO(pendingWorkDTO);
        }
    }
}
