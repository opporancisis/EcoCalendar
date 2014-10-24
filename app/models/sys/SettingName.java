package models.sys;

public enum SettingName {
	PORTAL_TITLE(String.class),

	DELETED_USER_ID(Long.class, false);

	private String clazz;

	private boolean editable;

	private SettingName(Class<?> clazz) {
		this(clazz, true);
	}

	private SettingName(Class<?> clazz, boolean editable) {
		this.editable = editable;
		this.clazz = clazz.getName();
	}

	public String getClazz() {
		return clazz;
	}

	public boolean isEditable() {
		return editable;
	}

}
