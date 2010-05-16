package com.joshlong.jukebox2.batch.musicbrainz.replication.batch;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PendingFieldSetMapper implements FieldSetMapper<PendingDTO> {
    public PendingDTO mapFieldSet(final FieldSet fieldSet) {
        if (fieldSet == null) {
            return null;
        }

        PendingDTO pendingDTO = new PendingDTO();
        pendingDTO.setOp(fieldSet.readString("Op"));
        pendingDTO.setSeqId(fieldSet.readInt("SeqId"));
        pendingDTO.setXID(fieldSet.readInt("XID"));
        pendingDTO.setTableName(fieldSet.readString("TableName"));

        return pendingDTO;
    }
}
