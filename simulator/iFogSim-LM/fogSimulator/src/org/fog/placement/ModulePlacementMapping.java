package org.fog.placement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;

public class ModulePlacementMapping extends ModulePlacement{

	private ModuleMapping moduleMapping;
	protected Map<Integer, Double> currentCpuLoad;

	private boolean debugMode;
	
	@Override
	protected void mapModules() {
		Map<String, Map<String, Integer>> mapping = moduleMapping.getNodeToModuleCountMapping();
		for(String deviceName : mapping.keySet()){
			FogDevice device = getDeviceByName(deviceName);
			for(String moduleName : mapping.get(deviceName).keySet()){
				AppModule module = getApplication().getModuleByName(moduleName);
				if(module == null)
					continue;
				//TODO: compute rates for CPU load of incoming edges?
				int numModules=mapping.get(deviceName).get(moduleName).intValue();
				Double currentCpuLoad = getCurrentCpuLoad().get(device.getId());
				Double newCpuLoad = currentCpuLoad + numModules * module.getMips();
				if (this.debugMode) {
					System.out.println("MAPPING " + numModules + " " + module.getName() + " on " + device.getName()
										+ " - old CPU load = "+currentCpuLoad + " new CPU load = "
										+ newCpuLoad);
				}
				// updates the device cpu load
				getCurrentCpuLoad().put(device.getId(), newCpuLoad);

				// creates an instance of the module in the device if possible
				createModuleInstanceOnDevice(module, device);
				// updates the quantity of modules in the device
				getModuleInstanceCountMap().get(device.getId()).put(moduleName, numModules);
			}
		}
	}

	public Map<Integer, Double> getCurrentCpuLoad() {
		return currentCpuLoad;
	}

	public void setCurrentCpuLoad(Map<Integer, Double> currentCpuLoad) {
		this.currentCpuLoad= currentCpuLoad;
	}
	
	public ModulePlacementMapping(List<FogDevice> fogDevices, Application application, 
			ModuleMapping moduleMapping, Map<Integer, Double> globalCPULoad, boolean debugMode){
		this.debugMode = debugMode;
		this.setFogDevices(fogDevices);
		this.setApplication(application);
		this.setModuleMapping(moduleMapping);
		setCurrentCpuLoad(globalCPULoad);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
		this.setModuleInstanceCountMap(new HashMap<Integer, Map<String, Integer>>());
		for(FogDevice device : getFogDevices())
			getModuleInstanceCountMap().put(device.getId(), new HashMap<String, Integer>());
		mapModules();
	}
	
	
	public ModuleMapping getModuleMapping() {
		return moduleMapping;
	}
	public void setModuleMapping(ModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	
}
