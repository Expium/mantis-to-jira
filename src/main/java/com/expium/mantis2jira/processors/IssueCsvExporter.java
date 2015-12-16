package com.expium.mantis2jira.processors;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import com.expium.mantis2jira.jira.csv.IssueCsvGenerator;
import com.expium.mantis2jira.jira.csv.ReferencesCsvGenerator;
import com.expium.mantis2jira.mantis.MantisClient;
import com.expium.mantis2jira.mantis.MantisClientImpl;
import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.util.PropertiesUtil;
import com.expium.mantis2jira.util.UsersMapper;

public class IssueCsvExporter extends Exporter {
	protected MantisClient mantisClient;
	protected UsersMapper usersMapper;

	public IssueCsvExporter(UsersMapper usersMapper) {
		init();
		this.usersMapper = usersMapper;
	}

	protected void init() {
		mantisClient = new MantisClientImpl(PropertiesUtil.getMantisUrl(), PropertiesUtil.getMantisUsername(),
				PropertiesUtil.getMantisPassword());
	}

	@Override
	public void startExport() {
		if (!mantisClient.isReady()) {
			// SOmething wrong with mantis client
			return;
		}
		String mantisProjectName = PropertiesUtil.getMantisProjectName();
		Integer projectId = mantisClient.getProjectId(mantisProjectName);
		if (projectId == 0) {
			log.error("Project not found in mantis BT");
			return;
		}
		IssueCsvGenerator issueExport = new IssueCsvGenerator(PropertiesUtil.getCommentsLimit(),
				PropertiesUtil.getAttachmentsLimit(), PropertiesUtil.getCsvFilename(), usersMapper);
		ReferencesCsvGenerator referencesExport = new ReferencesCsvGenerator(PropertiesUtil.getRefFilename());
		
		Iterable<IssueModel> issues = mantisClient.getIssues(projectId, true, PropertiesUtil.getIssueIdLimit());
	
		for (IssueModel issueModel : issues) {
			issueExport.addIssue(issueModel);
			referencesExport.addIssue(issueModel);
		}
		
		issueExport.close();
		referencesExport.close();
		
		saveUsersStats();
	}

	private void saveUsersStats() {
		StringBuilder stats = new StringBuilder();
		Map<String, Integer> usersStats = usersMapper.getUsersStats();
		for (String username : usersStats.keySet()) {
			if (stats.length() > 0) {
				stats.append("\n");
			}
			stats.append(MessageFormat.format("      {0, number, #} {1}", usersStats.get(username), username));
		}
		try {
			String statsFilename = PropertiesUtil.getStatsFilename();
			FileWriter writer = new FileWriter(statsFilename);
			writer.write(stats.toString());
			writer.flush();
			writer.close();
			log.info("Users stats succesfully saved to '{}'", statsFilename);
		} catch (IOException e) {
			log.info("Cannot write user stats file", e);
		}

	}
}
