package org.fog.utils;

import org.fog.application.AppModule;

import java.util.List;

public class ModuleLaunchConfig {

	private AppModule module;
	private int instanceCount;

	private List<Integer> downstreamInstanceList;
	
	public ModuleLaunchConfig(AppModule module, int instanceCount, List<Integer> downstreamInstanceList){
		setModule(module);
		setInstanceCount(instanceCount);
		setDownstreamInstanceList(downstreamInstanceList);
	}
	
	public AppModule getModule() {
		return module;
	}
	public void setModule(AppModule module) {
		this.module = module;
	}
	public int getInstanceCount() { return instanceCount; }
	public void setInstanceCount(int instanceCount) {
		this.instanceCount = instanceCount;
	}

	public List<Integer> getDownstreamInstanceList() {
		return downstreamInstanceList;
	}

	public void setDownstreamInstanceList(List<Integer> downstreamInstanceList) {
		this.downstreamInstanceList = downstreamInstanceList;
	}
	
}
