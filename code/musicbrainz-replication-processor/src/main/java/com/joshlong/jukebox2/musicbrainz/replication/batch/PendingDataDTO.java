package com.joshlong.jukebox2.musicbrainz.replication.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PendingDataDTO {
    private int seqId;
    private boolean key;
    private String data;

    public PendingDataDTO() {
    }

    public PendingDataDTO(final int seqId, final boolean key, final String data) {
        this.seqId = seqId;
        this.key = key;
        this.data = data;
    }

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(final int seqId) {
        this.seqId = seqId;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(final boolean key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
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
