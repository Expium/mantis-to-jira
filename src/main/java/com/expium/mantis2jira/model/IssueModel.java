package com.expium.mantis2jira.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.expium.mantis2jira.model.ReferenceModel.ReferenceType;

public class IssueModel {
	private static final String PLACEHOLDER = "Placeholder";

	public enum IssueType {
		BUG, SUB_TASK
	}

	private boolean placeholder;
	private String projectKey;

	private Integer id;
	private Integer projectId;
	
	private IssueType type;
	private String reporterName;
	private String assigneeName;

	private String priority;

	private String summary;
	private String description;
	private String additionalInfo;
	private String stepsToReproduce;

	private String status;
	private String resolution;

	private String category;
	private Date submitted;
	private Date updated;

	private List<String> tags;
	private List<CommentModel> comments;
	private List<AttachmentModel> attachments;
	private List<ReferenceModel> references;
	private boolean isChild;

	private String build;

	public IssueModel(Integer issueId) {
		this.id = issueId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getIssueKey() {
		return projectKey + "-" + id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(boolean placeholder) {
		this.placeholder = placeholder;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getSubmitted() {
		return submitted;
	}

	public void setSubmitted(Date submitted) {
		this.submitted = submitted;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public String getStatus() {
		if (isPlaceholder()) {
			return PLACEHOLDER;
		}
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getReporterName() {
		return reporterName;
	}

	public void setReporterName(String reporterName) {
		this.reporterName = reporterName;
	}

	public String getAssigneeName() {
		return assigneeName;
	}

	public void setAssigneeName(String assigneeName) {
		this.assigneeName = assigneeName;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public List<CommentModel> getComments() {
		if (comments == null) {
			return Collections.emptyList();
		}
		return comments;
	}

	public void setComments(List<CommentModel> comments) {
		this.comments = comments;
	}

	public List<AttachmentModel> getAttachments() {
		if (attachments == null) {
			return Collections.emptyList();
		}
		return attachments;
	}

	public void setAttachments(List<AttachmentModel> attachments) {
		this.attachments = attachments;
	}

	public List<ReferenceModel> getReferences() {
		return references;
	}

	public void setReferences(List<ReferenceModel> references) {
		this.references = references;
		for (ReferenceModel referenceModel : references) {
			if (ReferenceType.CHILD_OF.equals(referenceModel.getType())) {
				isChild = true;
				return;
			}
		}
	}

	public IssueType getType() {
		if (isChild) {
			return IssueType.SUB_TASK;
		}
		return type;
	}

	public void setType(IssueType type) {
		this.type = type;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getStepsToReproduce() {
		return stepsToReproduce;
	}

	public void setStepsToReproduce(String stepsToReproduce) {
		this.stepsToReproduce = stepsToReproduce;
	}
}
