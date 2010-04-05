package com.joshlong.jukebox2.services.impl;

/**
 * @author josh
 *
 * Code for dealing files as they are stored nthe file system
 *
 *
 */

import com.quietriot.ManagedFileService;
import com.quietriot.UploadService;
import com.joshlong.jukebox2.services.impl.util.BaseService;
import com.joshlong.jukebox2.services.impl.util.Bytes;
import com.joshlong.jukebox2.services.impl.util.ManagedFileMountPrefix;
import com.quietriot.model.ManagedFile;
import com.quietriot.model.StorageNode;
import com.quietriot.model.UploadBundle;
import com.quietriot.model.UserCredentials;
import com.joshlong.jukebox2.services.workflow.WorkflowService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbpm.graph.exe.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class ManagedFileServiceImpl extends BaseService implements ManagedFileService {
	public String getWebMediaFullUrl() {
		return webMediaFullUrl;
	}

	public String getFilePathExtension(String p) {
		if (!StringUtils.isEmpty(p)) {
			int li = p.lastIndexOf(".");
			if (li != -1) {
				String ext = StringUtils.defaultString(p.substring(li));
				if (ext.startsWith("."))
					ext = ext.substring(1);

				if (ext.indexOf(" ") == -1)
					return ext.toLowerCase();
			}
		}
		return null;
	}

	public void setWebMediaFullUrl(String webMediaFullUrl) {
		this.webMediaFullUrl = webMediaFullUrl;
	}

	private String webMediaFullUrl;

	@Autowired
	private UploadService uploadService;
	@Autowired
	private WorkflowService workflowService;

	public String getWebPathForManagedFile(long managedFileId, String host) {

		ManagedFile mf = getManagedFileById(managedFileId);
		long lid = mf.getId();
		long rootFolderId = deriveFolderIdFor(lid);
		getLoggingUtils().log("rootFolderId:" + rootFolderId);
		StorageNode storageNodeForManagedFile = getStorageNodeForManagedFile(mf.getId());
		getLoggingUtils().log("storage node:" + storageNodeForManagedFile.getId());
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
		getLoggingUtils().log("rootFolderId:" + rootFolderId);
		StorageNode sn = getStorageNodeForManagedFile(mf.getId());
		getLoggingUtils().log("storage node:" + sn.getId());
		String mountPrefix = sn.getMountPrefix();
		String pathString = String.format("%s/%s/media/" + assetPathMask, mapPrefixToFileSystem(mf.getMountPrefix()), mountPrefix, rootFolderId, lid, mf.getExtension());
		return pathString;
	}

	public String getPathForUploadBundleFile(long uploadBundleId, String fileName) {
		String pathForUploadBundled = getPathForUploadBundle(uploadBundleId);
		loggingUtils.log(pathForUploadBundled + ":" + fileName);
		return new File(pathForUploadBundled, fileName).getAbsolutePath();
	}

	public String getPathForUploadBundle(long uploadBundleId) {
		UploadBundle ubUploadBundle = uploadService.getUploadBundleById(uploadBundleId);
		UserCredentials userCredentials = ubUploadBundle.getUserCredentials();
		File f = new File(getUserCredentialFtpUploadHomeDirectory(userCredentials.getId()), ubUploadBundle.getName());
		return f.getAbsolutePath();

	}

	public StorageNode createStorageNode(String mountPrefix, double totalByteCapacity) {

		StorageNode existingStorageNode = getStorageNodeByMountPrefix(mountPrefix);

		if (null != existingStorageNode)
			return existingStorageNode;

		StorageNode newStorageNode = new StorageNode();
		newStorageNode.setBytesUsed(0);
		newStorageNode.setMountPrefix(mountPrefix);
		newStorageNode.setOffline(false);
		newStorageNode.setReady(true);
		newStorageNode.setTotalByteCapacity(totalByteCapacity);
		getHibernateTemplate().save(newStorageNode);
		return getStorageNodeById(newStorageNode.getId());
	}

	public void deleteManagedFile(long managedFileId) {
		ManagedFile managedFile = getManagedFileById(managedFileId);
		StorageNode sn = managedFile.getStorageNode();
		managedFile.setStorageNode(null);
		getHibernateTemplate().saveOrUpdate(managedFile);
		if (!managedFile.isReady()) {
			loggingUtils.log("not deleted managed file " + managedFileId + " because it's not 'ready'.");
			getHibernateTemplate().delete(managedFile);
			return; // theres no file to delete!
		}

		// update the storage node, as well
		if (managedFile.isReady()) {
			// then deduct
			sn.setBytesUsed(sn.getBytesUsed() - managedFile.getByteSize());
			getHibernateTemplate().saveOrUpdate(sn);
		}

		String path = getPathForManagedFile(managedFileId);
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("managedFilePath", path);
		ProcessInstance pi = workflowService.createProcessInstance("delete-managed-file", vars);
		workflowService.startProcessInstance(pi.getId());
		getHibernateTemplate().delete(managedFile);
		getLoggingUtils().log("just called delete managed file on mf DB object for " + path);
	}

	public String getUserCredentialFtpUploadHomeDirectory(long userCredentialsId) {
		File baseForUploads = mapPrefixToFileSystem(ManagedFileMountPrefix.UPLOADS.toString().toLowerCase());

		return new File(baseForUploads, userCredentialsId + "").getAbsolutePath();

	}

	private static String MANAGED_FILE_MNT_DIRECTORY = "managedFiles";

	private String mapPrefixToWeb(String mPrefix) {
		return String.format("%s/%s", MANAGED_FILE_MNT_DIRECTORY, mPrefix);
	}

	private File mapPrefixToFileSystem(String prefix) {
		File usrHome = /* SystemUtils.getUserHome(); */new File("/home/quietriot/");
		File cdfs = new File(usrHome, MANAGED_FILE_MNT_DIRECTORY);
		return new File(cdfs, prefix);
	}

	private String assetPathMask = "%010d/%010d.%s";

	public ManagedFile getManagedFileById(long id) {
		return get(ManagedFile.class, id);
	}

	public void setManagedFileReadyYN(final long mfid, final boolean yesOrNo) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ManagedFile managedFile = getManagedFileById(mfid);
				boolean readyAlready = managedFile.isReady();
				managedFile.setReady(yesOrNo);

				getHibernateTemplate().saveOrUpdate(managedFile);

				StorageNode storageNode = managedFile.getStorageNode();
				if (storageNode == null)
					throw new RuntimeException(String.format("You cant mark a managed file as ready without it " + "being assigned to a storage node! Managed File: %s", managedFile.getId()));
				if (storageNode != null) {
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
						getLoggingUtils().log("Discounting " + managedFile.getByteSize() + " bytes from storageNode #" + storageNode.getId());
					}

					getHibernateTemplate().saveOrUpdate(storageNode);

				}

				return managedFile;
			}
		});
	}

	public void setStorageNodeReady(long storageNodeId, boolean readyYesOrNo) {
		StorageNode storageNode = get(StorageNode.class, storageNodeId);
		storageNode.setReady(readyYesOrNo);
		getHibernateTemplate().saveOrUpdate(storageNode);
	}

	public void setManagedFilePriority(long managedFileId, int priority) {
		ManagedFile mf = getManagedFileById(managedFileId);
		mf.setPriority(priority);
		getHibernateTemplate().saveOrUpdate(mf);
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
		} else
			mfPrefix = prefix.name().toLowerCase();

		managedFile.setMountPrefix(mfPrefix);

		getHibernateTemplate().saveOrUpdate(managedFile);
		StorageNode storageNodeForManagedFile = getStorageNodeForManagedFile(managedFile.getId());
		managedFile.setStorageNode(storageNodeForManagedFile);
		getLoggingUtils().log(String.format("the storage node for managed file # %s is # %s", managedFile.getId(), storageNodeForManagedFile.getId()));
		return managedFile;
	}

	public Collection<StorageNode> getAvailableStorageNodes() {
		String hql = "select sn FROM StorageNode sn WHERE sn.offline = false and sn.ready=true and sn.bytesUsed < (sn.totalByteCapacity * 0.95)";
		Collection<StorageNode> nods = getHibernateTemplate().find(hql);
		return nods;
	}

	public StorageNode getStorageNodeByMountPrefix(String mountPrefix) {

		List<StorageNode> nodes = getHibernateTemplate().findByNamedParam("select sn FROM StorageNode sn WHERE sn.mountPrefix=:mp", "mp", StringUtils.defaultString(mountPrefix));
		if (nodes != null && nodes.size() > 0) {
			return nodes.iterator().next();
		}
		return null;
	}

	public StorageNode getStorageNodeById(long storageNodeId) {
		return (StorageNode) getHibernateTemplate().get(StorageNode.class, storageNodeId);
	}

	// todo finish this
	// todo should this also spin of some sort of process?
	public boolean moveManagedFileToStorageNode(long managedFileId, long storageNodeId) {
		ManagedFile managedFile = getManagedFileById(managedFileId);
		if (managedFile.isReady()) {
			// thenwe need to update the bookkeeping
			StorageNode storageNode = managedFile.getStorageNode();
			if (storageNode != null) {
				storageNode.setBytesUsed(storageNode.getBytesUsed() - managedFile.getByteSize()); // so
																									// deduct
																									// the
																									// size
																									// of
																									// this
																									// file
																									// if
																									// its
																									// ready
				getHibernateTemplate().saveOrUpdate(storageNode);
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

			getHibernateTemplate().saveOrUpdate(managedFile);
			getLoggingUtils().log(String.format("assigned storagenode.id==%s to managedfile.id==%s", storageNode.getId(), managedFile.getId()));
			return true;
		}
		return false;
	}

	public Collection<StorageNode> getStorageNodesByPriority(int priority) {
		return getHibernateTemplate().findByNamedParam("select sn from StorageNode sn where sn.priority=:pri AND sn.offline = false and sn.ready = true", "pri", priority);

	}

	public StorageNode getStorageNodeForManagedFile(long managedFileId) {
		ManagedFile mf = getManagedFileById(managedFileId);
		if (mf.getStorageNode() != null) {
			return getStorageNodeById(mf.getStorageNode().getId());
		}
		ArrayList<StorageNode> nodes = new ArrayList<StorageNode>();
		nodes.addAll(getAvailableStorageNodes());
		Collections.sort(nodes, new Comparator<StorageNode>() {
			public int compare(StorageNode storageNode, StorageNode storageNode1) {
				return storageNode.getPriority() - storageNode1.getPriority();
			}
		});
		final int priority = mf.getPriority();
		StorageNode sn = null;
		sn = (StorageNode) CollectionUtils.find(nodes, new Predicate() {
			public boolean evaluate(Object o) {
				StorageNode sn = (StorageNode) o;
				return sn.getPriority() == priority && (sn.getBytesUsed() <= (.95 * sn.getTotalByteCapacity()));
			}
		});
		if (null != sn)
			getLoggingUtils().log("am assigning " + sn.getMountPrefix() + " to mf " + mf.getId());

		if (sn == null) {

			sn = (StorageNode) CollectionUtils.find(nodes, new Predicate() {
				public boolean evaluate(Object o) {
					StorageNode sn = (StorageNode) o;
					return sn.getPriority() != priority && (sn.getBytesUsed() <= (.95 * sn.getTotalByteCapacity()));
				}
			});
			if (null != sn)
				getLoggingUtils().log("am assigning " + sn.getMountPrefix() + " to mf " + mf.getId());
		}

		if (sn == null)
			throw new RuntimeException("Can't allocate disk storage! No space " + "for a managed file with priority " + mf.getPriority() + "on _ANY_ nodes!");

		addManagedFileToStorageNode(managedFileId, sn.getId());
		return getStorageNodeById(getManagedFileById(managedFileId).getStorageNode().getId());
	}

	@Override
	protected void initDao() throws Exception {
		for (int i = 0; i < 4; i++)
			createStorageNode("d00" + i, Bytes.terabytes(1).bytes());
	}
}
