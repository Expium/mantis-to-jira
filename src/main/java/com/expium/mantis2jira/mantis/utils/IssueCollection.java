package com.expium.mantis2jira.mantis.utils;

import java.text.MessageFormat;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.mantis.MantisClient;
import com.expium.mantis2jira.mantis.MantisClientImpl;
import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.util.PropertiesUtil;

/**
 * This collection contains issues from mantis in strict order
 *
 */
public class IssueCollection implements Iterable<IssueModel> {
	final static Logger log = LoggerFactory.getLogger(MantisClientImpl.LOGGER_NAME);

	private int lastestIssueId = 0;
	private boolean createPlaceholders = false;
	private MantisClient mantisClient;

	public IssueCollection(int projectId, boolean createPlaceholders, MantisClient mantisClient, int issueIdLimit) {
		this.lastestIssueId = mantisClient.getLastestIssueId(projectId);
		if (issueIdLimit != 0 && issueIdLimit < lastestIssueId) {
			lastestIssueId = issueIdLimit;
		}
		this.createPlaceholders = createPlaceholders;
		this.mantisClient = mantisClient;

	}

	public IssueCollection(int projectId, boolean createPlaceholders, MantisClient mantisClient) {
		this(projectId, createPlaceholders, mantisClient, 0);
	}

	@Override
	public Iterator<IssueModel> iterator() {
		return new Iterator<IssueModel>() {
			int issueId = 1;

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public IssueModel next() {
				IssueModel issue = mantisClient.getIssue(issueId);
				if (issue == null && createPlaceholders) {
					issue = createPlaceholder(issueId);
				}
				issueId++;
				return issue;
			}

			@Override
			public boolean hasNext() {
				return issueId <= lastestIssueId;
			}
		};
	}

	/**
	 * Create placeholder with given id
	 *
	 * @param issueId
	 * @return
	 */
	// TODO move properties to outside
	private IssueModel createPlaceholder(int issueId) {
		log.info("Creating placeholder for issue {}", issueId);
		IssueModel issue = new IssueModel(issueId);
		issue.setPlaceholder(true);

		String summary = PropertiesUtil.getPlaceholderSummary();
		if (StringUtils.isNotBlank(summary)) {
			issue.setSummary(MessageFormat.format(summary, issueId));
		}

		String description = PropertiesUtil.getPlaceholderDescription();
		if (StringUtils.isNotBlank(description)) {
			issue.setDescription(MessageFormat.format(description, issueId));
		}

		return issue;
	}
}
