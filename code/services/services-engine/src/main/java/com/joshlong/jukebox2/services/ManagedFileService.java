package com.joshlong.jukebox2.services;

import com.joshlong.jukebox2.model.ManagedFile;
import com.joshlong.jukebox2.model.StorageNode;
import com.joshlong.jukebox2.services.impl.util.ManagedFileMountPrefix;

import java.util.Collection;


/**
 * This provides all the facilities required to interact with the file system
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public interface ManagedFileService {
    String getFilePathExtension(String p);

    String getWebPathForManagedFile(long managedFileId, String host);

    String getPathForManagedFile(long mfId);

    StorageNode createStorageNode(String mountPrefix, double totalByteCapacity);

    void deleteManagedFile(long managedFileId);

    String getFtpUploadPathForUserCredentials(long userCredentialsId);

    ManagedFile getManagedFileById(long id);

    void setManagedFileReadyYN(final long mfid, final boolean yesOrNo);

    void setStorageNodeReady(long storageNodeId, boolean readyYesOrNo);

    void setManagedFilePriority(long managedFileId, int priority);

    boolean isManagedFileReady(long managedFileId);

    ManagedFile createManagedFile(String originalFileName, String extension, ManagedFileMountPrefix prefix, double byteSize, int priority);

    Collection<StorageNode> getAvailableStorageNodes();

    StorageNode getStorageNodeByMountPrefix(String mountPrefix);

    StorageNode getStorageNodeById(long storageNodeId);

    boolean moveManagedFileToStorageNode(long managedFileId, long storageNodeId);

    boolean addManagedFileToStorageNode(long managedFileId, long storageNodeId);

    Collection<StorageNode> getStorageNodesByPriority(int priority);

    StorageNode getStorageNodeForManagedFile(long managedFileId);
}
