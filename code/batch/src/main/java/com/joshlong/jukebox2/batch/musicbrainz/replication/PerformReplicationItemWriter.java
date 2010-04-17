package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.log4j.Logger;

import org.springframework.batch.item.ItemWriter;

import org.springframework.beans.factory.annotation.Required;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.*;


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

    void applyDelete(final ResultSet rs, WorkDTO workDTO)
        throws Throwable {
        Map<String, String> kvs = this.dataTuple(workDTO);

        WhereClause wc = new WhereClause(kvs);
        String delSQL = String.format("DELETE FROM \"%s\" WHERE %s   ", workDTO.getTableName(), wc.getWhereClauseSQL());
        final List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList(wc.getArguments()));

        this.jdbcTemplate.update(delSQL,
            new PreparedStatementSetter() {
                public void setValues(final PreparedStatement ps)
                    throws SQLException {
                    int ctr = 0;

                    for (String v : args) {
                        ps.setString(ctr, v);
                        ctr += 1;
                    }
                }
            });
        System.out.println(delSQL);
    }

    void applyUpdate(final ResultSet rs, WorkDTO workDTO)
        throws Throwable {
        // the id to which the update applies
        final WhereClause whereClause = new WhereClause(this.dataTuple(workDTO));

        if (!rs.next()) {
            throw new RuntimeException("AAHHHH!! Something's *very* wrong here!");
        }

        WorkDTO nextRow = this.workDTORowMapper.mapRow(rs, 0);
        final WhereClause whereClause2 = new WhereClause(this.dataTuple(nextRow));

        String upSql = String.format("UPDATE \"%s\" SET %s WHERE %s ", workDTO.getTableName(), whereClause2.getSetClauseSQL(), whereClause.getWhereClauseSQL());
        System.out.println(StringUtils.join(whereClause2.getArguments(), ","));
        System.out.println(StringUtils.join(whereClause.getArguments(), ","));

        System.out.println(upSql);

        final List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList(whereClause2.getArguments()));
        args.addAll(Arrays.asList(whereClause.getArguments()));

        this.jdbcTemplate.update(upSql,
            new PreparedStatementSetter() {
                public void setValues(final PreparedStatement ps)
                    throws SQLException {
                    int ctr = 0;

                    for (String v : args) {
                        ps.setString(ctr, v);
                        ctr += 1;
                    }
                }
            });
    }

    void applyInsert(final ResultSet rs, WorkDTO workDTO)
        throws Throwable {
        Map<String, String> kvs = this.dataTuple(workDTO);
        WhereClause whereClause = new WhereClause(kvs);
        String insertSQL = String.format("INSERT INTO \"%s\"( %s ) VALUES( %s )", workDTO.getTableName(), whereClause.getColumnNamesClauseSQL(), whereClause.getArgumentsClauseSQL());
        final List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList(whereClause.getArguments()));

        this.jdbcTemplate.update(insertSQL,
            new PreparedStatementSetter() {
                public void setValues(final PreparedStatement ps)
                    throws SQLException {
                    int ctr = 0;

                    for (String v : args) {
                        ps.setString(ctr, v);
                        ctr += 1;
                    }
                }
            });

        System.out.println(insertSQL);
    }

    Map<String, String> dataTuple(WorkDTO dto) throws Throwable {
        return this.dataTupleUnpacker.unpack(dto.getData());
    }

    // todo - you have a parser that can unpack the data in the {@link WorkDTO}'s data field
    // todo now u should be able to create a where clause and implement the prepare_delete,prepare_insert,prepare_update from dbmirror.pm
    void processPendingWorkDTO(PendingWorkDTO pendingWorkDTO)
        throws Exception {
        //  List<WorkDTO> rowsOfWorkDTOs = new ArrayList <WorkDTO>() ;
      //  try{
            this.jdbcTemplate.query(this.selectOperationsForTransactionSql,

            new RowCallbackHandler() {
                public void processRow(final ResultSet rs)
                    throws SQLException {
                    WorkDTO workDTO = workDTORowMapper.mapRow(rs, 0);
                    System.out.println(StringUtils.repeat("=", 100));

                    // System.out.println(workDTO);
                    try {
                        if (workDTO.isInsert()) {
                            applyInsert(rs, workDTO);
                        }

                        if (workDTO.isDelete()) {
                            applyDelete(rs, workDTO);
                        }

                        if (workDTO.isUpdate()) {
                            applyUpdate(rs, workDTO);
                        }
                    } catch (Throwable e) {
                        logger.debug("Exception occurred: " + ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }, pendingWorkDTO.getXid());

            this.jdbcTemplate.execute("DELETE FROM \"pending\" WHERE \"xid\"=" +
                                      "'" + pendingWorkDTO.getXid() +"'");

     ///   } finally{}
    }

    public void write(final List<?extends PendingWorkDTO> pendingWorkDTOs)
        throws Exception {
        for (PendingWorkDTO pendingWorkDTO : pendingWorkDTOs) {
            processPendingWorkDTO(pendingWorkDTO);
        }
    }

    public static void main(String[] a) throws Throwable {
        Map<String, String> kvs = new HashMap<String, String>();
        kvs.put("age", "222");
        kvs.put("firstName", "josh");

        WhereClause whereClause = new WhereClause(kvs);

        System.out.println(whereClause);
        System.out.println(StringUtils.join(whereClause.getArguments(), ","));
    }

    /**
     *
     *
     */
    static class WhereClause {
        private String whereClauseSQL;
        private String setClauseSQL;
        private List<String> arguments;
        private List<String> columnNames;
        private Map<String, String> kvs;

        public WhereClause(Map<String, String> kvs) {
            this.kvs = kvs;
            this.arguments = new ArrayList<String>();
            this.columnNames = new ArrayList<String>();

            build();
        }

        void build() {
            if ((kvs == null) || (kvs.size() == 0)) {
                return;
            }

            List<String> keys = new ArrayList<String>(kvs.keySet());
            List<String> conditions = new ArrayList<String>();

            for (String k : keys) {
                String v = kvs.get(k);

                if (!StringUtils.isEmpty(v)) {
                    conditions.add(String.format("\"%s\" = ?", k));
                    arguments.add(v);
                } else {
                    conditions.add(String.format("\"%s\" IS NULL", k));
                }

                columnNames.add(k);
            }

            this.setClauseSQL = StringUtils.join(conditions.iterator(), ",");

            this.whereClauseSQL = StringUtils.join(conditions.iterator(), " AND ");
        }

        public String getColumnNamesClauseSQL() {
            return StringUtils.join(this.getColumnNames(), ",");
        }

        public String getSetClauseSQL() {
            return setClauseSQL;
        }

        public String getArgumentsClauseSQL() {
            String[] cnt = new String[this.arguments.size()];

            for (int i = 0; i < cnt.length; i++) {
                cnt[i] = "?";
            }

            return StringUtils.join(cnt, ",");
        }

        public String getWhereClauseSQL() {
            return whereClauseSQL;
        }

        @Override
        public String toString() {
            return this.whereClauseSQL;
        }

        public String[] getColumnNames() {
            return this.columnNames.toArray(new String[columnNames.size()]);
        }

        public String[] getArguments() {
            return this.arguments.toArray(new String[arguments.size()]);
        }
    }
}
