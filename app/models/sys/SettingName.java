package models.sys;

public enum SettingName {
	PORTAL_TITLE(String.class),

	DELETED_USER_ID(Long.class, false);

	private Class<?> clazz;

	private boolean editable;

	private String defaultVal;

	private SettingName(Class<?> clazz) {
		this(clazz, true);
	}

	private SettingName(Class<?> clazz, boolean editable) {
		this(clazz, editable, null);
	}

	private SettingName(Class<?> clazz, boolean editable, String defaultVal) {
		this.editable = editable;
		this.clazz = clazz;
		this.defaultVal = defaultVal;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public boolean isEditable() {
		return editable;
	}

	public String getDefaultVal() {
		return defaultVal;
	}
}
