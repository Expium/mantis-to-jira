package com.expium.mantis2jira.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.Mantis2JiraConverter;

public class PropertiesUtil {
	final static Logger log = LoggerFactory.getLogger(Mantis2JiraConverter.LOGER_NAME);
	
	private static final String ISSUE_ID_LIMIT = "issue.limit.id";
	private static final String DATE_FORMAT = "issue.date";

	private static final String MANTIS_PROJECT_NAME = "mantis.project.name";
	private static final String MANTIS_URL = "mantis.url";
	private static final String MANTIS_USERNAME = "mantis.username";
	private static final String MANTIS_PASSWORD = "mantis.password";
	private static final String MANTIS_DOWNLOAD_URL = "mantis.downloadUrl";

	private static final String JIRA_PROJECT_KEY = "jira.project.key";
	private static final String JIRA_URL = "jira.url";
	private static final String JIRA_USERNAME = "jira.username";
	private static final String JIRA_PASSWORD = "jira.password";

	private final static String USER_DEFAULT_PROPERTY = "user.default";
	private final static String USER_UNASSIGNED_PROPERTY = "user.unassigned";
	
	
	private static final String PLACEHOLDER_SUMMARY = "placeholder.summary";
	private static final String PLACEHOLDER_DESCRIPTION = "placeholder.description";

	private static final String CSV_FILENAME = "csv.filename";
	private static final String REF_FILENAME = "csv.ref.filename";
	private static final String CSV_COMMENTS_LIMIT = "csv.limit.comments";
	private static final String CSV_ATTACHMENTS_LIMIT = "csv.limit.attachments";
	private static final String STATS_FILENAME = "csv.stats.filename";

	private static final String PROPERTIES_FILE = "config.properties";
	
	private static Properties properties;

	// Only static stuff
	private PropertiesUtil() {
	}

	public static void loadProperties(String file) throws FileNotFoundException, IOException {
		properties = new Properties();
		properties.load(new FileInputStream(file));
	}

	public static void loadProperties() throws FileNotFoundException, IOException {
		loadProperties(PROPERTIES_FILE);
	}

	public static String getStringProperty(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			return StringUtils.EMPTY;
		}
		return value;

	}

	private static int getIntegerProperty(String key) {
		String value = getStringProperty(key);
		if (!StringUtils.isEmpty(value)) {
			return Integer.parseInt(value);
		}
		return 0;
	}

	public static String getMantisUrl() {
		return getStringProperty(MANTIS_URL);
	}

	public static String getMantisPassword() {
		return getStringProperty(MANTIS_PASSWORD);
	}

	public static String getMantisUsername() {
		return getStringProperty(MANTIS_USERNAME);
	}

	public static String getMantisProjectName() {
		return getStringProperty(MANTIS_PROJECT_NAME);
	}

	public static String getJiraProjectKey() {
		return getStringProperty(JIRA_PROJECT_KEY);
	}

	public static String getJiraUrl() {
		return getStringProperty(JIRA_URL);
	}

	public static String getJiraUser() {
		return getStringProperty(JIRA_USERNAME);
	}

	public static String getJiraPassword() {
		return getStringProperty(JIRA_PASSWORD);
	}

	public static String getPlaceholderSummary() {
		return getStringProperty(PLACEHOLDER_SUMMARY);
	}

	public static String getPlaceholderDescription() {
		return getStringProperty(PLACEHOLDER_DESCRIPTION);
	}

	public static String getCsvFilename() {
		return getStringProperty(CSV_FILENAME);
	}
	public static String getRefFilename() {
		return getStringProperty(REF_FILENAME);
	}
	public static String getStatsFilename() {
		return getStringProperty(STATS_FILENAME);
	}
	
	public static int getIssueIdLimit() {
		return getIntegerProperty(ISSUE_ID_LIMIT);
	}

	public static int getCommentsLimit() {
		return getIntegerProperty(CSV_COMMENTS_LIMIT);
	}

	public static int getAttachmentsLimit() {
		return getIntegerProperty(CSV_ATTACHMENTS_LIMIT);
	}

	public static String getMantisDownloadUrl() {
		return getStringProperty(MANTIS_DOWNLOAD_URL);
	}
	public static String getDefaultUser(){
		return getStringProperty(USER_DEFAULT_PROPERTY);
	}
	public static String getUnassignedUser(){
		return getStringProperty(USER_UNASSIGNED_PROPERTY);
	}
}
