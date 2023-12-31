package org.fog.placement;

import java.util.HashMap;
import java.util.Map;

public class ModuleMapping {
	/**
	 * Mapping from node name to list of <moduleName, numInstances> of instances to be launched on node
	 */
	protected Map<String, Map<String, Integer>> nodeToModuleCountMapping;
	
	public static ModuleMapping createModuleMapping(){
		return new ModuleMapping();
	}

	public Map<String, Map<String, Integer>> getNodeToModuleCountMapping() {
		return nodeToModuleCountMapping;
	}
	
	public void setNodeToModuleCountMapping(Map<String, Map<String, Integer>> nodeToModuleCountMapping) {
		this.nodeToModuleCountMapping = nodeToModuleCountMapping;
	}

	protected ModuleMapping(){
		setNodeToModuleCountMapping(new HashMap<String, Map<String, Integer>>());
	}
	
	/**
	 * Add 1 instance of module moduleName to device deviceName
	 * @param moduleName
	 * @param deviceName
	 */
	public void addModuleToDevice(String moduleName, String deviceName){
		addModuleToDevice(moduleName, deviceName, 1);
	}
	
	/**
	 * Add <b>instanceCount</b> number of instances of module <b>moduleName</b> to <b>device deviceName</b>
	 * @param moduleName
	 * @param deviceName
	 * @param instanceCount
	 */
	public void addModuleToDevice(String moduleName, String deviceName, int instanceCount){
		if(!getNodeToModuleCountMapping().containsKey(deviceName))
			getNodeToModuleCountMapping().put(deviceName, new HashMap<String, Integer>());
		if(!getNodeToModuleCountMapping().get(deviceName).containsKey(moduleName))
			getNodeToModuleCountMapping().get(deviceName).put(moduleName, instanceCount);
	}
	
}
