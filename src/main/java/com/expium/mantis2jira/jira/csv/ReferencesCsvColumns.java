package com.expium.mantis2jira.jira.csv;

import java.util.ArrayList;

import com.expium.mantis2jira.jira.csv.ReferencesCsvColumns.CsvColumn;


public class ReferencesCsvColumns extends ArrayList<CsvColumn> { //TODO immutable list?
	private static final long serialVersionUID = 1L;

	public enum CsvColumn {
		FROM_ISSUE("From"), TO_ISSUE("To"), TYPE("Type");

		private String value;

		private CsvColumn(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public ReferencesCsvColumns() {
		super();
		super.add(CsvColumn.FROM_ISSUE);
		super.add(CsvColumn.TO_ISSUE);
		super.add(CsvColumn.TYPE);
	}
}
