package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class WorkDTO {
    private long sequenceId;
    private String tableName;
    private String op;
    private String data;
    private boolean key;

    public WorkDTO() {
    }

    public WorkDTO(final long sequenceId,
                   final String tableName,
                   final String op,
                   final String data,
                   final boolean key) {
        this.sequenceId = sequenceId;
        this.tableName = tableName;
        this.op = op;
        this.data = data;
        this.key = key;
    }

    private boolean is(char c) {
        return StringUtils.defaultString(this.op).trim().toLowerCase().equals("" + c);
    }

    public boolean isInsert() {
        return is('i');
    }

    public boolean isDelete() {
        return is('d');
    }

    public boolean isUpdate() {
        return is('u');
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(final long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public String getOp() {
        return op;
    }

    public void setOp(final String op) {
        this.op = op;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(final boolean key) {
        this.key = key;
    }
}
