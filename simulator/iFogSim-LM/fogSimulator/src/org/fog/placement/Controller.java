package org.fog.placement;

import java.util.*;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.ModuleLaunchConfig;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;
import org.fog.utils.estatisticas.*;

public class Controller extends SimEntity{

	public static boolean ONLY_CLOUD = false;

	private List<FogDevice> fogDevices;
	private List<Sensor> sensors;
	private List<Actuator> actuators;

	private Map<String, Application> applications;
	private Map<String, Integer> appLaunchDelays;
	private ModuleMapping moduleMapping;
	private Map<Integer, Double> globalCurrentCpuLoad;
	private Map<Integer, Double> globalCurrentCpuLoadRate;

	private Map<Integer, Integer> deviceIdToVSOTModuleCount;
	private Map<Integer, Integer> deviceIdToEEGModuleCount;
	private Map<Integer, Integer> deviceIdToSWWModuleCount;

	private boolean debugMode;

	private List<StatisticsCollector> statisticsCollectors;
	private Map<String, Integer> appStopDelays;

	public Controller(String name, List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, ModuleMapping moduleMapping, boolean debugMode) {
		super(name);
		this.applications = new HashMap<String, Application>();
		this.globalCurrentCpuLoad = new HashMap <Integer, Double>();
		this.globalCurrentCpuLoadRate = new HashMap <Integer, Double>();
		this.deviceIdToVSOTModuleCount = new HashMap <Integer, Integer>();
		this.deviceIdToEEGModuleCount = new HashMap <Integer, Integer>();
		this.deviceIdToSWWModuleCount = new HashMap <Integer, Integer>();
		this.debugMode = debugMode;
		setAppLaunchDelays(new HashMap<String, Integer>());
		setModuleMapping(moduleMapping);
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
		setActuators(actuators);
		setSensors(sensors);
		connectWithLatencies();
		initializeCPULoads();
		appStopDelays = new HashMap<>();
	}

	public void setStatisticsCollectors(List<StatisticsCollector> statisticsCollectors) {
		this.statisticsCollectors = statisticsCollectors;
	}

	private void initializeCPULoads() {
		for(FogDevice device : getFogDevices()){
			this.globalCurrentCpuLoad.put(device.getId(), 0.0);
			this.globalCurrentCpuLoadRate.put(device.getId(), 0.0);
		}

	}

	private FogDevice getFogDeviceById(int id){
		for(FogDevice fogDevice : getFogDevices()){
			if(id==fogDevice.getId())
				return fogDevice;
		}
		return null;
	}

	private void connectWithLatencies(){
		for(FogDevice fogDevice : getFogDevices()){
			FogDevice parent = getFogDeviceById(fogDevice.getParentId());
			if(parent == null)
				continue;
			double latency = fogDevice.getUplinkLatency();
			parent.getChildToLatencyMap().put(fogDevice.getId(), latency);
			parent.getChildrenIds().add(fogDevice.getId());
		}
	}

	@Override
	public void startEntity() {
		for(String appId : applications.keySet()){
			if(getAppLaunchDelays().get(appId)==0)
				processAppSubmit(applications.get(appId));
			else
				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, applications.get(appId));

			if(appStopDelays.containsKey(appId)){
				int start = 0;
				if(getAppLaunchDelays().get(appId)!=0){
					start = getAppLaunchDelays().get(appId);
				}
				send(getId(), start+appStopDelays.get(appId), FogEvents.APP_STOP, getApplications().get(appId));
			}
		}

		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);

		send(getId(), Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);

		for(FogDevice dev : getFogDevices())
			sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);

		send(getId(), 1, FogEvents.CHECK_STATISTICS);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
			case FogEvents.APP_SUBMIT:
				processAppSubmit(ev);
				break;
			case FogEvents.APP_STOP:
				stopApplication(ev);
				break;
			case FogEvents.TUPLE_FINISHED:
				processTupleFinished(ev);
				break;
			case FogEvents.CONTROLLER_RESOURCE_MANAGE:
				manageResources();
				break;
			case FogEvents.CHECK_STATISTICS:
				for(StatisticsCollector c: statisticsCollectors)
					c.computeStatistics();
				send(getId(), 1, FogEvents.CHECK_STATISTICS);
				break;
			case FogEvents.STOP_SIMULATION:
				CloudSim.stopSimulation();
				printTimeDetails();
				printModuleCountDetails();
