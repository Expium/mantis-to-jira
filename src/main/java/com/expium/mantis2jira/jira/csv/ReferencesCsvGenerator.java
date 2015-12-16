package com.expium.mantis2jira.jira.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.jira.converter.ConvertUtil;
import com.expium.mantis2jira.jira.csv.ReferencesCsvColumns.CsvColumn;
import com.expium.mantis2jira.model.AttachmentModel;
import com.expium.mantis2jira.model.CommentModel;
import com.expium.mantis2jira.model.IssueModel;
import com.expium.mantis2jira.model.ReferenceModel;
import com.expium.mantis2jira.model.IssueModel.IssueType;
import com.expium.mantis2jira.model.ReferenceModel.ReferenceType;
import com.expium.mantis2jira.util.UsersMapper;

import au.com.bytecode.opencsv.CSVWriter;

public class ReferencesCsvGenerator {
	private static final char SEPARATOR = ',';
	public static final String LOGER_NAME = "CSV";
	final static Logger log = LoggerFactory.getLogger(LOGER_NAME);

	private List<CsvColumn> columns = new ReferencesCsvColumns();
	private String[] header;

	private CSVWriter writer;
	private String filename;


	public ReferencesCsvGenerator(String filename) {
		this.header = getHeader(columns);
		this.filename = filename;
		init();
	}

	private void init() {
		if(StringUtils.isBlank(filename)){
			throw new IllegalStateException("References filename cannot be empty");
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
		if(issueModel.isPlaceholder()){
			return;
		}
		
		List<ReferenceModel> references = issueModel.getReferences();
		if(CollectionUtils.isEmpty(references)){
			return;
		}
		log.info("Processing issue '{}' references", ConvertUtil.convertIdToKey(issueModel.getId()));

		for (ReferenceModel referenceModel : references) {
			writer.writeNext(getRow(referenceModel));
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			log.error("Error on writing to file", e);
		}
		log.info("CSV file '{}' succesfully exported", filename);
	}

	private String[] getHeader(List<CsvColumn> columns) {
		String[] header = new String[columns.size()];
		for (int i = 0; i < header.length; i++) {
			header[i] = columns.get(i).getValue();
		}
		return header;
	}

	private String[] getRow(ReferenceModel model) {
		List<String> row = new ArrayList<String>();

		for (int i = 0; i < columns.size(); i++) {
			CsvColumn column = columns.get(i);
			switch (column) {
			case FROM_ISSUE:
				row.add(String.valueOf(model.getOwnerId()));
				break;
			case TO_ISSUE:
				row.add(String.valueOf(model.getTargetId()));
				break;
			case TYPE:
				row.add(model.getType().name());
				break;
			default:
				throw new IllegalStateException(MessageFormat.format("Unknown type of column {0}", column.getValue()));
			}
		}
		return row.toArray(new String[0]); //Convert list of rows to array. String[0] is required because of generics
	}

	
}
