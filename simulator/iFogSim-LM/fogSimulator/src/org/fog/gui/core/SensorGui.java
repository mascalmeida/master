package org.fog.gui.core;

import java.io.Serializable;

import org.fog.entities.Sensor;
import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.utils.distribution.Distribution;
import org.fog.utils.distribution.NormalDistribution;
import org.fog.utils.distribution.UniformDistribution;

public class SensorGui extends Node<Sensor> implements Serializable{

	private static final long serialVersionUID = 4087896123649020073L;

	private String name;
	private String sensorType;
	private int userId;
	private String appId;
	private int appInstance;
	
	private Distribution distribution;
	
	public SensorGui(String name, String type, Distribution distribution, int userId, String appId, int appInstance){
		super(name, "SENSOR");
		this.userId = userId;
		this.appId = appId;
		this.appInstance = appInstance;
		setName(name);
		setSensorType(type);
		setDistribution(distribution);
	}

	public SensorGui(String name, String sensorType, String selectedItem, double normalMean_,
			double normalStdDev_, double uniformLow_, double uniformUp_,
			double deterministicVal_) {
		super(name, "SENSOR");
		setName(name);
		setSensorType(sensorType);
		if(normalMean_ != -1){
			setDistribution(new NormalDistribution(normalMean_, normalStdDev_));
		}else if(uniformLow_ != -1){
			setDistribution(new UniformDistribution(uniformLow_, uniformUp_));
		}else if(deterministicVal_ != -1){
			setDistribution(new DeterministicDistribution(deterministicVal_));
		}
	}

	public int getDistributionType(){
		return distribution.getDistributionType();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Distribution getDistribution() {
		return distribution;
	}

	public void setDistribution(Distribution distribution) {
		this.distribution = distribution;
	}
	
	@Override
	public String toString() {
		
		if(distribution instanceof NormalDistribution)
			return "Sensor [dist=1 mean=" + ((NormalDistribution)distribution).getMean() + " stdDev=" + ((NormalDistribution)distribution).getStdDev() + "]";
		else if(distribution instanceof UniformDistribution)
			return "Sensor [dist=2 min=" + ((UniformDistribution)distribution).getMin() + " max=" + ((UniformDistribution)distribution).getMax() + "]";
		else if(distribution instanceof DeterministicDistribution)
			return "Sensor [dist=3 value=" + ((DeterministicDistribution)distribution).getValue() + "]";
		else
			return "";
	}

	@Override
	public Sensor convertToSimEntity() {
		Sensor sensor = new Sensor(
				getName(),
				this.getSensorType(),
				this.getUserId(),
				this.getAppId(),
				this.getDistribution(),
				this.getAppInstance());

		return sensor;
	}

	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}

	public int getUserId() {
		return userId;
	}

	public String getAppId() {
		return appId;
	}

	public int getAppInstance() {
		return appInstance;
	}
}