//			printPowerDetails();
//			printCostDetails();
				printNetworkUsageDetails();
				for(StatisticsCollector c: statisticsCollectors)
					c.printMyStatistics();
				System.exit(0);
				break;

		}
	}

	private void printNetworkUsageDetails() {
		System.out.println("["+ CloudSim.clock() +"] Total network usage = "+NetworkUsageMonitor.getNetworkUsage()/Config.MAX_SIMULATION_TIME);
		System.out.println("-----------------------End------------------------");
	}

	private FogDevice getCloud(){
		for(FogDevice dev : getFogDevices())
			if(dev.getName().equals("cloud"))
				return dev;
		return null;
	}

	private void printCostDetails(){
		if (this.debugMode) {
			System.out.println("Cost of execution in cloud = "+getCloud().getTotalCost());
		}
	}
	private void printPowerDetails() {
		// TODO Auto-generated method stub
		if (this.debugMode) {
			for(FogDevice fogDevice : getFogDevices()){
				System.out.println(fogDevice.getName() + " : Energy Consumed = "+fogDevice.getEnergyConsumption());
			}
		}
	}

	public String getStringForLoopId(int loopId){
		for(String appId : getApplications().keySet()){
			Application app = getApplications().get(appId);
			for(AppLoop loop : app.getLoops()){
				if(loop.getLoopId() == loopId)
					return String.join(" | ", loop.getModules());
			}
		}
		return null;
	}

	private void printModuleCountDetails() {
		System.out.println("\n------------ Module Count By Device ------------");

		for (Integer key: deviceIdToVSOTModuleCount.keySet()) {
			String fogDeviceName = getFogDeviceById(key).getName();
			if (!fogDeviceName.contains("m-") && (fogDeviceName.contains("cloudlet-1") || fogDeviceName == "cloud")) {
				System.out.println("DCNS/" + fogDeviceName + ":" + deviceIdToVSOTModuleCount.get(key));
			}
		}

		for (Integer key: deviceIdToEEGModuleCount.keySet()) {
			String fogDeviceName = getFogDeviceById(key).getName();
			if (!fogDeviceName.contains("m-") && (fogDeviceName.contains("cloudlet-1") || fogDeviceName == "cloud")) {
				System.out.println("VRGame/" + fogDeviceName + ":" + deviceIdToEEGModuleCount.get(key));
			}
		}

		for (Integer key: deviceIdToSWWModuleCount.keySet()) {
			String fogDeviceName = getFogDeviceById(key).getName();
			if (!fogDeviceName.contains("m-") && (fogDeviceName.contains("cloudlet-1") || fogDeviceName == "cloud")) {
				System.out.println("SWM/" + fogDeviceName + ":" + deviceIdToSWWModuleCount.get(key));
			}
		}
		System.out.println("------------ Module Count By Device ------------\n");
		System.out.println("EXECUTION TIME : "+ (Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime()));
	}

	private void printTimeDetails() {
		if (this.debugMode) {
			System.out.println();
			System.out.println("=========================================");
			System.out.println("============== RESULTS ==================");
			System.out.println("=========================================");
			System.out.println();
			System.out.println("=========================================");
			System.out.println("EXECUTION TIME : "+ (Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime()));
			System.out.println("=========================================");
			System.out.println();
			System.out.println("=========================================");
			System.out.println("APPLICATION LOOP DELAYS");
			System.out.println("=========================================");
		}
		for(Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()){
			System.out.println(getStringForLoopId(loopId) + " ---> "+TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));
		}
	}

	protected void manageResources(){
		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}

	private void processTupleFinished(SimEvent ev) {
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub

	}

	public void submitApplication(Application application, int delay){
		this.submitApplication(application, delay, -1);
	}

	public void submitApplication(Application application, int delay, int stopDelay){
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);

		if(stopDelay>0){
			appStopDelays.put(application.getAppId(), stopDelay);
		}

		for(Sensor sensor : sensors){
			sensor.setApp(getApplications().get(sensor.getAppId()));
		}
		for(Actuator ac : actuators){
			ac.setApp(getApplications().get(ac.getAppId()));
		}

		for(AppEdge edge : application.getEdges()) {
			if (edge.getEdgeType() == AppEdge.ACTUATOR) {
				String moduleName = edge.getSource();
				for (Actuator actuator : getActuators()) {
					if (actuator.getActuatorType().equalsIgnoreCase(edge.getDestination()))
						application.getModuleByName(moduleName).subscribeActuator(actuator.getId(), edge.getTupleType());
				}
			}
		}
	}



	private void processAppSubmit(SimEvent ev){
		Application app = (Application) ev.getData();
		processAppSubmit(app);
	}

	protected void processAppSubmit(Application application){

		if (this.debugMode) {
			System.out.println();
			System.out.println("=================================================================");
			System.out.println("Submitted application "+ application.getAppId());
			System.out.println("=================================================================");
			System.out.println();

			System.out.println(CloudSim.clock()+" Submitted application "+ application.getAppId());
		}

		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);

		ModulePlacement modulePlacement = null;
		if(application.getPlacementStrategy().equals("Edgewards")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Edgewards...");
			}
			modulePlacement = new ModulePlacementEdgewards(getFogDevices(), getSensors(), getActuators(),
					application, getModuleMapping());
		} else if(application.getPlacementStrategy().equals("EdgewardsIndividually")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Edgewards Individually...");
			}
			modulePlacement = new ModulePlacementEdgewardsIndividually(getFogDevices(), getSensors(), getActuators(),
					application, getModuleMapping(), globalCurrentCpuLoad, this.debugMode);
		} else if(application.getPlacementStrategy().equals("ModulePlacementEdgewardsIndividually2")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Edgewards Individually...");
			}
			modulePlacement = new ModulePlacementEdgewardsIndividually2(getFogDevices(), getSensors(), getActuators(),
					application, getModuleMapping(), globalCurrentCpuLoad, applications, this.debugMode);
		} else if(application.getPlacementStrategy().equals("ModulePlacementEdgewardsCommunicationAnalysis2")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Edgewards Individually...");
			}
			modulePlacement = new ModulePlacementEdgewardsCommunicationAnalysis2(getFogDevices(), getSensors(), getActuators(), application, getModuleMapping(), globalCurrentCpuLoad,applications, this.debugMode);

		}else if(application.getPlacementStrategy().equals("ModulePlacementEdgewardsCommunicationAnalysis3")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Edgewards Individually...");
			}
			modulePlacement = new ModulePlacementEdgewardsCommunicationAnalysis3(getFogDevices(), getSensors(), getActuators(), application, getModuleMapping(), globalCurrentCpuLoad,applications, this.debugMode);

		} else if(application.getPlacementStrategy().equals("ModulePlacementEdgewardsCommunicationAnalysis4")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Edgewards Individually...");
			}
			modulePlacement = new ModulePlacementEdgewardsCommunicationAnalysis4(
					getFogDevices(),
					getSensors(),
					getActuators(),
					application,
					getModuleMapping(),
					globalCurrentCpuLoad,
					globalCurrentCpuLoadRate,
					applications,
					this.debugMode);

		}else if(application.getPlacementStrategy().equals("ModulePlacementEdgewardsCommunicationAnalysis5")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Edgewards Individually...");
			}
			modulePlacement = new ModulePlacementEdgewardsCommunicationAnalysis5(
					getFogDevices(),
					getSensors(),
					getActuators(),
					application,
					getModuleMapping(),
					globalCurrentCpuLoad,
					globalCurrentCpuLoadRate,
					applications,
					this.debugMode);

		} else if(application.getPlacementStrategy().equals("Mapping")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Mapping...");
			}
			modulePlacement = new ModulePlacementMapping(getFogDevices(), application, getModuleMapping(), globalCurrentCpuLoad, this.debugMode);
		} else if(application.getPlacementStrategy().equals("EdgewardsWithCommunicationAnalysis")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using Edgewards With Communication Analysis...");
			}
			modulePlacement = new ModulePlacementEdgewardsCommunicationAnalysis(getFogDevices(), getSensors(), getActuators(), application, getModuleMapping(), globalCurrentCpuLoad, this.debugMode);
		} else if(application.getPlacementStrategy().equals("MyModulePlacement")) {
			if (this.debugMode) {
				System.out.println("Placing application " + application.getAppId() + " using MyModulePlacement");
			}
			modulePlacement = new ModulePlacementOnlyCloud2(getFogDevices(), getSensors(), getActuators(), new ArrayList<>(applications.values()));
		}

		//for (Application app : applications.values()) {
			for(FogDevice fogDevice : fogDevices){
				sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);
			}
		//}


		Map<Integer, List<AppModule>> deviceToModuleMap = modulePlacement.getDeviceToModuleMap();
		Map<Integer, Map<String, Integer>> instanceCountMap = modulePlacement.getModuleInstanceCountMap();
		Map<Integer, Map<String, List<Integer>>> instanceListMap = modulePlacement.getModuleInstanceListMap();
		Map<Integer, Map<String, List<Integer>>> downstreamInstanceCountMap =
				modulePlacement.getModuleDownstreamInstances();

		if(debugMode) {
			System.out.println("instanceCountMap: " + instanceCountMap);
		}

		for (Integer key : instanceCountMap.keySet() ) {
			final Map<String, Integer> moduleNameToCountMap = instanceCountMap.get(key);

			for (String name: moduleNameToCountMap.keySet()) {

				if (Arrays.asList("object_tracker", "object_detector", "motion_detector").contains(name)) {
					int currentCount = deviceIdToVSOTModuleCount.getOrDefault(key, 0);

					currentCount += moduleNameToCountMap.get(name);
					deviceIdToVSOTModuleCount.put(key, currentCount);
				} else if (Arrays.asList("concentration_calculator_1", "client_1", "connector_1").contains(name)) {
					int currentCount = deviceIdToEEGModuleCount.getOrDefault(key, 0);

					currentCount += moduleNameToCountMap.get(name);
					deviceIdToEEGModuleCount.put(key, currentCount);
				} else if (Arrays.asList("data_aggregator", "leak_detector", "actuator_controller").contains(name)) {
					int currentCount = deviceIdToSWWModuleCount.getOrDefault(key, 0);

					currentCount += moduleNameToCountMap.get(name);
					deviceIdToSWWModuleCount.put(key, currentCount);
				}
			}
		}

		if (debugMode) {
			System.out.println("=================================================================");
			System.out.println("instance map");
			System.out.println("=================================================================");
		}
		for(Integer deviceId : deviceToModuleMap.keySet()){
			if (debugMode) {
				System.out.print("device " + getFogDeviceById(deviceId).getName() + ": ");
			}
			for(AppModule module : deviceToModuleMap.get(deviceId)){
//				if (!module.getAppId().equals(application.getAppId())) {
//					continue;
//				}
				sendNow(deviceId, FogEvents.APP_SUBMIT, applications.get(module.getAppId()));
				sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);

				int instanceCount = instanceCountMap.get(deviceId).getOrDefault(module.getName(),0);
				List<Integer> downstreamInstanceList = new ArrayList<>();
				if (downstreamInstanceCountMap.containsKey(deviceId)
						&& downstreamInstanceCountMap.get(deviceId).containsKey(module.getName())) {
					downstreamInstanceList = downstreamInstanceCountMap.get(deviceId).get(module.getName());
				}
				ModuleLaunchConfig config = new ModuleLaunchConfig(module, instanceCount, downstreamInstanceList);
				sendNow(deviceId, FogEvents.LAUNCH_MODULE_INSTANCE, config);

				if (debugMode) {
					System.out.print(module.getName() + " (" + instanceCount + "), ");
				}
			}
			if (debugMode) {
				System.out.println();
			}
		}
	}

	private void stopApplication(SimEvent ev){
		Application app = (Application)  ev.getData();
		//	runningApps.remove(app);
		stopApplication(app);
	}

	private void stopApplication(Application app){
		for(FogDevice device: this.getFogDevices()){
			Iterator<Vm> iteratorVM =  device.getVmList().iterator();

			while(iteratorVM.hasNext()){
				AppModule module = (AppModule) iteratorVM.next();
				if(module.getAppId().equals(app.getAppId()) && !device.getName().startsWith("m-")){
					ModuleLaunchConfig config = new ModuleLaunchConfig(module, 0, Collections.emptyList());
					sendNow(device.getId(), FogEvents.LAUNCH_MODULE_INSTANCE, config);
					sendNow(device.getId(), FogEvents.RELEASE_MODULE, module);
					iteratorVM.remove();
				}
			}


			//Map<String, Integer> modulesCount = device.getModuleInstanceCount().get(app.getAppId());

			/*if(modulesCount!=null) {
				for (String module : modulesCount.keySet()) {
					modulesCount.put(module, 0);
				}
			}*/

			//device.getModuleInstanceCount().remove(app.getAppId());
		}

		/*
		//Stop Sensor
		for(Sensor sensor : sensors){
			if(sensor.getApp() == app){
				sensor.setApp(null); //NullPoint no transmit
			}
		}

		//Stop Actuator
		for(Actuator actuator : actuators){
			if(actuator.getApp() == app){
				actuator.setApp(null); //NullPoint no transmit
			}
		}*/

		//remove from modulesMap
        /*Map<String, Map<String, Integer>> nodeToModuleCountMapping = getModuleMapping().getNodeToModuleCountMapping();
        List<String> modulesNames = app.getModules().stream()
                .map(appModule -> appModule.getName())
                .collect(Collectors.toList());

        for(Map.Entry<String, Map<String, Integer>> devicesModules : nodeToModuleCountMapping.entrySet()){
            String deviceName = devicesModules.getKey();
            Map<String, Integer> modules = devicesModules.getValue();
            Iterator<Map.Entry<String, Integer>> iterator = modules.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String, Integer> module = iterator.next();
                if(modulesNames.contains(module.getKey())){
                    //remove from controller
                    iterator.remove();
                }
            }
        }*/


		//remove mapping

		//AddCode to Remove application
		//FogUtils.appIdToGeoCoverageMap.remove(app.getAppId());
		//getApplications().remove(app.getAppId());
	}

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Integer> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Integer> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}

	public Map<String, Application> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, Application> applications) {
		this.applications = applications;
	}
	public ModuleMapping getModuleMapping() {
		return moduleMapping;
	}
	public void setModuleMapping(ModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		for(Sensor sensor : sensors)
			sensor.setControllerId(getId());
		this.sensors = sensors;
	}

	public List<Actuator> getActuators() {
		return actuators;
	}

	public void setActuators(List<Actuator> actuators) {
		this.actuators = actuators;
	}

	public Map<Integer, Double> getGlobalCPULoad() {
		return globalCurrentCpuLoad;
	}

	public void setGlobalCPULoad(Map<Integer, Double> currentCpuLoad) {
		for(FogDevice device : getFogDevices()){
			this.globalCurrentCpuLoad.put(device.getId(), currentCpuLoad.get(device.getId()));
		}
	}
}
