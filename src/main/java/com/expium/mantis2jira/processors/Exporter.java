package com.expium.mantis2jira.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Exporter {
	public static final String LOGER_NAME = "MAJIRA";
	protected final static Logger log = LoggerFactory.getLogger(LOGER_NAME);

	public abstract void startExport();

}
