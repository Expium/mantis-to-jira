package com.expium.mantis2jira.jira;

import com.expium.mantis2jira.model.AttachmentModel;
import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.model.ReferenceModel;

public interface JiraClient {
	public String createIssue(IssueModel issue);

	public void closeIssue(String issueKey);

	public boolean issueExists(IssueModel issue);

	public void addAttachment(String issueKey, AttachmentModel attach);

	public boolean hasAttachement(String issueKey, AttachmentModel attach);

	/*
	 * References
	 */
	public void addReference(ReferenceModel reference);

	public boolean hasReference(ReferenceModel reference);
}
