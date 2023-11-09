package org.fog.placement;

import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;

public class ModulePlacementEdgewardsCommunicationAnalysis extends ModulePlacement{

    protected ModuleMapping moduleMapping;
    protected List<Sensor> sensors;
    protected List<Actuator> actuators;
    protected Map<Integer, Double> currentDeviceCpuLoad;

    /**
     * Stores the current mapping of application modules to fog devices
     */
    protected Map<Integer, List<String>> currentDeviceModuleMap;
    protected Map<Integer, Map<String, Double>> currentDeviceModuleLoadMap;
    protected Map<Integer, Map<String, Integer>> currentDeviceModuleInstanceNum;

    private boolean debugMode;
    static double DCNS_TRANSMISSION_TIME = 6;
    static double EEG_TRANSMISSION_TIME = 15;

    public ModulePlacementEdgewardsCommunicationAnalysis(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators,
                                    Application application, ModuleMapping moduleMapping, Map<Integer, Double> globalCPULoad, boolean debugMode){
        System.out.println(">>>>>>>>>>>>> ALGO 5");
        this.setFogDevices(fogDevices);
        this.setSensors(sensors);
        this.setActuators(actuators);
        this.setApplication(application);
        this.setModuleMapping(moduleMapping);
        this.setCurrentDeviceCpuLoad(globalCPULoad);
        this.debugMode = debugMode;

        this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
        this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
        this.setCurrentDeviceModuleMap(new HashMap<Integer, List<String>>());
        this.setCurrentDeviceModuleLoadMap(new HashMap<Integer, Map<String, Double>>());
        this.setCurrentDeviceModuleInstanceNum(new HashMap<Integer, Map<String, Integer>>());

        for(FogDevice dev : getFogDevices()){
            getCurrentDeviceModuleLoadMap().put(dev.getId(), new HashMap<String, Double>());
            getCurrentDeviceModuleMap().put(dev.getId(), new ArrayList<String>());
            getCurrentDeviceModuleInstanceNum().put(dev.getId(), new HashMap<String, Integer>());
        }

        this.mapModules();

//		System.out.println(getCurrentDeviceModuleInstanceNum());
        this.setModuleInstanceCountMap(getCurrentDeviceModuleInstanceNum());
    }

    @Override
    protected void mapModules() {

        // get all devices
        for(String deviceName : getModuleMapping().getNodeToModuleCountMapping().keySet()){
            // get all modules for the device
            for(String moduleName : getModuleMapping().getNodeToModuleCountMapping().get(deviceName).keySet()){
                // update the current device to module map
                int deviceId = CloudSim.getEntityId(deviceName);
                getCurrentDeviceModuleMap().get(deviceId).add(moduleName);
                getCurrentDeviceModuleLoadMap().get(deviceId).put(moduleName, 0.0);
                getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, 0);
            }
        }

        // get all paths from leaves nodes to the cloud device
        List<List<Integer>> leafToRootPaths = getLeafToRootPaths();

        for(List<Integer> path : leafToRootPaths){
            placeModulesInPath(path);
        }

