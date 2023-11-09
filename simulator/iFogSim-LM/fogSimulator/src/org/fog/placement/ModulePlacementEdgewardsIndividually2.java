package org.fog.placement;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.gui.core.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModulePlacementEdgewardsIndividually2 extends ModulePlacement {

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
    protected static Map<Integer, Map<String, Integer>> currentDeviceModuleInstanceNumPersistent = new HashMap<>();
    protected Map<Integer, Map<String, List<Integer>>> currentDeviceModuleInstanceList;

    protected Map<String, Application> applications;

    private boolean debugMode;

    public ModulePlacementEdgewardsIndividually2(List<FogDevice> fogDevices, List<Sensor> sensors,
                                                 List<Actuator> actuators,
                                                 Application application, ModuleMapping moduleMapping,
                                                 Map<Integer, Double> globalCPULoad, Map<String, Application> applications, boolean debugMode) {
        this.setFogDevices(fogDevices);
        this.setSensors(sensors);
        this.setActuators(actuators);
        this.setApplication(application);
        this.setModuleMapping(moduleMapping);
        this.setCurrentDeviceCpuLoad(globalCPULoad);

        this.applications = applications;

        this.debugMode = debugMode;

        this.setModuleToDeviceMap(new HashMap<>());
        this.setDeviceToModuleMap(new HashMap<>());
        this.setCurrentDeviceModuleMap(new HashMap<>());
        this.setCurrentDeviceModuleLoadMap(new HashMap<>());
        this.setCurrentDeviceModuleInstanceNum(new HashMap<>());
        this.setCurrentDeviceModuleInstanceList(new HashMap<>());

        for(FogDevice dev : getFogDevices()) {
            getCurrentDeviceModuleLoadMap().put(dev.getId(), new HashMap<>());
            getCurrentDeviceModuleMap().put(dev.getId(), new ArrayList<>());
            getCurrentDeviceModuleInstanceNum().put(dev.getId(), new HashMap<>());
            getCurrentDeviceModuleInstanceList().put(dev.getId(), new HashMap<>());
        }

        this.mapModules();

        this.setModuleInstanceCountMap(getCurrentDeviceModuleInstanceNum());
        this.setModuleInstanceListMap(getCurrentDeviceModuleInstanceList());
    }

    @Override
    protected void mapModules() {

        // get all devices
        for(String deviceName : getModuleMapping().getNodeToModuleCountMapping().keySet()) {
            // get all modules for the device
            for(String moduleName : getModuleMapping().getNodeToModuleCountMapping().get(deviceName).keySet()) {
                // update the current device to module map
                int deviceId = CloudSim.getEntityId(deviceName);
                getCurrentDeviceModuleMap().get(deviceId).add(moduleName);
                getCurrentDeviceModuleLoadMap().get(deviceId).put(moduleName, 0.0);
                getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, 0);
                getCurrentDeviceModuleInstanceList().get(deviceId).put(moduleName, new ArrayList<>());
            }
        }

        // get all paths from leaves nodes to the cloud device
        List<List<Integer>> leafToRootPaths = getLeafToRootPaths();

        for(List<Integer> path : leafToRootPaths) {
            placeModulesInPath(path);
        }

        for(int deviceId : getCurrentDeviceModuleMap().keySet()) {
            for(String module : getCurrentDeviceModuleMap().get(deviceId)) {
                List<Integer> modDevMap = getModuleToDeviceMap().get(module);
                if(modDevMap != null) {
                    if(!modDevMap.contains(deviceId)) {
                        createModuleInstanceOnDevice(getApplication().getModuleByName(module),
                                                     getFogDeviceById(deviceId));
                    }
                } else {
                    createModuleInstanceOnDevice(getApplication().getModuleByName(module),
                                                 getFogDeviceById(deviceId));
                }
            }
        }
    }

    private List<String> getModulesToPlace(List<String> placedModules) {
        Application app = getApplication();
        List<String> modulesToPlace_1 = new ArrayList<>();
        List<String> modulesToPlace = new ArrayList<>();

        // get the application modules not placed yet
        for(AppModule module : app.getModules()) {
            if(!placedModules.contains(module.getName())) {
                modulesToPlace_1.add(module.getName());
            }
        }

        // filter the not placed modules whose incoming and outcoming edges are already placed
        for(String moduleName : modulesToPlace_1) {
            boolean toBePlaced = true;

            for(AppEdge edge : app.getEdges()) {
                //CHECK IF OUTGOING DOWN EDGES ARE PLACED
                if(edge.getSource().equals(moduleName) && edge.getDirection() == Tuple.DOWN
                    && !placedModules.contains(edge.getDestination())) {
                    toBePlaced = false;
                }
                //CHECK IF INCOMING UP EDGES ARE PLACED
                if(edge.getDestination().equals(moduleName) && edge.getDirection() == Tuple.UP
                    && !placedModules.contains(edge.getSource())) {
                    toBePlaced = false;
                }
            }
            if(toBePlaced) {
                modulesToPlace.add(moduleName);
            }
        }

        return modulesToPlace;
    }

    private void placeModulesInPath(List<Integer> path) {
        if(path.size() == 0) {
            return;
        }
        List<String> placedModules = new ArrayList<>();
        List<Integer> instanceToPlace = getInstanceListToPlace(path);

        // for each device in the path
        for(Integer deviceId : path) {

            FogDevice device = getFogDeviceById(deviceId);
            Map<String, Integer> sensorsAssociated = getAssociatedSensors(device);
            Map<String, Integer> actuatorsAssociated = getAssociatedActuators(device);

            // add all sensors and actuators to the placed list
            placedModules.addAll(sensorsAssociated.keySet());
            placedModules.addAll(actuatorsAssociated.keySet());

            // get all modules whose incoming edges is already placed (modules that are ready to be placed)
            List<String> modulesToPlace = getModulesToPlace(placedModules);

            // while there is module to place
            while(modulesToPlace.size() > 0) {
                // get the first module
                String moduleName = modulesToPlace.get(0);

                //IF MODULE IS ALREADY PLACED UPSTREAM, THEN UPDATE THE EXISTING MODULE

                // ignores the incoming edge rate and consider only the module MIPS
                double totalCpuLoad = getApplication().getModuleByName(moduleName).getMips();

                // gets the device where the module is placed
                int upstreamDeviceId = isPlacedUpstream(moduleName, path);

                for (int downstreamDeviceId : path) {
                    if (deviceId.equals(downstreamDeviceId)) {
                        break;
                    }

                    if ((getCurrentDeviceModuleMap().get(downstreamDeviceId).contains(moduleName))
                         && (getCurrentDeviceModuleInstanceNum().get(downstreamDeviceId).get(moduleName) <= 0)) {

                        System.out.println("Removing " + moduleName + " from device: " + getFogDeviceById(downstreamDeviceId).getName());
                        getCurrentDeviceModuleMap().get(downstreamDeviceId).remove(moduleName);
                        getCurrentDeviceModuleLoadMap().get(downstreamDeviceId).remove(moduleName);
                        getCurrentDeviceModuleInstanceNum().get(downstreamDeviceId).remove(moduleName);
                        getCurrentDeviceModuleInstanceList().get(downstreamDeviceId).remove(moduleName);

                        if(currentDeviceModuleInstanceNumPersistent.containsKey(downstreamDeviceId))
                            currentDeviceModuleInstanceNumPersistent.get(downstreamDeviceId).remove(moduleName);
                    }
                }

                // check if it is place in any device
                if(upstreamDeviceId > 0) {
                    // check if it is placed in the current device
                    if(upstreamDeviceId == deviceId) {
                        // mark that the module is placed
                        placedModules.add(moduleName);
                        // updates the modules to place
                        modulesToPlace = getModulesToPlace(placedModules);

                        // if not, move the module to upstream device
                        List<String> _placedModules = placeModule(moduleName,
                                                                  totalCpuLoad,
                                                                  1,
                                                                  deviceId,
                                                                  instanceToPlace);
                        // updates the placed modules
                        for(String placedModule : _placedModules) {
                            if(!placedModules.contains(placedModule)) {
                                placedModules.add(placedModule);
                            }
                        }

                        modulesToPlace.removeAll(placedModules);
                    }
                } else {
                    // FINDING OUT WHETHER PLACEMENT OF MODULE ON DEVICE IS POSSIBLE

                    if(this.debugMode) {
                        System.out.println();
                        System.out.println("------------------------------------------------");
                        System.out.println("Trying to place module " + moduleName);
                    }

                    if(totalCpuLoad + getCurrentDeviceCpuLoad().get(deviceId) > device.getHost().getTotalMips()) {
                        System.out.println("Placement of module " + moduleName + " NOT POSSIBLE on device "
                                               + device.getName());
                        System.out.println("CPU load = " + totalCpuLoad);
                        System.out.println("Current CPU load = " + getCurrentDeviceCpuLoad().get(deviceId));
                        System.out.println("Max mips = " + device.getHost().getTotalMips());
                    } else {
                        System.out.println("Placement of module " + moduleName + " on device " + device.getName());
                        getCurrentDeviceCpuLoad().put(deviceId, totalCpuLoad + getCurrentDeviceCpuLoad().get(deviceId));

                        System.out.println("Updated CPU load = " + getCurrentDeviceCpuLoad().get(deviceId));
                        if(!currentDeviceModuleMap.containsKey(deviceId)) {
                            currentDeviceModuleMap.put(deviceId, new ArrayList<>());
                        }
                        currentDeviceModuleMap.get(deviceId).add(moduleName);
                        placedModules.add(moduleName);
                        modulesToPlace = getModulesToPlace(placedModules);
                        getCurrentDeviceModuleLoadMap().get(device.getId()).put(moduleName, totalCpuLoad);

                        getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, 1);
                        getCurrentDeviceModuleInstanceList().get(deviceId).put(moduleName, instanceToPlace);
                        System.out.println("Number of instances for module " + moduleName + " = " + 1);

                    }
                }

                modulesToPlace.remove(moduleName);
            }

        }
    }

    private List<Integer> getInstanceListToPlace(List<Integer> path) {
        List<Integer> instanceList = new ArrayList<>();
        for(Sensor sensor : getSensors()) {
            if (path.contains(sensor.getGatewayDeviceId())
                && !instanceList.contains(sensor.getAssociatedAppInstance())) {

                instanceList.add(sensor.getAssociatedAppInstance());
            }
        }
        return  instanceList;
    }

    Map<String, Double> sensorFrequencyEmmiter = new HashMap<>(){{
        put("DCNS", 6.05);
        put("VRGame0", 15.04);
        put("VRGame1", 15.04);
        put("VRGame2", 15.04);
        put("LSWM", 6.04);
    }};

    Map<String, Double> moduleFrequencyReceiver = new HashMap<>(){{
        put("concentration_calculator_1", 4.52);
        put("connector_1", 4.52);
    }};

    public double getFrequencyReceiber(String module, String application){
        double frequencia = sensorFrequencyEmmiter.get(application);

        if (moduleFrequencyReceiver.containsKey(module)){
            frequencia = moduleFrequencyReceiver.get(module);
        }
        return frequencia;
    }

    private double cpuNeeded(String applicationName, String module){


        double cpuNeed = applications.get(applicationName).getEdges().stream()
                .filter(appEdge -> appEdge.getEdgeType() == AppEdge.MODULE)
                .filter(appEdge -> appEdge.getDestination().equals(module))
                .map(appEdge -> appEdge.getTupleCpuLength().getMeanInterTransmitTime() /
                        getFrequencyReceiber(module,applicationName))
                .mapToDouble(Double::doubleValue)
                .sum();

        return cpuNeed;
    }

    private Double calcularprevisaoDeOcupacao(int deviceId, AppModule novoModulo){
        if(!currentDeviceModuleInstanceNumPersistent.containsKey(deviceId))
            currentDeviceModuleInstanceNumPersistent.put(deviceId, new HashMap<>());

        Map<String, Integer> instancesCount = currentDeviceModuleInstanceNumPersistent.get(deviceId);

        Map<String, String> mapModulesApplication = new HashMap<>();
        applications.values().forEach(application -> {
            application.getModules().forEach(appModule -> {
                mapModulesApplication.put(appModule.getName(), application.getAppId());
            });
        });

        Map<String, Double> cpuNeeded = new HashMap<>();
        instancesCount.forEach((nomeModulo, instances) -> {
            String applicationName = mapModulesApplication.get(nomeModulo);
            double cpuNeed = cpuNeeded(applicationName, nomeModulo);
            int instancias = 0;
            if(currentDeviceModuleInstanceNumPersistent.containsKey(deviceId) && currentDeviceModuleInstanceNumPersistent
                    .get(deviceId).containsKey(nomeModulo)){
                instancias = currentDeviceModuleInstanceNumPersistent.get(deviceId).get(nomeModulo);
            }
            double cpuNeedByInstances = cpuNeed * instancias;
            cpuNeeded.put(nomeModulo, cpuNeedByInstances);
        });

        cpuNeeded.put("NOVO", cpuNeeded(novoModulo.getAppId(),novoModulo.getName()));

        FogDevice device = (FogDevice) CloudSim.getEntity(deviceId);

        Double totalCpuNeeded = cpuNeeded
                .values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();


        return device.getHost().getTotalMips() - (totalCpuNeeded);
    }

    /**
     * Shifts a module moduleName from device deviceId northwards
     *
     * @param moduleName name of the module to shift
     * @param cpuLoad    cpuLoad of the module
     * @param deviceId   original module device
     */
    private List<String> placeModule(String moduleName, double cpuLoad, int instanceCount,
                                     Integer deviceId, List<Integer> instanceToPlace) {
        if(deviceId == -1) {
            System.out.println("Could not place modules " + moduleName + " northwards.");
            return new ArrayList<>();
        }

        String deviceName = CloudSim.getEntityName(deviceId);

        if(this.debugMode) {
            System.out.println();
            System.out.println("------------------------------------------------");
            System.out.println("Trying to place " + instanceCount + " module " + moduleName + " on " + deviceName);
        }

        FogDevice device = getFogDeviceById(deviceId);
        double currentDeviceLoad = getCurrentDeviceCpuLoad().get(deviceId);
        double newModuleLoad = cpuLoad * instanceCount;

        Double currentCpuLoad = getCurrentDeviceCpuLoad().get(deviceId);
        double totalCpuLoad = getApplication().getModuleByName(moduleName).getMips();

        boolean sobrecarregar = false;
        boolean temEspaco = totalCpuLoad + currentCpuLoad <= (getFogDeviceById(deviceId).getHost().getTotalMips());

        Double previsaoOcupacao = calcularprevisaoDeOcupacao(deviceId, getApplication().getModuleByName(moduleName));
        if(previsaoOcupacao >= 0 && !device.getName().startsWith("m-") && !device.getName().startsWith("proxy")){
            sobrecarregar = true;
        }

       if(!temEspaco && !sobrecarregar)
//        if(currentDeviceLoad + newModuleLoad >(getFogDeviceById(deviceId).getHost().getTotalMips()+acrescimo))
        {
            // can't place the new module in the device, so needs to shift the current module north
            if(this.debugMode) {
                System.out.println("Unable to add " + instanceCount + " module " + moduleName
                                       + " to device " + deviceName);
                System.out.println("Current device load: " + currentDeviceLoad
                                       + ", final load = " + (currentDeviceLoad + newModuleLoad));
                System.out.println("Shift module " + moduleName + " from device " + deviceName);
            }

            if ((getCurrentDeviceModuleMap().get(deviceId).contains(moduleName))
                && (getCurrentDeviceModuleInstanceNum().get(deviceId).get(moduleName) <= 0)) {

                System.out.println("Removing " + moduleName + " from device: " + getFogDeviceById(deviceId).getName());
                getCurrentDeviceModuleMap().get(deviceId).remove(moduleName);
                getCurrentDeviceModuleLoadMap().get(deviceId).remove(moduleName);
                getCurrentDeviceModuleInstanceNum().get(deviceId).remove(moduleName);
                getCurrentDeviceModuleInstanceList().get(deviceId).remove(moduleName);
            }

            // place the module in the upstream device
            return this.placeModule(moduleName, cpuLoad,
                                    instanceCount,
                                    getParentDevice(deviceId), instanceToPlace);

        } else {
            // can place the module in the device
            if(this.debugMode) {
                System.out.println("Placing " + instanceCount + " module " + moduleName
                                       + " on device " + deviceName + " with load: " + cpuLoad);
            }

            double finalDeviceLoad = currentDeviceLoad + newModuleLoad;
            getCurrentDeviceCpuLoad().put(deviceId, finalDeviceLoad);

            if(this.debugMode) {
                System.out.println("Load of device " + deviceName + " change from " + currentDeviceLoad
                                       + " to " + finalDeviceLoad);
            }

            getCurrentDeviceModuleLoadMap().get(deviceId).put(moduleName, cpuLoad);
            if(!getCurrentDeviceModuleMap().get(deviceId).contains(moduleName)) {
                getCurrentDeviceModuleMap().get(deviceId).add(moduleName);
            }

            int initialInstancesCount = 0;
            if (getCurrentDeviceModuleInstanceNum().get(deviceId).containsKey(moduleName)) {
                initialInstancesCount = getCurrentDeviceModuleInstanceNum().get(deviceId).get(moduleName);
            }
            if (!getCurrentDeviceModuleInstanceList().get(deviceId).containsKey(moduleName)) {
                getCurrentDeviceModuleInstanceList().get(deviceId).put(moduleName, new ArrayList<>());
            }
            for (int instance: instanceToPlace) {
                getCurrentDeviceModuleInstanceList().get(deviceId).get(moduleName).add(instance);
            }
            int finalInstancesCount = getCurrentDeviceModuleInstanceList().get(deviceId).get(moduleName).size();
            getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, finalInstancesCount);


            if(!currentDeviceModuleInstanceNumPersistent.containsKey(deviceId)){
                currentDeviceModuleInstanceNumPersistent.put(deviceId, new HashMap<>());
            }
            currentDeviceModuleInstanceNumPersistent.get(deviceId).put(moduleName, finalInstancesCount);



            if(this.debugMode) {
                System.out.println("instances of " + moduleName + " on device " + deviceName
                                       + " change from " + initialInstancesCount + " to " + finalInstancesCount);
            }

            List<String> placedModules = new ArrayList<>();
            placedModules.add(moduleName);
            return placedModules;
        }
    }

    private int isPlacedUpstream(String moduleName, List<Integer> path) {
        int upstreamDeviceId = -1;

        List<String> dependentModules = findDependentModules(moduleName);

        for(int deviceId : path) {
            if(currentDeviceModuleMap.containsKey(deviceId)) {
                for (String module : dependentModules) {
                    if (currentDeviceModuleMap.get(deviceId).contains(module)){
                        upstreamDeviceId = deviceId;
                        break;
                    }
                }
            }
        }
        return upstreamDeviceId;
    }

    private List<String> findDependentModules(String module) {
        List<String> dependentModules = new ArrayList<String>();
        dependentModules.add(module);
        boolean changed = true;
        while(changed){
            changed = false;
            for(AppEdge edge : getApplication().getEdges()){
                // check if there is any edge from a module hat is not in the dependency list yet
                // to a module that is in the dependency list. This way, the modules on the list depend
                // on this new module
                if(dependentModules.contains(edge.getDestination()) && edge.getDirection()==Tuple.UP
                    && !dependentModules.contains(edge.getSource())){

                    dependentModules.add(edge.getSource());
                    changed = true;
                }
            }
        }
        return dependentModules;
    }


    private Map<String, Integer> getAssociatedSensors(FogDevice device) {
        Map<String, Integer> endpoints = new HashMap<>();
        for(Sensor sensor : getSensors()) {
            if(sensor.getGatewayDeviceId() == device.getId()) {
                if(!endpoints.containsKey(sensor.getTupleType())) {
                    endpoints.put(sensor.getTupleType(), 0);
                }
                endpoints.put(sensor.getTupleType(), endpoints.get(sensor.getTupleType()) + 1);
            }
        }
        return endpoints;
    }

    private Map<String, Integer> getAssociatedActuators(FogDevice device) {
        Map<String, Integer> endpoints = new HashMap<>();
        for(Actuator actuator : getActuators()) {
            if(actuator.getGatewayDeviceId() == device.getId()) {
                if(!endpoints.containsKey(actuator.getActuatorType())) {
                    endpoints.put(actuator.getActuatorType(), 0);
                }
                endpoints.put(actuator.getActuatorType(), endpoints.get(actuator.getActuatorType()) + 1);
            }
        }
        return endpoints;
    }

    @SuppressWarnings("serial")
    protected List<List<Integer>> getPaths(final int fogDeviceId) {
        FogDevice device = (FogDevice) CloudSim.getEntity(fogDeviceId);
        // check if is leaf node
        if(device.getChildrenIds().size() == 0) {
            // if is leaf node, create a path with just this node and return
            final List<Integer> path = (new ArrayList<Integer>() {{
                add(fogDeviceId);
            }});
            List<List<Integer>> paths = (new ArrayList<List<Integer>>() {{
                add(path);
            }});
            return paths;
        }
        List<List<Integer>> paths = new ArrayList<>();
        // get all paths from leaves nodes to children of this node
        for(int childId : device.getChildrenIds()) {
            // get all paths from leaves nodes to the current child
            List<List<Integer>> childPaths = getPaths(childId);
            // insert the current device in the paths
            for(List<Integer> childPath : childPaths) {
                childPath.add(fogDeviceId);
            }
            paths.addAll(childPaths);
        }
        return paths;
    }

    protected List<List<Integer>> getLeafToRootPaths() {
        // gets the cloud device
        FogDevice cloud = null;
        for(FogDevice device : getFogDevices()) {
            if(device.getName().equals("cloud")) {
                cloud = device;
            }
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

    public Map<Integer, Map<String, List<Integer>>> getCurrentDeviceModuleInstanceList() {
        return currentDeviceModuleInstanceList;
    }

    public void setCurrentDeviceModuleInstanceList(
        Map<Integer, Map<String, List<Integer>>> currentDeviceModuleInstanceList) {
        this.currentDeviceModuleInstanceList = currentDeviceModuleInstanceList;
    }

}
