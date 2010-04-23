package com.joshlong.jukebox2.services.impl;
import com.joshlong.jukebox2.model.ManagedFile;
import com.joshlong.jukebox2.model.StorageNode;
import com.joshlong.jukebox2.services.ManagedFileService;
import com.joshlong.jukebox2.services.impl.util.BaseService;
import com.joshlong.jukebox2.services.impl.util.Bytes;
import com.joshlong.jukebox2.services.impl.util.ManagedFileMountPrefix;
import com.joshlong.jukebox2.services.impl.util.ServiceUtils;
import com.joshlong.jukebox2.services.workflow.WorkflowService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbpm.graph.exe.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * This is code for handling the storage of files on the file system (presumably some sort of SAN mount or something).
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 *
 */
public class ManagedFileServiceImpl extends BaseService implements ManagedFileService {
    private static String MANAGED_FILE_MNT_DIRECTORY = "managedFiles";
    private String assetPathMask = "%010d/%010d.%s";
    private String webMediaFullUrl;
 
    @Autowired
    private HibernateTemplate hibernateTemplate;
    @Autowired
    private ServiceUtils serviceUtils;
    @Autowired
    private WorkflowService workflowService;

    @PostConstruct
    public void setup() {
        for (int i = 0; i < 4; i++) {
            createStorageNode("d00" + i, Bytes.terabytes(1).bytes());
        }
    }

    public String getFilePathExtension(String p) {
        if (!StringUtils.isEmpty(p)) {
            int li = p.lastIndexOf(".");

            if (li != -1) {
                String ext = StringUtils.defaultString(p.substring(li));

                if (ext.startsWith(".")) {
                    ext = ext.substring(1);
                }

                if (ext.indexOf(" ") == -1) {
                    return ext.toLowerCase();
                }
            }
        }

        return null;
    }

    public void setWebMediaFullUrl(String webMediaFullUrl) {
        this.webMediaFullUrl = webMediaFullUrl;
    }

    public String getWebPathForManagedFile(long managedFileId, String host) {
        ManagedFile mf = getManagedFileById(managedFileId);
        long lid = mf.getId();
        long rootFolderId = deriveFolderIdFor(lid);
        StorageNode storageNodeForManagedFile = getStorageNodeForManagedFile(mf.getId());
        String mountPrefix = storageNodeForManagedFile.getMountPrefix();
        String pathString = String.format("/%s/%s/media/" + assetPathMask, mapPrefixToWeb(mf.getMountPrefix()), mountPrefix, rootFolderId, lid, mf.getExtension());

        return String.format("%s%s", StringUtils.defaultString(host), pathString);
    }

    private long deriveFolderIdFor(long mfid) {
        return mfid % 5;
    }

    public String getPathForManagedFile(long mfId) {
        ManagedFile mf = getManagedFileById(mfId);
        long lid = mf.getId();
        long rootFolderId = deriveFolderIdFor(lid);
        StorageNode sn = getStorageNodeForManagedFile(mf.getId());
        String mountPrefix = sn.getMountPrefix();

        return String.format("%s/%s/media/" + assetPathMask, mapPrefixToFileSystem(mf.getMountPrefix()), mountPrefix, rootFolderId, lid, mf.getExtension());
    }

    /*public String getPathForUploadBundleFile(long uploadBundleId, String fileName) {
         String pathForUploadBundled = getPathForUploadBundle(uploadBundleId);
         return new File(pathForUploadBundled, fileName).getAbsolutePath();
     }*/

    /*public String getPathForUploadBundle(long uploadBundleId) {
            UploadBundle ubUploadBundle = uploadService.getUploadBundleById(uploadBundleId);
            UserCredentials userCredentials = ubUploadBundle.getUserCredentials();
            File f = new File(getUserCredentialFtpUploadHomeDirectory(userCredentials.getId()), ubUploadBundle.getName());
            return f.getAbsolutePath();

        }
    */
    public StorageNode createStorageNode(String mountPrefix, double totalByteCapacity) {
        StorageNode existingStorageNode = getStorageNodeByMountPrefix(mountPrefix);

        if (null != existingStorageNode) {
            return existingStorageNode;
        }

        StorageNode newStorageNode = new StorageNode();
        newStorageNode.setBytesUsed(0);
        newStorageNode.setMountPrefix(mountPrefix);
        newStorageNode.setOffline(false);
        newStorageNode.setReady(true);
        newStorageNode.setTotalByteCapacity(totalByteCapacity);
        hibernateTemplate.save(newStorageNode);

        return getStorageNodeById(newStorageNode.getId());
    }

