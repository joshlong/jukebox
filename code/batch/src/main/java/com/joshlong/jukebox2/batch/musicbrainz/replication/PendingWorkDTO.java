package com.joshlong.jukebox2.batch.musicbrainz.replication;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This is specifically to give us a place to hold an XID and a count
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PendingWorkDTO {
    private int xid;
    private int max;

    public PendingWorkDTO(final int xid, final int max) {
        this.xid = xid;
        this.max = max;
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

    public int getXid() {
        return xid;
    }

    public void setXid(final int xid) {
        this.xid = xid;
    }

    public int getMax() {
        return max;
    }

    public void setMax(final int max) {
        this.max = max;
    }
}
