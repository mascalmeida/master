package org.fog.gui.core;

import org.fog.entities.Actuator;
import org.fog.entities.Sensor;

import java.io.Serializable;

public class ActuatorGui extends Node<Actuator> implements Serializable{

	private static final long serialVersionUID = 4087896123649020073L;

	private String name;
	private String actuatorType;
	private String appId;
	private int userId;
	
	public ActuatorGui(String name, String actuatorType, String appId, int userId){
		super(name, "ACTUATOR");
		setName(name);
		setActuatorType(actuatorType);
		setAppId(appId);
		setUserId(userId);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Actuator []";
	}

	@Override
	public Actuator convertToSimEntity() {
		Actuator actuator = new Actuator(getName(), this.getUserId(), this.getAppId(), getActuatorType());
		return actuator;
	}

	public String getActuatorType() {
		return actuatorType;
	}

	public void setActuatorType(String actuatorType) {
		this.actuatorType = actuatorType;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}
