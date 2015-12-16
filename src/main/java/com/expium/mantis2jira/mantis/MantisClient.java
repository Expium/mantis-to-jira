package com.expium.mantis2jira.mantis;

import java.util.Collection;

import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.model.Project;

public interface MantisClient {
	public boolean isReady();

	public Integer getProjectId(String name);

	public Collection<Project> getProjects();

	public byte[] getAttachContent(int attachId);

	public IssueModel getIssue(int id);

	// public Iterable<IssueModel> getIssues(int projectId, boolean
	// createPlaceholders);

	public Iterable<IssueModel> getIssues(int projectId, boolean createPlaceholders, int issueIdLimit);

	public Integer getLastestIssueId(int projectId);

	public byte[] getAttachmentContent(int attachmentId);
}
