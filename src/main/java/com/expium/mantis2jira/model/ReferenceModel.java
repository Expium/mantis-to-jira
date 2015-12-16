package com.expium.mantis2jira.model;

public class ReferenceModel {

	public enum ReferenceType {
		DUPLICATE_OF, CHILD_OF, PARENT_OF, RELATED_TO, HAS_DUPLICATE
	}

	private Integer ownerId;
	private Integer targetId;
	private ReferenceType type;

	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	public ReferenceType getType() {
		return type;
	}

	public void setType(ReferenceType type) {
		this.type = type;
	}

	public Integer getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		if (ownerId != null && targetId != null) {
			result = prime * result + (ownerId.hashCode() + targetId.hashCode());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReferenceModel other = (ReferenceModel) obj;
		if (type != other.type)
			return false;
		if (type == ReferenceType.RELATED_TO) {
			if (ownerId.equals(other.ownerId) && targetId.equals(other.targetId)) {
				return true;
			} else if (ownerId.equals(other.targetId) && targetId.equals(other.ownerId)) {
				return true;
			}
			return false;
		}
		return true;
	}
}
