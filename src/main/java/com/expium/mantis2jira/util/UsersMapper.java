package com.expium.mantis2jira.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expium.mantis2jira.Mantis2JiraConverter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * This class is responsible for relationship between mantis bt usernames and
 * JIRA usernames
 *
 */
public class UsersMapper {
	final static Logger log = LoggerFactory.getLogger(Mantis2JiraConverter.LOGER_NAME);
	/*
	 * Mantis username is key, jira username is value
	 */
	private BiMap<String, String> usersMap;
	private String defaultUsername;
	private String unassignedUser = "";

	private Map<String, Integer> usersStats = new HashMap<String, Integer>();

	public UsersMapper(File file) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		usersMap = HashBiMap.create();
		for (Entry<Object, Object> entry : prop.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			usersMap.put(key, value);
		}
	}

	public UsersMapper() {
		usersMap = HashBiMap.create(1);
	}

	public void setDefaultUser(String defaultUser) {
		log.info("Default user: {}", defaultUser);
		this.defaultUsername = defaultUser;
	}
	public void setUnassignedUser(String unassignedUser) {
		log.info("Unassigned user: {}", unassignedUser);
		this.unassignedUser = unassignedUser;
	}
	public String getUnassignedUser(){
		return unassignedUser;
	}

	public String getJiraUsername(String mantisUsername) {
		if (StringUtils.isEmpty(mantisUsername) && StringUtils.isNotEmpty(defaultUsername)) {
			updateUsersStats(defaultUsername);
			return defaultUsername;
		}
		String username = usersMap.get(mantisUsername);
		if (username == null) {
			username = mantisUsername;
		}
		updateUsersStats(username);
		return username;
	}

	private void updateUsersStats(String username) {
		if (StringUtils.isEmpty(username)) {
			username = defaultUsername;
		}
		if (!getUsersStats().containsKey(username)) {
			getUsersStats().put(username, 0);
		}
		getUsersStats().put(username, getUsersStats().get(username) + 1);
	}

	public Map<String, Integer> getUsersStats() {
		return usersStats;
	}
}
