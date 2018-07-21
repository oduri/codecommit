package com.kavi.user.dataobjects;


public class TicketDO {

	private String id;
	private String title;
	private String severity;
	private String module;
	private String description;

	public TicketDO(String title, String severity, String module, String description) {
		super();
		this.title = title;
		this.severity = severity;
		this.module = module;
		this.description = description;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}

