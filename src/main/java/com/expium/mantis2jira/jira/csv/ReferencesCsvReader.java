package com.expium.mantis2jira.jira.csv;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.jira.csv.ReferencesCsvColumns.CsvColumn;
import com.expium.mantis2jira.model.ReferenceModel;
import com.expium.mantis2jira.model.ReferenceModel.ReferenceType;

import au.com.bytecode.opencsv.CSVReader;

public class ReferencesCsvReader {
	public static final String LOGER_NAME = "CSV";
	final static Logger log = LoggerFactory.getLogger(LOGER_NAME);

	private String filename;
	private CSVReader reader;
	private List<CsvColumn> columns = new ReferencesCsvColumns();

	public ReferencesCsvReader(String filename) {
		this.filename = filename;
		init();
	}

	private void init() {
		if (StringUtils.isBlank(filename)) {
			throw new IllegalStateException("References filename cannot be blank");
		}
		try {
			reader = new CSVReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			log.error("Cannot read references file {}", filename);
		}
	}

	private ReferenceModel parseRow(String[] row) {
		if (ArrayUtils.isEmpty(row)) {
			return null;
		}
		ReferenceModel model = new ReferenceModel();
		for (int i = 0; i < columns.size(); i++) {
			CsvColumn column = columns.get(i);
			switch (column) {
			case FROM_ISSUE:
				model.setOwnerId(Integer.parseInt(row[i]));
				break;
			case TO_ISSUE:
				model.setTargetId(Integer.parseInt(row[i]));
				break;
			case TYPE:
				model.setType(ReferenceType.valueOf(row[i]));
				break;
			default:
				throw new IllegalStateException(MessageFormat.format("Unknown type of column {0}", column.getValue()));
			}
		}
		return model;
	}

	public Collection<ReferenceModel> removeDuplicates(Collection<ReferenceModel> models) {
		List<ReferenceModel> references = new ArrayList<ReferenceModel>(models.size() / 2);
		for (ReferenceModel reference : models) {
			if (reference.getType().equals(ReferenceType.HAS_DUPLICATE)) {
				continue;
			}
			if(references.contains(reference)){
				continue;
			}
			references.add(reference);
		}
		return references;
	}

	public Collection<ReferenceModel> getReferenceModels() throws IOException {
		reader.readNext(); // Skip header
		List<String[]> rows = reader.readAll();
		List<ReferenceModel> models = new ArrayList<ReferenceModel>(rows.size());
		for (String[] row : rows) {
			ReferenceModel model = parseRow(row);
			/*
			 * We ignore child/parent relations, because they are already
			 * imported
			 */
			ReferenceType type = model.getType();
			if (type == null) {
				continue;
			}
			if (type.equals(ReferenceType.CHILD_OF)) {
				continue;
			}
			if (type.equals(ReferenceType.PARENT_OF)) {
				continue;
			}
			models.add(model);
		}
		return removeDuplicates(models);
	}
}
