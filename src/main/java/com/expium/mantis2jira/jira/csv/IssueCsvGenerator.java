package com.expium.mantis2jira.jira.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.jira.converter.ConvertUtil;
import com.expium.mantis2jira.model.AttachmentModel;
import com.expium.mantis2jira.model.CommentModel;
import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.model.ReferenceModel;
import com.expium.mantis2jira.model.IssueModel.IssueType;
import com.expium.mantis2jira.model.ReferenceModel.ReferenceType;
import com.expium.mantis2jira.util.UsersMapper;

import au.com.bytecode.opencsv.CSVWriter;

public class IssueCsvGenerator {
	private static final char SEPARATOR = ',';
	public static final String LOGER_NAME = "CSV";
	final static Logger log = LoggerFactory.getLogger(LOGER_NAME);

	public static final String DATE_FORMAT = "MM/dd/yy hh:mm:ss a";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
	{
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private UsersMapper usersMapper;
	private int maxComments;
	private int maxAttachments;
	private List<CsvColumn> columns;
	private String[] header;

	private CSVWriter writer;
	private String filename;

	public enum CsvColumn {
		ID("Issue Id"), CATEGORY("Category"), SUMMARY("Summary"), DESCRIPTION("Description"), CREATED("Date Created"), MODIFIED(
				"Date Modified"), TYPE("Issue type"), PRIORITY("Priority"), STATUS("Status"), RESOLUTION("Resolution"), REPORTER(
				"Reporter"), ASSIGNEE("Assignee"), TAGS("Tags"), BUILD("Build"), COMMENTS("Comments"), ATTACHMENT(
				"Attachment"), PARENT_ID("Parent Id");

		private String value;

		private CsvColumn(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public enum ReferencesColumn {
		ID("Issue Id"), SUMMARY("Summary");
		private String value;

		private ReferencesColumn(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public IssueCsvGenerator(int maxComments, int maxAttachments, String filename, UsersMapper usersMapper) {
		this.filename = filename;
		this.maxAttachments = maxAttachments;
		this.maxComments = maxComments;
		this.usersMapper = usersMapper;
		this.columns = getColumns();
		this.header = getHeader(columns);
		init();
	}

	private void init() {
		if (StringUtils.isBlank(filename)) {
			throw new IllegalStateException("Issues csv filename cannot be empty");
		}
		try {
			writer = new CSVWriter(new FileWriter(filename), SEPARATOR);
			writer.writeNext(header);
		} catch (IOException e) {
			log.error("Error on writing to file", e);
		}
	}

	public void addIssue(IssueModel issueModel) {
		if (writer == null) {
			throw new IllegalStateException("Writer cannot be null");
		}
		log.info("Writing issue id: {}", ConvertUtil.convertIdToKey(issueModel.getId()));
		writer.writeNext(getRow(issueModel));
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			log.error("Error on writing to file", e);
		}
		log.info("CSV file '{}' succesfully exported", filename);
	}

	private List<CsvColumn> getColumns() {
		List<CsvColumn> columns = new ArrayList<CsvColumn>();
		columns.add(CsvColumn.ID);
		columns.add(CsvColumn.CATEGORY);
		columns.add(CsvColumn.SUMMARY);
		columns.add(CsvColumn.DESCRIPTION);
		columns.add(CsvColumn.CREATED);
		columns.add(CsvColumn.MODIFIED);
		columns.add(CsvColumn.PRIORITY);
		columns.add(CsvColumn.TYPE);
		columns.add(CsvColumn.STATUS);
		columns.add(CsvColumn.RESOLUTION);
		columns.add(CsvColumn.REPORTER);
		columns.add(CsvColumn.ASSIGNEE);
		columns.add(CsvColumn.TAGS);
		columns.add(CsvColumn.BUILD);
		columns.add(CsvColumn.PARENT_ID);

		for (int i = 0; i < maxComments; i++) {
			columns.add(CsvColumn.COMMENTS);
		}
		for (int i = 0; i < maxAttachments; i++) {
			columns.add(CsvColumn.ATTACHMENT);
		}
		return columns;
	}

	private String[] getHeader(List<CsvColumn> columns) {
		String[] header = new String[columns.size()];
		for (int i = 0; i < header.length; i++) {
			header[i] = columns.get(i).getValue();
		}
		return header;
	}

	private String[] getRow(IssueModel model) {
		List<String> entries = new ArrayList<String>();

		boolean commentsProcessed = false;
		boolean attachmentsProcessed = false;

		for (int i = 0; i < columns.size(); i++) {
			CsvColumn column = columns.get(i);
			switch (column) {
			case ID:
				entries.add(ConvertUtil.convertIdToKey(model.getId()));
				break;
			case CATEGORY:
				entries.add(model.getCategory());
				break;
			case ASSIGNEE:
				entries.add(getAssignee(model));
				break;
			case BUILD:
				entries.add(model.getBuild());
				break;
			case CREATED:
				entries.add(getDate(model.getSubmitted()));
				break;
			case DESCRIPTION:
				entries.add(getDescription(model));
				break;
			case MODIFIED:
				entries.add(getDate(model.getUpdated()));
				break;
			case PARENT_ID:
				entries.add(getParentId(model));
				break;
			case PRIORITY:
				entries.add(model.getPriority());
				break;
			case REPORTER:
				entries.add(usersMapper.getJiraUsername(model.getReporterName()));
				break;
			case RESOLUTION:
				entries.add(model.getResolution());
				break;
			case STATUS:
				entries.add(model.getStatus());
				break;
			case SUMMARY:
				entries.add(ConvertUtil.replaceMantisLinks(model.getSummary()));
				break;
			case TAGS:
				entries.add(getTags(model));
				break;
			case TYPE:
				entries.add(getType(model));
				break;
			case COMMENTS:
				if (commentsProcessed) {
					throw new IllegalStateException(
							"Comments are already processed! Something wrong with column order.");
				}
				i += maxComments;
				commentsProcessed = true;
				entries.addAll(getComments(model));
				break;
			case ATTACHMENT:
				if (attachmentsProcessed) {
					throw new IllegalStateException(
							"Attachments are already processed! Something wrong with column order.");
				}
				i += maxAttachments;
				attachmentsProcessed = true;
				entries.addAll(getAttachments(model));
				break;
			default:
				throw new IllegalStateException(MessageFormat.format("Unknown type of column {0}", column.getValue()));
			}
		}

		return entries.toArray(new String[0]);
	}

	protected String getAssignee(IssueModel model) {
		String assigneeName = model.getAssigneeName();
		if (StringUtils.isBlank(assigneeName)) {
			assigneeName = usersMapper.getUnassignedUser();
		} else {
			assigneeName = usersMapper.getJiraUsername(assigneeName);
		}
		return assigneeName;
	}

	protected String getDescription(IssueModel model) {
		StringBuilder description = new StringBuilder(model.getDescription());
		if (StringUtils.isNotBlank(model.getAdditionalInfo())) {
			description.append(MessageFormat.format("\n\nAdditional Information:\n{0}", model.getAdditionalInfo()));
		}
		if (StringUtils.isNotBlank(model.getStepsToReproduce())) {
			description.append(MessageFormat.format("\n\nSteps to Reproduce:\n{0}", model.getStepsToReproduce()));
		}
		return ConvertUtil.replaceMantisLinks(description.toString());
	}

	/**
	 * Method parses issue model, and creates collection with formatted comments
	 * 
	 * @param model
	 * @return
	 */
	private Collection<String> getAttachments(IssueModel model) {
		List<AttachmentModel> attachments = model.getAttachments();
		if (attachments.size() > maxAttachments) {
			throw new IllegalStateException(MessageFormat.format(
					"Issue {0} has {1} attachements, but limit is {2}. Please, increase attachments limit.",
					model.getId(), attachments.size(), maxAttachments));
		}

		List<String> csvAttachment = new ArrayList<String>(maxComments);

		boolean hasComments = CollectionUtils.isNotEmpty(attachments);
		if (hasComments) {
			for (AttachmentModel attach : attachments) {
				String date = dateFormat.format(attach.getUploaded());
				csvAttachment.add(MessageFormat.format("{0};{1};{2};{3}", date,
						usersMapper.getJiraUsername(attach.getUserName()), attach.getFileName(),
						ConvertUtil.getAttachmentDownloadUrl(attach)));
			}
		}

		/*
		 * Here we add empty attachments for other attachment columns
		 */
		for (int i = attachments.size(); i < maxAttachments; i++) {
			csvAttachment.add(StringUtils.EMPTY);
			continue;
		}
		return csvAttachment;

	}

	/**
	 * Method parses issue model, and creates collection with formatted comments
	 * 
	 * @param model
	 * @return
	 */
	private Collection<String> getComments(IssueModel model) {
		List<CommentModel> comments = model.getComments();
		if (comments.size() > maxComments) {
			throw new IllegalStateException(MessageFormat.format(
					"Issue #{0} has {1} comments, but limit is {2}. Please, increase comments limit.", model.getId(),
					comments.size(), maxComments));
		}

		List<String> csvComments = new ArrayList<String>(maxComments);

		boolean hasComments = CollectionUtils.isNotEmpty(comments);
		if (hasComments) {
			for (CommentModel comment : comments) {
				String text = ConvertUtil.replaceMantisLinks(comment.getText());
				String user = usersMapper.getJiraUsername(comment.getUserName());
				String date = dateFormat.format(comment.getCreated());
				csvComments.add(MessageFormat.format("Comment: {0} : {1} :\n\n {2}", user, date, text));
			}
		}

		/*
		 * Here we add empty comments for other comment columns
		 */
		for (int i = comments.size(); i < maxComments; i++) {
			csvComments.add(StringUtils.EMPTY);
			continue;
		}
		return csvComments;

	}

	/**
	 * Method looks through references, and if model is "child of", returns
	 * parent id
	 * 
	 * @param model
	 * @return
	 */
	private String getParentId(IssueModel model) {
		List<ReferenceModel> references = model.getReferences();
		if (CollectionUtils.isEmpty(references)) {
			return StringUtils.EMPTY;
		}

		for (ReferenceModel referenceModel : references) {
			if (!ReferenceType.CHILD_OF.equals(referenceModel.getType())) {
				continue;
			}
			return ConvertUtil.convertIdToKey(referenceModel.getTargetId());
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Method creates tag string
	 * 
	 * @param model
	 * @return
	 */
	private String getTags(IssueModel model) {
		List<String> tags = model.getTags();
		if (CollectionUtils.isEmpty(tags)) {
			return StringUtils.EMPTY;
		}
		StringBuilder string = new StringBuilder();
		for (String tag : tags) {
			if (string.length() > 0) {
				string.append(" ");
			}
			string.append(StringUtils.replaceChars(tag, ' ', '-'));
		}
		return string.toString();
	}

	private String getType(IssueModel model) {
		IssueType type = model.getType();
		if (type == null) {
			return StringUtils.EMPTY;
		}
		return type.name();
	}

	private String getDate(Date date) {
		if (date == null) {
			return StringUtils.EMPTY;
		}
		return dateFormat.format(date);
	}
}
