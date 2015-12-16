package com.expium.mantis2jira.jira.converter;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.expium.mantis2jira.model.AttachmentModel;
import com.expium.mantis2jira.model.ReferenceModel;
import com.expium.mantis2jira.model.ReferenceModel.ReferenceType;
import com.expium.mantis2jira.util.PropertiesUtil;

public class ConvertUtil {
	private static Map<ReferenceType, String> referenceTypeMap = new HashMap<ReferenceModel.ReferenceType, String>();
	static {
		referenceTypeMap.put(ReferenceType.DUPLICATE_OF, "Duplicate");
		referenceTypeMap.put(ReferenceType.RELATED_TO, "Relates");
		// Blocks
		// Cloners
	}

	public static String convertReferenceTypeToString(ReferenceType type) {
		return referenceTypeMap.get(type);
	}

	public static String convertIdToKey(Integer id) {
		if (id == null) {
			return StringUtils.EMPTY;
		}
		return PropertiesUtil.getJiraProjectKey() + "-" + id;
	}

	/**
	 * Replace mantis links with JIRA links
	 * 
	 * @param text
	 * @return
	 */
	public static String replaceMantisLinks(String text) {
		return StringUtils.replace(text, "#", PropertiesUtil.getJiraProjectKey() + "-");

	}

	public static String getAttachmentDownloadUrl(AttachmentModel model) {
		String mantisDownloadUrl = PropertiesUtil.getMantisDownloadUrl();
		if (StringUtils.isEmpty(mantisDownloadUrl)) {
			return model.getDownloadUrl();
		}
		return MessageFormat.format(mantisDownloadUrl, model.getId());
	}
}