    public void deleteManagedFile(long managedFileId) {
        ManagedFile managedFile = getManagedFileById(managedFileId);
        StorageNode sn = managedFile.getStorageNode();
        managedFile.setStorageNode(null);
        hibernateTemplate.saveOrUpdate(managedFile);

        if (!managedFile.isReady()) {
            hibernateTemplate.delete(managedFile);

            return; // theres no file to delete!
        }

        // update the storage node, as well
        if (managedFile.isReady()) {
            // then deduct
            sn.setBytesUsed(sn.getBytesUsed() - managedFile.getByteSize());
            hibernateTemplate.saveOrUpdate(sn);
        }

        String path = getPathForManagedFile(managedFileId);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("managedFilePath", path);

        ProcessInstance pi = workflowService.createProcessInstance("delete-managed-file", vars);
        workflowService.startProcessInstance(pi.getId());
        hibernateTemplate.delete(managedFile);
    }

    public String getFtpUploadPathForUserCredentials(long userCredentialsId) {
        File baseForUploads = mapPrefixToFileSystem(ManagedFileMountPrefix.UPLOADS.toString().toLowerCase());

        return new File(baseForUploads, userCredentialsId + "").getAbsolutePath();
    }

    private String mapPrefixToWeb(String mPrefix) {
        return String.format("%s/%s", MANAGED_FILE_MNT_DIRECTORY, mPrefix);
    }

    private File mapPrefixToFileSystem(String prefix) {
        File usrHome = new File(SystemUtils.getUserHome(), "jukebox2");
        File cdfs = new File(usrHome, MANAGED_FILE_MNT_DIRECTORY);

        return new File(cdfs, prefix);
    }

    public ManagedFile getManagedFileById(long id) {
        return this.serviceUtils.get(ManagedFile.class, id);
    }

