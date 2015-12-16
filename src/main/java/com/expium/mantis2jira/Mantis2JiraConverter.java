package com.expium.mantis2jira;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.processors.Exporter;
import com.expium.mantis2jira.processors.IssueCsvExporter;
import com.expium.mantis2jira.processors.ReferencesExporter;
import com.expium.mantis2jira.util.PropertiesUtil;
import com.expium.mantis2jira.util.UsersMapper;

public class Mantis2JiraConverter {
	private static final String COMMAND_LINE = "java -jar majira.jar [-conf file] -mode (csv/ref) ";
	public static final String LOGER_NAME = "MAIN";
	final static Logger log = LoggerFactory.getLogger(LOGER_NAME);

	private final static Options options = new Options();

	private static final String MODE_OPTION = "mode";
	private static final String MODE_OPTION_COMMENT = "Application mode, either csv or ref";
	private static final String MODE_OPTION_CSV = "csv";
	private static final String MODE_OPTION_REF = "ref";

	private static final String CONF_OPTION = "conf";
	private static final String CONF_OPTION_COMMENT = "Configuration file (config.properties is default)";

	private static final String USERS_OPTION = "users";
	private static final String USERS_DEFAULT_FILENAME = "users.properties";
	private static final String USERS_OPTION_COMMENT = "Username relationship configuration file (users.properties by default)";
	
	private static final String MAJIRA_PROPERTIES = "majira.properties";
	private static final String PROPERTY_VERSION = "version";
	private static String version = "";

	public static void main(String[] args) {
		getVersion();
		log.info("MAJIRA version {}", version);
		
		options.addOption(MODE_OPTION, true, MODE_OPTION_COMMENT);
		options.addOption(CONF_OPTION, true, CONF_OPTION_COMMENT);
		options.addOption(USERS_OPTION, true, USERS_OPTION_COMMENT);
		
		if (ArrayUtils.isEmpty(args)) {
			printUsage();
			return;
		}
		/*
		 * Parsing command line options
		 */
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			log.error("Cannot parse command line!", e);
			printUsage();
			return;
		}

		/*
		 * Loading config file
		 */
		String config = commandLine.getOptionValue(CONF_OPTION);
		try {
			if (StringUtils.isNotBlank(config)) {
				PropertiesUtil.loadProperties(config);
			} else {
				PropertiesUtil.loadProperties();
			}
		} catch (FileNotFoundException e) {
			log.error("Configuration file not found");
			return;
		} catch (IOException e) {
			log.error("Cannot read configuration file");
			return;
		}
		validateConfiguration();

		/*
		 * Loading users list
		 */
		String usersListFile = commandLine.getOptionValue(USERS_OPTION);
		UsersMapper usersMapper = null;
		try {
			File defaultFile = new File(USERS_DEFAULT_FILENAME);
			if (StringUtils.isNotBlank(usersListFile)) {
				usersMapper = new UsersMapper(new File(usersListFile));
			}
			else if (defaultFile.exists()){
				usersMapper = new UsersMapper(defaultFile);
			}
			else {
				usersMapper = new UsersMapper();// Empty users list
			}
			usersMapper.setDefaultUser(PropertiesUtil.getDefaultUser());
			usersMapper.setUnassignedUser(PropertiesUtil.getUnassignedUser());
			
		} catch (FileNotFoundException e) {
			log.error("File with users list not found");
			return;
		} catch (IOException e) {
			log.error("Cannot read file with users list");
			return;
		}

		/*
		 * Selecting application mode
		 */

		String mode = commandLine.getOptionValue(MODE_OPTION);
		if (StringUtils.isBlank(mode)) {
			printUsage();
			log.error("Application mode is not selected");
			return;
		}

		Exporter exporter = null;
		if (StringUtils.equalsIgnoreCase(mode, MODE_OPTION_CSV)) {
			exporter = new IssueCsvExporter(usersMapper);
		} else if (StringUtils.equalsIgnoreCase(mode, MODE_OPTION_REF)) {
			exporter = new ReferencesExporter(PropertiesUtil.getRefFilename());
		}
		if (exporter == null) {
			printUsage();
			log.error("Selected application mode is not valid");
			return;
		}
		exporter.startExport();
	}

	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(COMMAND_LINE, options);
	}

	private static void validateConfiguration() {
		String mantisProjectName = PropertiesUtil.getMantisProjectName();
		if (StringUtils.isBlank(mantisProjectName)) {
			log.error("Project name cannot be empty");
			System.exit(1);
		}
		String jiraProjectKey = PropertiesUtil.getJiraProjectKey();
		if (StringUtils.isBlank(jiraProjectKey)) {
			log.error("Project key cannot be empty");
			System.exit(1);
		}

		int issueIdLimit = PropertiesUtil.getIssueIdLimit();
		if (issueIdLimit != 0) {
			log.info("Issue id limit is set to {}", issueIdLimit);
		}
	}
	
	private static void getVersion(){
		Properties pomProperties = new Properties();
		try {
			pomProperties.load(Mantis2JiraConverter.class.getClassLoader().getResourceAsStream(MAJIRA_PROPERTIES));
		} catch (IOException e) {
			log.warn("Cannot load {} file", MAJIRA_PROPERTIES);
		}
		version = pomProperties.getProperty(PROPERTY_VERSION);
	}
}