        for(int deviceId : getCurrentDeviceModuleMap().keySet()){
            for(String module : getCurrentDeviceModuleMap().get(deviceId)){
                List<Integer> modDevMap = getModuleToDeviceMap().get(module);
                if(modDevMap != null) {
                    if(!modDevMap.contains(deviceId)) {
                        createModuleInstanceOnDevice(getApplication().getModuleByName(module), getFogDeviceById(deviceId));
                    }
                }
                else
                    createModuleInstanceOnDevice(getApplication().getModuleByName(module), getFogDeviceById(deviceId));
            }
        }
    }

    private List<String> getModulesToPlace(List<String> placedModules){
        Application app = getApplication();
        List<String> modulesToPlace_1 = new ArrayList<String>();
        List<String> modulesToPlace = new ArrayList<String>();

        // get the application modules not placed yet
        for(AppModule module : app.getModules()){
            if(!placedModules.contains(module.getName()))
                modulesToPlace_1.add(module.getName());
        }

        // filter the not placed modules whose incoming and outcoming edges are already placed
        for(String moduleName : modulesToPlace_1){
            boolean toBePlaced = true;

            for(AppEdge edge : app.getEdges()){
                //CHECK IF OUTGOING DOWN EDGES ARE PLACED
                if(edge.getSource().equals(moduleName) && edge.getDirection()==Tuple.DOWN && !placedModules.contains(edge.getDestination()))
                    toBePlaced = false;
                //CHECK IF INCOMING UP EDGES ARE PLACED
                if(edge.getDestination().equals(moduleName) && edge.getDirection()==Tuple.UP && !placedModules.contains(edge.getSource()))
                    toBePlaced = false;
            }
            if(toBePlaced)
                modulesToPlace.add(moduleName);
        }

        return modulesToPlace;
    }

    protected double getRateOfSensor(String sensorType){
        for(Sensor sensor : getSensors()){
            if(sensor.getTupleType().equals(sensorType))
                return 1/sensor.getTransmitDistribution().getMeanInterTransmitTime();
        }
        return 0;
    }

    private void placeModulesInPath(List<Integer> path) {
        if(path.size()==0)return;
        List<String> placedModules = new ArrayList<String>();

//		Map<AppEdge, Double> appEdgeToRate = new HashMap<AppEdge, Double>();
//
//		// get the rate of data generation of each edge
//		for(AppEdge edge : getApplication().getEdges()){
//			if(edge.isPeriodic()){
//				appEdgeToRate.put(edge, 1/edge.getPeriodicity());
//			}
//		}

        // for each device in the path
        for(Integer deviceId : path){

            FogDevice device = getFogDeviceById(deviceId);
            Map<String, Integer> sensorsAssociated = getAssociatedSensors(device);
            Map<String, Integer> actuatorsAssociated = getAssociatedActuators(device);

            // add all sensors and actuators to the placed list
            placedModules.addAll(sensorsAssociated.keySet());
            placedModules.addAll(actuatorsAssociated.keySet());

//			// calculates the rate of data generation by the sensors
//			for(String sensor : sensorsAssociated.keySet()){
//				for(AppEdge edge : getApplication().getEdges()){
//					if(edge.getSource().equals(sensor)){
//						appEdgeToRate.put(edge, sensorsAssociated.get(sensor)*getRateOfSensor(sensor));
//					}
//				}
//			}
//
//			// now updating the AppEdge rates for the entire application based on the knowledge so far
//			boolean changed = true;
//			// loop runs while there is any edge rate change
//			while(changed){
//				changed=false;
//				Map<AppEdge, Double> rateMap = new HashMap<AppEdge, Double>(appEdgeToRate);
//				// for each edge
//				for(AppEdge edge : rateMap.keySet()){
//					// get destination module
//					AppModule destModule = getApplication().getModuleByName(edge.getDestination());
//					if(destModule == null)continue;
//					// get mapping from one tuple type to another with a given selectivity
//					Map<Pair<String, String>, SelectivityModel> map = destModule.getSelectivityMap();
//					// iterates through all mappings from the current edge tuple type to any other type
//					for(Pair<String, String> pair : map.keySet()){
//						if(pair.getFirst().equals(edge.getTupleType())){
//							// the output rate is equal to the incoming tuple rate times the tuple transformation mean rate
//							double outputRate = appEdgeToRate.get(edge)*map.get(pair).getMeanRate();
//							// check if the rate of the output edge has changed
//							AppEdge outputEdge = getApplication().getEdgeMap().get(pair.getSecond());
//							if(!appEdgeToRate.containsKey(outputEdge) || appEdgeToRate.get(outputEdge)!=outputRate){
//								changed = true;
//							}
//							// updates the rate of the output edge
//							appEdgeToRate.put(outputEdge, outputRate);
//
//						}
//					}
//				}
//			}

            // get all modules whose incoming edges is already placed (modules that are ready to be placed)
            List<String> modulesToPlace = getModulesToPlace(placedModules);

            // while there is module to place
            while(modulesToPlace.size() > 0){

                boolean shouldRemoveModuleToPlace = true;

                // get the first module
                String moduleName = modulesToPlace.get(0);
                double totalCpuLoad = 0;
                if (this.debugMode) {
                    System.out.println();
                }

                //IF MODULE IS ALREADY PLACED UPSTREAM, THEN UPDATE THE EXISTING MODULE

                // gets the device where the module is placed
                int upsteamDeviceId = isPlacedUpstream(moduleName, path);
                String upstreamDeviceName = CloudSim.getEntityName(upsteamDeviceId);

                // check if it is place in any device
                if(upsteamDeviceId > 0){
                    // check if it is placed in the current device
                    if(upsteamDeviceId==deviceId){
                        // mark that the module is placed
                        placedModules.add(moduleName);
                        // updates the modules to place
                        modulesToPlace = getModulesToPlace(placedModules);

                        // NOW THE MODULE TO PLACE IS IN THE CURRENT DEVICE. CHECK IF THE NODE CAN SUSTAIN THE MODULE

//						// compute the cpu load of all incoming edges
//						for(AppEdge edge : getApplication().getEdges()){
//							if(edge.getDestination().equals(moduleName)){
//								double rate = appEdgeToRate.get(edge);
//								totalCpuLoad += rate*edge.getTupleCpuLength();
//							}
//						}

                        Double currentCpuLoad = getCurrentDeviceCpuLoad().get(deviceId);
                        if (this.debugMode) {
                            System.out.println("Trying to place " + moduleName + " on device " + device.getName()
                                    + " - current CPU load = " + currentCpuLoad);
                        }

                        //////****** CAUTION: HARD HACK ******//////
                        // ignores the incoming edge rate and consider only the module MIPS
                        totalCpuLoad = getApplication().getModuleByName(moduleName).getMips();
                        if (this.debugMode) {
                            System.out.println("CPU load for module " + moduleName + ": " + totalCpuLoad);
                        }
                        //////****** CAUTION: HARD HACK ******//////

                        // check if can place the module in the device
                        if(totalCpuLoad + currentCpuLoad > device.getHost().getTotalMips()){
                            if (this.debugMode) {
                                System.out.println("Placement of module " + moduleName + " NOT POSSIBLE on device " + device.getName());
                                System.out.println("Module CPU load = " + totalCpuLoad);
                                System.out.println("Current CPU load = " + getCurrentDeviceCpuLoad().get(deviceId));
                                System.out.println("Max mips = " + device.getHost().getTotalMips());
                                System.out.println("Need to shift module " + moduleName + " somewhere upstream.");
                            }

                            List<String> _placedModules = shiftModuleWithLessCommunicationImpactNorth(moduleName, deviceId, modulesToPlace);
                            // updates the placed modules
                            for(String placedModule : _placedModules) {
                                if (!placedModules.contains(placedModule)) {
                                    placedModules.add(placedModule);
                                }
                            }

                            if(!_placedModules.contains(moduleName)) {
                                placedModules.remove(moduleName);
                                shouldRemoveModuleToPlace = false;
                                modulesToPlace.add(moduleName);
                            }

                            if (this.debugMode) {
                                System.out.println("---------------------------------------------------");
                            }
                        } else{
                            // updates the cpu load and module instance count
                            placedModules.add(moduleName);
                            currentCpuLoad += totalCpuLoad;
                            getCurrentDeviceCpuLoad().put(deviceId, currentCpuLoad);
                            double totalDeviceMips = device.getHost().getTotalMips();

                            getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, getCurrentDeviceModuleInstanceNum().get(deviceId).get(moduleName)+1);
                            if (this.debugMode) {
                                System.out.println(moduleName+" can be created in "+device.getName()
                                        + " (" + currentCpuLoad/totalDeviceMips +"% cpu in use = "
                                        + currentCpuLoad + " / " + totalDeviceMips + ")");
                            }
                        }
                    }
                }else{
//					// FINDING OUT WHETHER PLACEMENT OF MODULE ON DEVICE IS POSSIBLE
//					for(AppEdge edge : getApplication().getEdges()){		// take all incoming edges
//						if(edge.getDestination().equals(moduleName)){
//							System.out.println(edge.getTupleType());
//							double rate = appEdgeToRate.get(edge);
//							totalCpuLoad += rate*edge.getTupleCpuLength();
//							System.out.println("Tuple type = "+edge.getTupleType());
//							System.out.println("Rate = "+rate);
//							System.out.println("Cpu Load (TOTAL MIPS / %) = "+rate*edge.getTupleCpuLength() + "(" + device.getHost().getTotalMips() + " / " + getCurrentDeviceCpuLoad().get(deviceId) +"+"+ rate*edge.getTupleCpuLength()/device.getHost().getTotalMips() + ")");
//						}
//					}

                    if (this.debugMode) {
                        System.out.println("Trying to place module "+moduleName);
                    }
                    //////****** CAUTION: HARD HACK ******//////
                    totalCpuLoad = getApplication().getModuleByName(moduleName).getMips();
                    System.out.println("CPU load for module " + moduleName + ": " + totalCpuLoad);
                    //////****** CAUTION: HARD HACK ******//////

                    if(totalCpuLoad + getCurrentDeviceCpuLoad().get(deviceId) > device.getHost().getTotalMips()){
                        System.out.println("Placement of module "+moduleName+ " NOT POSSIBLE on device "+device.getName());
                        System.out.println("CPU load = "+totalCpuLoad);
                        System.out.println("Current CPU load = "+ getCurrentDeviceCpuLoad().get(deviceId));
                        System.out.println("Max mips = "+device.getHost().getTotalMips());
                    } else{
                        System.out.println("Placement of module "+moduleName+ " on device "+device.getName());
                        getCurrentDeviceCpuLoad().put(deviceId, totalCpuLoad + getCurrentDeviceCpuLoad().get(deviceId));

                        System.out.println("Updated CPU load = "+ getCurrentDeviceCpuLoad().get(deviceId));
                        if(!currentDeviceModuleMap.containsKey(deviceId))
                            currentDeviceModuleMap.put(deviceId, new ArrayList<String>());
                        currentDeviceModuleMap.get(deviceId).add(moduleName);
                        placedModules.add(moduleName);
                        modulesToPlace = getModulesToPlace(placedModules);
                        getCurrentDeviceModuleLoadMap().get(device.getId()).put(moduleName, totalCpuLoad);

                        int max = 1;
                        for(AppEdge edge : getApplication().getEdges()){
                            if(edge.getSource().equals(moduleName) && actuatorsAssociated.containsKey(edge.getDestination()))
                                max = Math.max(actuatorsAssociated.get(edge.getDestination()), max);
                            if(edge.getDestination().equals(moduleName) && sensorsAssociated.containsKey(edge.getSource()))
                                max = Math.max(sensorsAssociated.get(edge.getSource()), max);
                        }
                        getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, max);
                        System.out.println("Number of instances for module "+moduleName+" = "+max);

                    }
                }

                if(shouldRemoveModuleToPlace) {
                    modulesToPlace.remove(moduleName);
                }
            }

        }

    }

    /**
     * Shifts a module moduleName from device deviceId northwards
     * @param deviceId
     */
    private List<String> shiftModuleWithLessCommunicationImpactNorth(String moduleName, Integer deviceId, List<String> modulesToPlace) {

        List<String> modulesToShift = findModulesWithLessCommunicationImpactToShift(moduleName, deviceId);

        // get all modules related to the given module (with a path to the current module)
        //List<String> modulesToShift = findModulesToShift(moduleName, deviceId);

        if (this.debugMode) {
            System.out.println("Modules to shift northwards : "+modulesToShift);
        }

        Map<String, Integer> moduleToNumInstances = new HashMap<String, Integer>();
        double totalCpuLoad = 0;

        // updated module load map
        Map<String, Double> loadMap = new HashMap<String, Double>();
        // move the modules
        for(String module : modulesToShift){
            loadMap.put(module, getCurrentDeviceModuleLoadMap().get(deviceId).get(module));
            // updates the module instance count
            moduleToNumInstances.put(module, getCurrentDeviceModuleInstanceNum().get(deviceId).get(module));

            /* totalCpuLoad += getCurrentDeviceModuleLoadMap().get(deviceId).get(module); */

            //////****** CAUTION: HARD HACK ******//////
            // get quantity of modules to shift and the total module load
            int moduleCount = moduleToNumInstances.get(module);
            double moduleCpuLoad = getApplication().getModuleByName(module).getMips()*moduleCount;
            if (this.debugMode) {
                System.out.println("CPU load for " + moduleCount + " " + module + " modules: " + moduleCpuLoad);
            }
            //////****** CAUTION: HARD HACK ******//////

            // updates the total moved CPU load
            totalCpuLoad += moduleCpuLoad;

            // remove the module from the original device
            getCurrentDeviceModuleLoadMap().get(deviceId).remove(module);
            getCurrentDeviceModuleMap().get(deviceId).remove(module);
            getCurrentDeviceModuleInstanceNum().get(deviceId).remove(module);

            // updates the module load
            loadMap.put(module, loadMap.get(module)+getApplication().getModuleByName(module).getMips());
        }

        if (this.debugMode) {
            System.out.println("Current device load on " + getDeviceById(deviceId).getName() + ": " + getCurrentDeviceCpuLoad().get(deviceId));
            System.out.println("Load after removal on " + getDeviceById(deviceId).getName() + ": " + (getCurrentDeviceCpuLoad().get(deviceId) - totalCpuLoad));
        }

        // reduce the load of the original device
        getCurrentDeviceCpuLoad().put(deviceId, getCurrentDeviceCpuLoad().get(deviceId)-totalCpuLoad);

        int lowestInstanceNumber = moduleToNumInstances.get(modulesToShift.get(0));
        for(String module : modulesToShift) {
            if(moduleToNumInstances.get(module) < lowestInstanceNumber) {
                lowestInstanceNumber = moduleToNumInstances.get(module);
            }
        }

        for(String module : modulesToShift) {
            if(moduleToNumInstances.get(module) == lowestInstanceNumber) {
                moduleToNumInstances.put(module, moduleToNumInstances.get(module) + 1);
                double moduleCpuLoad = getApplication().getModuleByName(module).getMips();
                totalCpuLoad += moduleCpuLoad;
            }
        }

        if (this.debugMode) {
            System.out.println("Module instances to shift northwards : "+moduleToNumInstances);
        }

        int id = getParentDevice(deviceId);
        while(true){
            if(id==-1){
                System.out.println("Could not place modules "+modulesToShift+" northwards.");
                break;
            }
            FogDevice fogDevice = getFogDeviceById(id);

            // check if can place in the current device
            if(getCurrentDeviceCpuLoad().get(id) + totalCpuLoad > fogDevice.getHost().getTotalMips()){
                // if not, update the list of modules that should be shifted
                // because some of the modules at this device might depend on the result of some modules to shift
                //List<String> _modulesToShift = findModulesToShift(modulesToShift, id);
                // the total cpu load shifted from device id to its parent
                List<String> _modulesToShift = modulesToShift;
                double cpuLoadShifted = 0;
                // check if there is any new module to shift
                for(String module : _modulesToShift){
                    // check if the device contains any instance of a module that should be shifted
                    if(getCurrentDeviceModuleInstanceNum().get(id).containsKey(module)){
                        // updates the quantity of modules to shift, considering the modules in this device
                        int deviceModuleCount = getCurrentDeviceModuleInstanceNum().get(id).get(module);
                        int totalModuleCount = moduleToNumInstances.get(module) + deviceModuleCount;
                        moduleToNumInstances.put(module, totalModuleCount);

                        loadMap.put(module, getCurrentDeviceModuleLoadMap().get(id).get(module));
//						cpuLoadShifted += getCurrentDeviceModuleLoadMap().get(id).get(module);
//						totalCpuLoad += getCurrentDeviceModuleLoadMap().get(id).get(module);

                        //////****** CAUTION: HARD HACK ******//////
                        // get quantity of modules to shift in this device and the total module load
                        double moduleCpuLoad = getApplication().getModuleByName(module).getMips()*deviceModuleCount;
                        if (this.debugMode) {
                            System.out.println("CPU load for " + deviceModuleCount + " " + module + " modules: " + moduleCpuLoad);
                        }
                        cpuLoadShifted += moduleCpuLoad;
                        totalCpuLoad += moduleCpuLoad;
                        //////****** CAUTION: HARD HACK ******//////

                        // remove the module from the device
                        getCurrentDeviceModuleLoadMap().get(id).remove(module);
                        getCurrentDeviceModuleMap().get(id).remove(module);
                        getCurrentDeviceModuleInstanceNum().get(id).remove(module);
                    }
                }
                // updates the device load
                getCurrentDeviceCpuLoad().put(id, getCurrentDeviceCpuLoad().get(id)-cpuLoadShifted);

                if (this.debugMode) {
                    System.out.println("CPU load after module removal on device "+CloudSim.getEntityName(id)+" = "+ getCurrentDeviceCpuLoad().get(id));
                }

                // updates the module to shift and goes to the parent node
                modulesToShift = _modulesToShift;
                id = getParentDevice(id);
            } else{
                // can place the modules here
                if (this.debugMode) {
                    System.out.println("Can place modules "+modulesToShift+ " on device "+CloudSim.getEntityName(id));
                }

                double totalLoad = getCurrentDeviceCpuLoad().get(id);
                if (this.debugMode) {
                    System.out.println("Current CPU load on device "+CloudSim.getEntityName(id)+" = "+ totalLoad);
                }
                // get total load and final instance count
                for(String module : loadMap.keySet()){
                    double moduleCpuLoad = loadMap.get(module);
                    int modulesToPlaceCount = moduleToNumInstances.get(module);
                    totalLoad += moduleCpuLoad * modulesToPlaceCount;

                    getCurrentDeviceModuleLoadMap().get(id).put(module, moduleCpuLoad);
                    getCurrentDeviceModuleMap().get(id).add(module);

                    String module_ = module;
                    int initialNumInstances = 0;
                    if(getCurrentDeviceModuleInstanceNum().get(id).containsKey(module_))
                        initialNumInstances = getCurrentDeviceModuleInstanceNum().get(id).get(module_);
                    int finalNumInstances = initialNumInstances + modulesToPlaceCount;

                    if (this.debugMode) {
                        System.out.println("Placing "+finalNumInstances+" of " + module + " on "+CloudSim.getEntityName(id));
                    }
                    // update module instance count
                    getCurrentDeviceModuleInstanceNum().get(id).put(module_, finalNumInstances);
                }

                // updates device load
                getCurrentDeviceCpuLoad().put(id, totalLoad);
                modulesToPlace.removeAll(loadMap.keySet());

                if (this.debugMode) {
                    System.out.println("Final CPU load on device "+CloudSim.getEntityName(id)+" = "+ getCurrentDeviceCpuLoad().get(id));
                }
                List<String> placedModules = new ArrayList<String>();
                for(String op : loadMap.keySet())placedModules.add(op);
                return placedModules;
            }
        }
        return new ArrayList<String>();

    }

    // Chooses the modules that will have the less impact on the network to be shifted.
    // Looks on the edges incoming and oucoming from the module to make the decision
    private List<String> findModulesWithLessCommunicationImpactToShift(String moduleName, Integer deviceId) {

        List<String> deviceModules = currentDeviceModuleMap.containsKey(deviceId) ? currentDeviceModuleMap.get(deviceId) : new ArrayList<String>();
        if (!deviceModules.contains(moduleName)) {
            deviceModules.add(moduleName);
        }

        List<String> deviceTopModules = this.deviceTopModules(deviceModules);

        List<List<String>> modulesSets = new ArrayList<List<String>>();

        for(String deviceTopModule: deviceTopModules) {
            List<String> set = new ArrayList<String>();
            set.add(deviceTopModule);
            modulesSets.add(set);
        }

        List<String> parentDevicesModules =  new ArrayList<String>();
        Integer parentId = getParentDevice(deviceId);
        while(parentId != null && currentDeviceModuleMap.containsKey(parentId)) {
            parentDevicesModules.addAll(currentDeviceModuleMap.get(parentId));
            parentId = getParentDevice(parentId);
        }


        List<List<String>> previousModulesSets = new ArrayList<List<String>>();
        do {
            List<List<String>> modulesToSearch = new ArrayList<List<String>>();
            for(List<String> modulesSet: modulesSets) {
                if(!previousModulesSets.contains(modulesSet)) {
                    modulesToSearch.add(modulesSet);
                }
            }
            previousModulesSets = new ArrayList<>(modulesSets);
            for(List<String> set: modulesToSearch) {
                for(String module: set) {
                    for(AppEdge edge : getApplication().getEdges()) {
                        if(edge.getDestination().equals(module) && edge.getDirection() == Tuple.UP && deviceModules.contains(edge.getSource()) && !set.contains(edge.getSource())) {
                            List<String> newSet = new ArrayList<>(set);
                            newSet.add(edge.getSource());
                            modulesSets.add(newSet);
                        }
                    }
                }
            }
        } while(!previousModulesSets.equals(modulesSets));

        Map<List<String>, Double> modulesSetsNetworkImpact = new HashMap<List<String>, Double>();

        // For each module set on the current device evaluate the network impact of performing the change. The impact is
        // measured by how the network communication of the parent device will increase. More communication of the
        // parent device means more data being exchanged with a farther device and so causing more delay
        for(List<String> modulesSet : modulesSets) {

            Double modulesSetTotal = moduleSetNetworkImpact(modulesSet, parentDevicesModules, deviceId);
            modulesSetsNetworkImpact.put(modulesSet, modulesSetTotal);

        }

        List<String> modulesSetWithLessImpact = null;
        Double lessNetworkImpact = null;
        for (Map.Entry<List<String>, Double> entry : modulesSetsNetworkImpact.entrySet()) {
            if(lessNetworkImpact == null || entry.getValue() < lessNetworkImpact) {
                lessNetworkImpact = entry.getValue();
                modulesSetWithLessImpact = entry.getKey();
            }
        }

        System.out.println("Modules set with less impact to be shifted: " + modulesSetWithLessImpact + ". Impact: " + lessNetworkImpact);
        return modulesSetWithLessImpact != null ? modulesSetWithLessImpact : new ArrayList<>();
    }

    private Double moduleSetNetworkImpact(List<String> moduleSet, List<String> parentDevicesModules, Integer deviceId) {
        Double setImpact = 0.0;
        for(String module : moduleSet) {
            setImpact += moduleNetworkImpact(module, moduleSet, parentDevicesModules, deviceId);
        }

        return setImpact;
    }

    private Double moduleNetworkImpact(String module, List<String> moduleSet, List<String> parentDevicesModules, Integer deviceId) {
        Double moduleNetworkTotal = 0.0;
//        int moduleIntanceNum = currentDeviceModuleInstanceNum.get(deviceId).get(module);
//        if(moduleIntanceNum == 0) {
//            moduleIntanceNum = 1;
//        }
        int moduleIntanceNum = 1;

        for(AppEdge edge : getApplication().getEdges()) {

            double edgeSelectivity = 1.0;
            AppModule edgeSourceModule = getApplication().getModuleByName(edge.getSource());
            if(edgeSourceModule != null) {
                Map<Pair<String, String>, SelectivityModel> moduleSelectivityMap = edgeSourceModule.getSelectivityMap();
                for (Map.Entry<Pair<String, String>, SelectivityModel> entry : moduleSelectivityMap.entrySet()) {
                    if(entry.getKey().getSecond().equals(edge.getTupleType())) {
                        FractionalSelectivity selectivityModel = (FractionalSelectivity)entry.getValue();
                        if(selectivityModel != null) {
                            edgeSelectivity = selectivityModel.getSelectivity();
                        }
                    }
                }
            }

            double edgePeriodicityMultiplier = 1;
            if(edge.isPeriodic()) {
                if(getApplication().getAppId() == "DCNS") {
                    edgePeriodicityMultiplier = 1/(edge.getPeriodicity()/DCNS_TRANSMISSION_TIME);
                } else if(getApplication().getAppId().contains("VRGame")) {
                    edgePeriodicityMultiplier = 1/(edge.getPeriodicity()/EEG_TRANSMISSION_TIME);
                }
            }
            if(edge.getSource().equals(module)) {
                // If the current module is the source of an edge and the parent device does not contain the
                // destination, then this edge network length will be added to the parent communication
                if(!parentDevicesModules.contains(edge.getDestination())) {
                    if(!moduleSet.contains(edge.getDestination())) {
                        moduleNetworkTotal += edge.getTupleNwLength() * moduleIntanceNum * edgeSelectivity * edgePeriodicityMultiplier;
                    }

                } else {
                    // If the parent module contains the destination module of the edge, then moving this module will
                    // cause the communication of the parent device to deacrese since the both modules will be on
                    // the same device when the change finishes
                    moduleNetworkTotal -= edge.getTupleNwLength() * moduleIntanceNum * edgeSelectivity * edgePeriodicityMultiplier;
                }

            } else if(edge.getDestination().equals(module)) {
                if(!parentDevicesModules.contains(edge.getSource())) {
                    if(!moduleSet.contains(edge.getSource())) {
                        moduleNetworkTotal += edge.getTupleNwLength() * moduleIntanceNum * edgeSelectivity * edgePeriodicityMultiplier;

                    }
                } else {
                    moduleNetworkTotal -= edge.getTupleNwLength() * moduleIntanceNum * edgeSelectivity * edgePeriodicityMultiplier;
                }
            }
        }
        return moduleNetworkTotal;
    }
    private List<String>  deviceTopModules(List<String> deviceModules) {

        List<String> deviceTopModules = new ArrayList<String>();
        for(String module : deviceModules) {

            if(getApplication().getModuleByName(module) != null) {
                boolean foundUpEdge = false;
                for(AppEdge edge : getApplication().getEdges()) {
                    if(edge.getSource().equals(module) && edge.getDirection() == Tuple.UP && deviceModules.contains(edge.getDestination())) {
                        foundUpEdge = true;
                    }
                }

                if(!foundUpEdge) {
                    deviceTopModules.add(module);
                }
            }
        }

        return deviceTopModules;
    }

    private int isPlacedUpstream(String moduleName, List<Integer> path) {
        for(int deviceId : path){
            if(currentDeviceModuleMap.containsKey(deviceId) && currentDeviceModuleMap.get(deviceId).contains(moduleName))
                return deviceId;
        }
        return -1;
    }

    private Map<String, Integer> getAssociatedSensors(FogDevice device) {
        Map<String, Integer> endpoints = new HashMap<String, Integer>();
        for(Sensor sensor : getSensors()){
            if(sensor.getGatewayDeviceId()==device.getId()){
                if(!endpoints.containsKey(sensor.getTupleType()))
                    endpoints.put(sensor.getTupleType(), 0);
                endpoints.put(sensor.getTupleType(), endpoints.get(sensor.getTupleType())+1);
            }
        }
        return endpoints;
    }
    private Map<String, Integer> getAssociatedActuators(FogDevice device) {
        Map<String, Integer> endpoints = new HashMap<String, Integer>();
        for(Actuator actuator : getActuators()){
            if(actuator.getGatewayDeviceId()==device.getId()){
                if(!endpoints.containsKey(actuator.getActuatorType()))
                    endpoints.put(actuator.getActuatorType(), 0);
                endpoints.put(actuator.getActuatorType(), endpoints.get(actuator.getActuatorType())+1);
            }
        }
        return endpoints;
    }

    @SuppressWarnings("serial")
    protected List<List<Integer>> getPaths(final int fogDeviceId){
        FogDevice device = (FogDevice)CloudSim.getEntity(fogDeviceId);
        // check if is leaf node
        if(device.getChildrenIds().size() == 0) {
            // if is leaf node, create a path with just this node and return
            final List<Integer> path =  (new ArrayList<Integer>(){{add(fogDeviceId);}});
            List<List<Integer>> paths = (new ArrayList<List<Integer>>(){{add(path);}});
            return paths;
        }
        List<List<Integer>> paths = new ArrayList<List<Integer>>();
        // get all paths from leaves nodes to children of this node
        for(int childId : device.getChildrenIds()){
            // get all paths from leaves nodes to the current child
            List<List<Integer>> childPaths = getPaths(childId);
            // insert the current device in the paths
            for(List<Integer> childPath : childPaths)
                childPath.add(fogDeviceId);
            paths.addAll(childPaths);
        }
        return paths;
    }

    protected List<List<Integer>> getLeafToRootPaths(){
        // gets the cloud device
        FogDevice cloud = null;
        for(FogDevice device : getFogDevices()){
            if(device.getName().equals("cloud"))
                cloud = device;
        }
        // get all paths from leaves nodes to the cloud device
        return getPaths(cloud.getId());
    }

    public ModuleMapping getModuleMapping() {
        return moduleMapping;
    }

    public void setModuleMapping(ModuleMapping moduleMapping) {
        this.moduleMapping = moduleMapping;
    }

    public Map<Integer, List<String>> getCurrentDeviceModuleMap() {
        return currentDeviceModuleMap;
    }

    public void setCurrentDeviceModuleMap(Map<Integer, List<String>> currentDeviceModuleMap) {
        this.currentDeviceModuleMap = currentDeviceModuleMap;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public List<Actuator> getActuators() {
        return actuators;
    }

    public void setActuators(List<Actuator> actuators) {
        this.actuators = actuators;
    }

    public Map<Integer, Double> getCurrentDeviceCpuLoad() {
        return currentDeviceCpuLoad;
    }

    public void setCurrentDeviceCpuLoad(Map<Integer, Double> currentDeviceCpuLoad) {
        this.currentDeviceCpuLoad = currentDeviceCpuLoad;
    }

    public Map<Integer, Map<String, Double>> getCurrentDeviceModuleLoadMap() {
        return currentDeviceModuleLoadMap;
    }

    public void setCurrentDeviceModuleLoadMap(
            Map<Integer, Map<String, Double>> currentDeviceModuleLoadMap) {
        this.currentDeviceModuleLoadMap = currentDeviceModuleLoadMap;
    }

    public Map<Integer, Map<String, Integer>> getCurrentDeviceModuleInstanceNum() {
        return currentDeviceModuleInstanceNum;
    }

    public void setCurrentDeviceModuleInstanceNum(
            Map<Integer, Map<String, Integer>> currentDeviceModuleInstanceNum) {
        this.currentDeviceModuleInstanceNum = currentDeviceModuleInstanceNum;
    }


}
