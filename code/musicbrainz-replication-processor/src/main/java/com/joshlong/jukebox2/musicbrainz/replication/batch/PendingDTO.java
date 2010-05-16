package com.joshlong.jukebox2.musicbrainz.replication.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PendingDTO {
    private int seqId;
    private int XID;
    private String tableName;
    private String op;

    public PendingDTO() {
    }

    public PendingDTO(int seqId, int XID, String tableName, String op) {
        this.seqId = seqId;
        this.XID = XID;
        this.tableName = tableName;
        this.op = op;
    }

    public String getOp() {
        return op;
    }

    public void setOp(final String op) {
        this.op = op;
    }

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(final int seqId) {
        this.seqId = seqId;
    }

    public int getXID() {
        return XID;
    }

    public void setXID(final int XID) {
        this.XID = XID;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
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
}
