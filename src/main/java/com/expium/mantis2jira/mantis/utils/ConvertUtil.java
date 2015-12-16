package com.expium.mantis2jira.mantis.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTimeZone;

import com.expium.mantis2jira.model.AttachmentModel;
import com.expium.mantis2jira.model.CommentModel;
import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.model.IssueModel.IssueType;
import com.expium.mantis2jira.model.Project;
import com.expium.mantis2jira.model.ReferenceModel;
import com.expium.mantis2jira.model.ReferenceModel.ReferenceType;

import biz.futureware.mantis.rpc.soap.client.AccountData;
import biz.futureware.mantis.rpc.soap.client.AttachmentData;
import biz.futureware.mantis.rpc.soap.client.IssueData;
import biz.futureware.mantis.rpc.soap.client.IssueNoteData;
import biz.futureware.mantis.rpc.soap.client.ObjectRef;
import biz.futureware.mantis.rpc.soap.client.ProjectData;
import biz.futureware.mantis.rpc.soap.client.RelationshipData;

/**
 * Class required for converting mantis SOAP data to application internal model
 *
 */
public class ConvertUtil {
	private static DateTimeZone defaultTz = DateTimeZone.getDefault();

	private static Map<String, ReferenceType> referencesMap = new HashMap<String, ReferenceType>();
	static {
		referencesMap.put("parent of", ReferenceType.PARENT_OF);
		referencesMap.put("child of", ReferenceType.CHILD_OF);
		referencesMap.put("duplicate of", ReferenceType.DUPLICATE_OF);
		referencesMap.put("has duplicate", ReferenceType.HAS_DUPLICATE);
		referencesMap.put("related to", ReferenceType.RELATED_TO);
	}

	/**
	 * Convert SOAP data to model
	 *
	 * @param data
	 * @param metaData
	 * @return
	 */
	public static IssueModel convertIssue(IssueData data, MetaData metaData) {
		IssueModel issue = new IssueModel(data.getId().intValue());
		issue.setProjectId(data.getProject().getId().intValue());

		issue.setType(IssueType.BUG);
		issue.setReporterName(data.getReporter().getName());

		AccountData handler = data.getHandler();
		if (handler != null) {
			issue.setAssigneeName(handler.getName());
		}

		issue.setSummary(data.getSummary());
		issue.setDescription(data.getDescription());
		issue.setAdditionalInfo(data.getAdditional_information());
		issue.setStepsToReproduce(data.getSteps_to_reproduce());

		issue.setCategory(data.getCategory());

		issue.setSubmitted(getTime(data.getDate_submitted()));
		issue.setUpdated(getTime(data.getLast_updated()));
		issue.setStatus(data.getStatus().getName());
		issue.setResolution(data.getResolution().getName());
		issue.setPriority(data.getPriority().getName());
		issue.setBuild(data.getBuild());
		/*
		 * Complex models
		 */
		issue.setTags(getTags(data));
		issue.setComments(getComments(data));
		issue.setAttachments(getAttachments(data));
		issue.setReferences(getReferences(data, issue.getId()));

		return issue;
	}
	/**
	 * This method extracts references from SOAP data
	 * @param data
	 * @param ownerId
	 * @return
	 */
	private static List<ReferenceModel> getReferences(IssueData data, Integer ownerId) {
		RelationshipData[] relationships = data.getRelationships();
		if (ArrayUtils.isEmpty(relationships)) {
			return Collections.emptyList();
		}
		List<ReferenceModel> references = new ArrayList<ReferenceModel>(relationships.length);
		for (RelationshipData relationshipData : relationships) {
			ReferenceModel ref = new ReferenceModel();
			ref.setOwnerId(ownerId);
			ref.setType(referencesMap.get(relationshipData.getType().getName()));
			ref.setTargetId(relationshipData.getTarget_id().intValue());
			references.add(ref);
		}
		return references;
	}

	/**
	 * This method extracts tags from SOAP data
	 * @param data
	 * @return
	 */
	private static List<String> getTags(IssueData data) {
		ObjectRef[] tags = data.getTags();
		if (ArrayUtils.isEmpty(tags)) {
			return Collections.emptyList();
		}

		List<String> tagsList = new ArrayList<String>();
		for (ObjectRef tag : tags) {
			tagsList.add(tag.getName());
		}
		return tagsList;
	}

	/**
	 * Method extracts comments from WS data
	 *
	 * @param data
	 * @return
	 */
	protected static List<CommentModel> getComments(IssueData data) {
		IssueNoteData[] notes = data.getNotes();
		if (ArrayUtils.isEmpty(notes)) {
			return Collections.emptyList();
		}
		List<CommentModel> commentList = new ArrayList<CommentModel>();
		for (IssueNoteData note : notes) {
			CommentModel comment = new CommentModel();
			comment.setUserName(note.getReporter().getName());
			comment.setText(note.getText());
			comment.setCreated(getTime(note.getDate_submitted()));
			commentList.add(comment);
		}
		return commentList;
	}

	/**
	 * This method extracts attachments from SOAP data
	 *
	 * @param data
	 * @return
	 */
	protected static List<AttachmentModel> getAttachments(IssueData data) {
		AttachmentData[] attachments = data.getAttachments();
		if (ArrayUtils.isEmpty(attachments)) {
			return Collections.emptyList();
		}
		List<AttachmentModel> attachmentsList = new ArrayList<AttachmentModel>();
		for (AttachmentData a : attachments) {
			AttachmentModel attachment = new AttachmentModel();
			attachment.setId(a.getId().intValue());
			attachment.setUserId(a.getUser_id().intValue());
			attachment.setFileName(a.getFilename());
			attachment.setSize(a.getSize().intValue());
			attachment.setMimeType(a.getContent_type());
			attachment.setUploaded(getTime(a.getDate_submitted()));
			attachment.setDownloadUrl(a.getDownload_url().toString());
			attachmentsList.add(attachment);
		}
		return attachmentsList;
	}

	private static Date getTime(Calendar calendar) {
		return calendar.getTime();
	}

	public static Project convertProject(ProjectData data) {
		Project project = new Project();
		project.setName(data.getName());
		project.setId(data.getId().intValue());
		return project;
	}
}
