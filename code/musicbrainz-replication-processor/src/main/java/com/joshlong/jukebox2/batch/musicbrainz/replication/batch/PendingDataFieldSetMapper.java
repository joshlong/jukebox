package com.joshlong.jukebox2.batch.musicbrainz.replication.batch;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PendingDataFieldSetMapper implements FieldSetMapper<PendingDataDTO> {
    public PendingDataDTO mapFieldSet(final FieldSet fieldSet) {
        if (fieldSet == null) {
            return null;
        }

        PendingDataDTO pendingDTO = new PendingDataDTO();
        pendingDTO.setData(fieldSet.readString("Data"));
        pendingDTO.setSeqId(fieldSet.readInt("SeqId"));
        pendingDTO.setKey(fieldSet.readBoolean("IsKey"));

        return pendingDTO;
    }
}
