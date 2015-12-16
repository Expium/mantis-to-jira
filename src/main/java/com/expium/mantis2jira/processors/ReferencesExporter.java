package com.expium.mantis2jira.processors;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.jira.JiraClient;
import com.expium.mantis2jira.jira.converter.ConvertUtil;
import com.expium.mantis2jira.jira.csv.ReferencesCsvReader;
import com.expium.mantis2jira.jira.rest.JiraClientImpl;
import com.expium.mantis2jira.model.ReferenceModel;
import com.expium.mantis2jira.model.ReferenceModel.ReferenceType;
import com.expium.mantis2jira.util.PropertiesUtil;

public class ReferencesExporter extends Exporter {
	public static final String LOGER_NAME = "REF";
	final static Logger log = LoggerFactory.getLogger(LOGER_NAME);
	private static List<ReferenceType> ignoredTypes = Arrays.asList(ReferenceType.CHILD_OF, ReferenceType.PARENT_OF);

	protected JiraClient jiraClient;
	private String csvFilename;

	public ReferencesExporter(String csvFilename) {
		this.csvFilename = csvFilename;
		init();
	}

	protected void init() {
		jiraClient = new JiraClientImpl(PropertiesUtil.getJiraUrl(), PropertiesUtil.getJiraUser(),
				PropertiesUtil.getJiraPassword());
	}

	@Override
	public void startExport() {
		log.info("Exporting references from csv file '{}' to JIRA project '{}'", csvFilename,
				PropertiesUtil.getJiraProjectKey());
		ReferencesCsvReader reader = new ReferencesCsvReader(csvFilename);
		Collection<ReferenceModel> referenceModels = null;
		try {
			referenceModels = reader.getReferenceModels();
		} catch (IOException e) {
			log.error("Cannot read references from file {}", csvFilename);
		}
		if (CollectionUtils.isEmpty(referenceModels)) {
			log.info("There are no references to import");
			return;
		}

		int done = 0;
		int total = referenceModels.size();

		for (ReferenceModel referenceModel : referenceModels) {
			createReference(referenceModel);
			done++;
			log.info("{}/{} references processed", done, total);
		}
		log.info("References processing complete");
	}

	private void createReference(ReferenceModel model) {

		ReferenceType type = model.getType();
		if (type == null || ignoredTypes.contains(type)) {
			return;
		}

		String targetKey = ConvertUtil.convertIdToKey(model.getTargetId());
		String ownerKey = ConvertUtil.convertIdToKey(model.getOwnerId());
		log.info("Creating reference type '{}' from  '{}' to '{}'", model.getType(), ownerKey, targetKey);
		
		if (jiraClient.hasReference(model)) {
			log.info("Reference already exists. Skipping.");
			return;
		}
		jiraClient.addReference(model);

	}
}
