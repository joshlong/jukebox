package com.joshlong.jukebox2.batch.musicbrainz.replication;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**     todo remove 
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class PendingDTOItemPreparedStatementSetter implements ItemPreparedStatementSetter<PendingDTO>
{
    public void setValues(final PendingDTO pendingDTO, final PreparedStatement preparedStatement) throws SQLException {
       /* :SeqId, :TableName, :Op, :XID*/




    }
}
