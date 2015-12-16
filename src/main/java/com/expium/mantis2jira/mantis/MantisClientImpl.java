package com.expium.mantis2jira.mantis;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.mantis.utils.ConvertUtil;
import com.expium.mantis2jira.mantis.utils.IssueCollection;
import com.expium.mantis2jira.mantis.utils.MetaData;
import com.expium.mantis2jira.model.AttachmentModel;
import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.model.Project;

import biz.futureware.mantis.rpc.soap.client.AccountData;
import biz.futureware.mantis.rpc.soap.client.IssueData;
import biz.futureware.mantis.rpc.soap.client.MantisConnectLocator;
import biz.futureware.mantis.rpc.soap.client.MantisConnectPortType;
import biz.futureware.mantis.rpc.soap.client.ObjectRef;
import biz.futureware.mantis.rpc.soap.client.ProjectData;

public class MantisClientImpl implements MantisClient {
	public static final String LOGGER_NAME = "MANTIS";
	final static Logger log = LoggerFactory.getLogger(LOGGER_NAME);

	private static final String API_URL = "/api/soap/mantisconnect.php";
	private static final String CLIENT_MANTIS_VERSION = "1.2.11";

	private String url;
	private String username;
	private String password;

	private MantisConnectPortType port;
	private MetaData metaData;

	private boolean isReady = false;
	private Map<Integer, String> usersCache = new HashMap<Integer, String>();

	public MantisClientImpl(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
		init();
	}

	private void init() {
		try {
			port = new MantisConnectLocator().getMantisConnectPort(new URL(url + API_URL));
			String mantisVersion = port.mc_version();
			if (!mantisVersion.equals(CLIENT_MANTIS_VERSION)) {
				log.warn("MantisBT server version is {}, but application was developed for version {}", mantisVersion,
						CLIENT_MANTIS_VERSION);
			} else {
				log.info("MantisBT server version is: " + mantisVersion);
			}
			isReady = true;
			return;
		} catch (MalformedURLException e) {
			log.error("Wrong Mantis BT url");
		} catch (ServiceException e) {
			log.error("Cannot estabilish connection with Mantis");
		} catch (RemoteException e) {
			log.error("Cannot get Mantis version: " + e.getMessage());
		}
		log.error("Looks like application cannot connect to Mantis BT server on URL {}", url);
		isReady = false;
	}

	/*
	 * Interface methods
	 */
	public boolean isReady() {
		return isReady;
	}

	public Collection<Project> getProjects() {
		try {
			ProjectData[] data = port.mc_projects_get_user_accessible(username, password);
			List<Project> projects = new ArrayList<Project>(data.length);
			for (ProjectData projectData : data) {
				projects.add(ConvertUtil.convertProject(projectData));
			}
			return projects;
		} catch (RemoteException e) {
			log.error("User projects not found on server: " + e.getMessage());
		}
		return null;
	}

	public Integer getProjectId(String name) {
		try {

			Integer projectId = port.mc_project_get_id_from_name(username, password, name).intValue();
			AccountData[] users = port.mc_project_get_users(username, password, BigInteger.valueOf(projectId),
					BigInteger.valueOf(0));
			for (AccountData user : users) {
				usersCache.put(user.getId().intValue(), user.getName());
			}
			return projectId;
		} catch (RemoteException e) {
			log.error("Unable to find project on server: " + e.getMessage());
		}
		return null;
	}

	public byte[] getAttachContent(int attachId) {
		try {
			return port.mc_issue_attachment_get(username, password, BigInteger.valueOf(attachId));
		} catch (RemoteException e) {
			log.error("Cannot get attachement with id " + attachId);
		}
		return null;
	}

	public IssueData[] getIssueData(int projectId, int perPage, int page) throws RemoteException {
		return port.mc_project_get_issues(username, password, BigInteger.valueOf(projectId), BigInteger.valueOf(page),
				BigInteger.valueOf(perPage));
	}

	public Iterable<IssueModel> getIssues(int projectId, boolean createPlaceholders, int issueIdLimit) {
		return new IssueCollection(projectId, createPlaceholders, this, issueIdLimit);
	}

	public Iterable<IssueModel> getIssues(int projectId, boolean createPlaceholders) {
		return getIssues(projectId, createPlaceholders, 0);
	}

	public IssueModel getIssue(int id) {
		try {
			IssueModel issue = ConvertUtil.convertIssue(port.mc_issue_get(username, password, BigInteger.valueOf(id)),
					metaData);
			List<AttachmentModel> attachments = issue.getAttachments();
			if (CollectionUtils.isNotEmpty(attachments)) {
				for (AttachmentModel attach : attachments) {
					attach.setUserName(usersCache.get(attach.getUserId()));
				}
			}
			return issue;
		} catch (RemoteException e) {
			log.error("Error on server: " + e.getMessage());
		}
		return null;
	}

	public Integer getLastestIssueId(int projectId) {
		try {
			return port.mc_issue_get_biggest_id(username, password, BigInteger.valueOf(projectId)).intValue();
		} catch (RemoteException e) {
			log.error("Cannot get lastest issue id: " + e.getMessage());
		}
		return null;
	}

	@Override
	public byte[] getAttachmentContent(int attachmentId) {
		try {
			return port.mc_issue_attachment_get(username, password, BigInteger.valueOf(attachmentId));
		} catch (RemoteException e) {
			log.error("Cannot get attachement content. " + e.getMessage());
		}
		return null;
	}

}