    public void setManagedFileReadyYN(final long mfid, final boolean yesOrNo) {
        hibernateTemplate.execute(new HibernateCallback() {
                public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                    ManagedFile managedFile = getManagedFileById(mfid);
                    boolean readyAlready = managedFile.isReady();
                    managedFile.setReady(yesOrNo);

                    hibernateTemplate.saveOrUpdate(managedFile);

                    StorageNode storageNode = managedFile.getStorageNode();

                    if (storageNode == null) {
                        throw new RuntimeException(String.format("You cant mark a managed file as ready without it " + "being assigned to a storage node! Managed File: %s", managedFile.getId()));
                    }

                    // ie, the bytes are ALREADY there
                    // on the storage node, now account for it in
                    // in the book keeping
                    if (yesOrNo) {
                        storageNode.setBytesUsed(storageNode.getBytesUsed() + managedFile.getByteSize());
                    }

                    // if the managed file was previously marked as ready (and
                    // thus, we would
                    // have allocated space for it), then we deduct that
                    // allotment
                    if (!yesOrNo && readyAlready) {
                        storageNode.setBytesUsed(storageNode.getBytesUsed() + managedFile.getByteSize());
                    }

                    hibernateTemplate.saveOrUpdate(storageNode);

                    return managedFile;
                }
            });
    }

    public void setStorageNodeReady(long storageNodeId, boolean readyYesOrNo) {
        StorageNode storageNode = this.serviceUtils.get(StorageNode.class, storageNodeId);
        storageNode.setReady(readyYesOrNo);
        hibernateTemplate.saveOrUpdate(storageNode);
    }

    public void setManagedFilePriority(long managedFileId, int priority) {
        ManagedFile mf = getManagedFileById(managedFileId);
        mf.setPriority(priority);
        hibernateTemplate.saveOrUpdate(mf);
    }

    public boolean isManagedFileReady(long managedFileId) {
        ManagedFile mf = getManagedFileById(managedFileId);

        return mf.isReady();
    }

    public ManagedFile createManagedFile(String originalFileName, String extension, ManagedFileMountPrefix prefix, double byteSize, int priority) {
        ManagedFile managedFile = new ManagedFile();
        managedFile.setByteSize(byteSize);
        managedFile.setExtension(StringUtils.defaultString(extension).toLowerCase());
        managedFile.setOriginalFileName(originalFileName);
        managedFile.setReady(false);
        managedFile.setPriority(priority);

        String mfPrefix = null;

        if (null == prefix) {
            mfPrefix = ManagedFileMountPrefix.DEFAULT.name().toLowerCase(); // prefix.name().toLowerCase()
        } else {
            mfPrefix = prefix.name().toLowerCase();
        }

        managedFile.setMountPrefix(mfPrefix);

        hibernateTemplate.saveOrUpdate(managedFile);

        StorageNode storageNodeForManagedFile = getStorageNodeForManagedFile(managedFile.getId());
        managedFile.setStorageNode(storageNodeForManagedFile);

        return managedFile;
    }

    public Collection<StorageNode> getAvailableStorageNodes() {
        String hql = "select sn FROM StorageNode sn WHERE sn.offline = false and sn.ready=true and sn.bytesUsed < (sn.totalByteCapacity * 0.95)";
        Collection<StorageNode> nods = hibernateTemplate.find(hql);

        return nods;
    }

    public StorageNode getStorageNodeByMountPrefix(String mountPrefix) {
        List<StorageNode> nodes = hibernateTemplate.findByNamedParam("select sn FROM StorageNode sn WHERE sn.mountPrefix=:mp", "mp", StringUtils.defaultString(mountPrefix));

        if ((nodes != null) && (nodes.size() > 0)) {
            return nodes.iterator().next();
        }

        return null;
    }

    public StorageNode getStorageNodeById(long storageNodeId) {
        return (StorageNode) hibernateTemplate.get(StorageNode.class, storageNodeId);
    }

    // todo finish this
    // todo should this also spin of some sort of process?
    public boolean moveManagedFileToStorageNode(long managedFileId, long storageNodeId) {
        ManagedFile managedFile = getManagedFileById(managedFileId);

        if (managedFile.isReady()) {
            // thenwe need to update the bookkeeping
            StorageNode storageNode = managedFile.getStorageNode();

            if (storageNode != null) {
                storageNode.setBytesUsed(storageNode.getBytesUsed() - managedFile.getByteSize());
                hibernateTemplate.saveOrUpdate(storageNode);
            }
        }

        // now we merely do the move
        return addManagedFileToStorageNode(managedFileId, storageNodeId);
    }

    public boolean addManagedFileToStorageNode(long managedFileId, long storageNodeId) {
        ManagedFile managedFile = getManagedFileById(managedFileId);
        StorageNode storageNode = getStorageNodeById(storageNodeId);

        // ie, this managed file cant be added to a storage node if the storage
        // node has less space available than is needed
        double available = storageNode.getTotalByteCapacity() - storageNode.getBytesUsed();
        double needed = managedFile.getByteSize();

        if (available > needed) {
            managedFile.setStorageNode(storageNode);

            hibernateTemplate.saveOrUpdate(managedFile);

            return true;
        }

        return false;
    }

    public Collection<StorageNode> getStorageNodesByPriority(int priority) {
        return hibernateTemplate.findByNamedParam("select sn from StorageNode sn where sn.priority=:pri AND sn.offline = false and sn.ready = true", "pri", priority);
    }

    public StorageNode getStorageNodeForManagedFile(long managedFileId) {
        ManagedFile mf = getManagedFileById(managedFileId);

        if (mf.getStorageNode() != null) {
            return getStorageNodeById(mf.getStorageNode().getId());
        }

        ArrayList<StorageNode> nodes = new ArrayList<StorageNode>();
        nodes.addAll(getAvailableStorageNodes());
        Collections.sort(nodes,
            new Comparator<StorageNode>() {
                public int compare(StorageNode storageNode, StorageNode storageNode1) {
                    return storageNode.getPriority() - storageNode1.getPriority();
                }
            });

        final int priority = mf.getPriority();
        StorageNode sn;
        sn = (StorageNode) CollectionUtils.find(nodes,
                new Predicate() {
                    public boolean evaluate(Object o) {
                        StorageNode sn = (StorageNode) o;

                        return (sn.getPriority() == priority) && (sn.getBytesUsed() <= (.95 * sn.getTotalByteCapacity()));
                    }
                });

        if (sn == null) {
            sn = (StorageNode) CollectionUtils.find(nodes,
                    new Predicate() {
                        public boolean evaluate(Object o) {
                            StorageNode sn = (StorageNode) o;

                            return (sn.getPriority() != priority) && (sn.getBytesUsed() <= (.95 * sn.getTotalByteCapacity()));
                        }
                    });
        }

        if (sn == null) {
            throw new RuntimeException("Can't allocate disk storage! No space " + "for a managed file with priority " + mf.getPriority() + "on _ANY_ nodes!");
        }

        addManagedFileToStorageNode(managedFileId, sn.getId());

        return getStorageNodeById(getManagedFileById(managedFileId).getStorageNode().getId());
    }
}
