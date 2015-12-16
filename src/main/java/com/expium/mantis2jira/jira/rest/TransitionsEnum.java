package com.expium.mantis2jira.jira.rest;

public enum TransitionsEnum {
	// TODO May be in future move this stuff to hash table
	CLOSE_ISSUE("Close issue"), RESOLVE_ISSUE("Resolve issue"), START_PROGRESS("Start progress"), REOPEN_ISSUE(
			"Reopen issue");
	private String value;

	private TransitionsEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}