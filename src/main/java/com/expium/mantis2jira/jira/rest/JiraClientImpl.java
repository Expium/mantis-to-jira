package com.expium.mantis2jira.jira.rest;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.expium.mantis2jira.jira.JiraClient;
import com.expium.mantis2jira.jira.converter.ConvertUtil;
import com.expium.mantis2jira.model.AttachmentModel;
import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.model.ReferenceModel;

public class JiraClientImpl implements JiraClient {
	private static final String LOGER_NAME = "JIRA";
	final static Logger log = LoggerFactory.getLogger(LOGER_NAME);

	private static final String JIRA_VERSION = "5.1.6";

	private static final long BUG_TYPE_ID = (long) 1;
	private static final String RESOLUTION_FIELD = "resolution";

	private String username;
	private String password;
	private String url;

	private JiraRestClient client;
	private IssueRestClient issueClient;

	private ProgressMonitor pm = new NullProgressMonitor();

	public JiraClientImpl(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
		init();
	}

	private void init() {
		try {
			client = new JerseyJiraRestClientFactory().createWithBasicHttpAuthentication(new URI(url), username,
					password);
			String version = client.getMetadataClient().getServerInfo(pm).getVersion();
			if (!StringUtils.equals(version, JIRA_VERSION)) {
				log.warn("Jira version is {}, but application was developed for {}", version, JIRA_VERSION);
			}
			issueClient = client.getIssueClient();
		} catch (URISyntaxException e) {
			log.error("Invalid jira url");
		}
	}

	public void addReference(ReferenceModel reference) {
		String referenceType = ConvertUtil.convertReferenceTypeToString(reference.getType());
		if (StringUtils.isEmpty(referenceType)) {
			// We skip not convertable references
			log.info("Cannot convert reference type {} into JIRA link", reference.getType().name());
			return;
		}
		LinkIssuesInput link = new LinkIssuesInput(ConvertUtil.convertIdToKey(reference.getOwnerId()),
				ConvertUtil.convertIdToKey(reference.getTargetId()), referenceType);
		issueClient.linkIssue(link, pm);
	}

	public boolean hasReference(ReferenceModel reference) {
		String referenceType = ConvertUtil.convertReferenceTypeToString(reference.getType());
		if (StringUtils.isEmpty(referenceType)) {
			// If reference is not convertable, we say that it already exists
			return true;
		}
		String targetKey = ConvertUtil.convertIdToKey(reference.getTargetId());
		Issue issue = issueClient.getIssue(ConvertUtil.convertIdToKey(reference.getOwnerId()), pm);

		for (IssueLink issueLink : issue.getIssueLinks()) {
			if (!StringUtils.equals(issueLink.getTargetIssueKey(), targetKey)) {
				continue;
			}
			// if
			// (!issueLink.getIssueLinkType().getDirection().equals(Direction.OUTBOUND))
			// {
			// continue;
			// }
			if (!StringUtils.equals(issueLink.getIssueLinkType().getName(), referenceType)) {
				continue;
			}
			return true;
		}
		return false;
	}

	public void addAttachment(String issueKey, AttachmentModel attach) {
		if (attach.getData() == null) {
			log.error("File {0} content is empty. Cannot export file.", attach.getFileName());
			return;
		}
		Issue issue = issueClient.getIssue(issueKey, pm);
		issueClient.addAttachment(pm, issue.getAttachmentsUri(), new ByteArrayInputStream(attach.getData()),
				attach.getFileName());
	}

	public boolean hasAttachement(String issueKey, AttachmentModel attach) {
		Issue issue = issueClient.getIssue(issueKey, pm);
		for (Attachment a : issue.getAttachments()) {
			if (StringUtils.equals(attach.getFileName(), a.getFilename()) && a.getSize() == attach.getSize()) {
				return true;
			}
		}
		return false;
	}

	public boolean issueExists(IssueModel issue) {
		try {
			issueClient.getIssue(issue.getIssueKey(), pm);
		} catch (RestClientException e) {
			return false;
		}
		return true;
	}

	public String createIssue(IssueModel issueModel) {
		IssueInputBuilder builder = new IssueInputBuilder(issueModel.getProjectKey(), BUG_TYPE_ID);
		builder.setSummary(issueModel.getSummary());
		builder.setDescription(issueModel.getDescription());
		builder.setReporterName(issueModel.getReporterName());
		builder.setAssigneeName(issueModel.getAssigneeName());
		builder.setComponentsNames(Arrays.asList(issueModel.getCategory()));
		String key = issueClient.createIssue(builder.build(), pm).getKey();
		return key;
	}

	public void closeIssue(String issueKey) {
		Issue issue = issueClient.getIssue(issueKey, pm);
		setStatus(issue, TransitionsEnum.CLOSE_ISSUE);
	}

	public void openIssue(String issueKey) {
		Issue issue = issueClient.getIssue(issueKey, pm);
		setStatus(issue, TransitionsEnum.REOPEN_ISSUE);
	}

	/*
	 * ---------------------------PRIVATE METHODS -----------------------
	 */
	private void setStatus(Issue issue, TransitionsEnum transitionEnum, ResolutionsEnum resolution) {
		Transition transition = getTransitionByEnum(issueClient.getTransitions(issue, pm), transitionEnum);
		if (transition == null) {
			return;
		}
		Collection<FieldInput> fieldInputs = Arrays.asList(new FieldInput(RESOLUTION_FIELD, resolution.getValue()));
		issueClient.transition(issue, new TransitionInput(transition.getId(), fieldInputs), pm);
	}

	private void setStatus(Issue issue, TransitionsEnum transitionEnum) {
		setStatus(issue, transitionEnum, ResolutionsEnum.FIXED);
	}

	private Transition getTransitionByEnum(Iterable<Transition> transitions, TransitionsEnum transitionEnum) {
		for (Transition transition : transitions) {
			if (transition.getName().equals(transitionEnum.getValue())) {
				return transition;
			}
		}
		return null;
	}
}
