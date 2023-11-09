package org.fog.placement;

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

import java.util.*;

public class ModulePlacementEdgewardsCommunicationAnalysis5 extends ModulePlacement{

    protected ModuleMapping moduleMapping;
    protected List<Sensor> sensors;
    protected List<Actuator> actuators;
    protected Map<Integer, Double> currentDeviceCpuLoad;
    protected Map<Integer, Double> currentDeviceCpuLoadRate;

    /**
     * Stores the current mapping of application modules to fog devices
     */
    protected Map<Integer, List<String>> currentDeviceModuleMap;
    protected Map<Integer, Map<String, Double>> currentDeviceModuleLoadMap;
    protected Map<Integer, Map<String, Double>> currentDeviceModuleLoadMapRate;
    protected Map<Integer, Map<String, Integer>> currentDeviceModuleInstanceNum;
    protected Map<Integer, Map<String, List<Integer>>> currentDeviceModuleInstanceList;
    protected Map<String, Application> applications;

    private boolean debugMode;
    static double DCNS_TRANSMISSION_TIME = 6;
    static double EEG_TRANSMISSION_TIME = 15;


    boolean useRealCpu = true;
    boolean individualUp = true;
    boolean networkAnalise = true;

    public ModulePlacementEdgewardsCommunicationAnalysis5(List<FogDevice> fogDevices,
                                                          List<Sensor> sensors,
                                                          List<Actuator> actuators,
                                                          Application application, ModuleMapping moduleMapping,
                                                          Map<Integer, Double> globalCPULoad,
                                                          Map<Integer, Double> globalCPULoadRate,
                                                          Map<String, Application> applications,
                                                          boolean debugMode){
        this.setFogDevices(fogDevices);
        this.setSensors(sensors);
        this.setActuators(actuators);
        this.setApplication(application);
        this.setModuleMapping(moduleMapping);
        this.setCurrentDeviceCpuLoad(globalCPULoad);
        this.setCurrentDeviceCpuLoadRate(globalCPULoadRate);
        this.debugMode = true;

        this.applications = applications;

        this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
        this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
        this.setCurrentDeviceModuleMap(new HashMap<Integer, List<String>>());
        this.setCurrentDeviceModuleLoadMap(new HashMap<Integer, Map<String, Double>>());
        this.setCurrentDeviceModuleLoadMapRate(new HashMap<Integer, Map<String, Double>>());
        this.setCurrentDeviceModuleInstanceNum(new HashMap<Integer, Map<String, Integer>>());
        this.setCurrentDeviceModuleInstanceList(new HashMap<>());

        for(FogDevice dev : getFogDevices()){
            getCurrentDeviceModuleLoadMap().put(dev.getId(), new HashMap<String, Double>());
            getCurrentDeviceModuleLoadMapRate().put(dev.getId(), new HashMap<String, Double>());
            getCurrentDeviceModuleMap().put(dev.getId(), new ArrayList<String>());
            getCurrentDeviceModuleInstanceNum().put(dev.getId(), new HashMap<String, Integer>());
            getCurrentDeviceModuleInstanceList().put(dev.getId(), new HashMap<>());
        }

        this.mapModules();

        this.setModuleInstanceCountMap(getCurrentDeviceModuleInstanceNum());
        this.setModuleInstanceListMap(getCurrentDeviceModuleInstanceList());
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
                getCurrentDeviceModuleLoadMapRate().get(deviceId).put(moduleName, 0.0);
                getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, 0);
                getCurrentDeviceModuleInstanceList().get(deviceId).put(moduleName, new ArrayList<>());
            }
        }

        // get all paths from leaves nodes to the cloud device
        List<List<Integer>> leafToRootPaths = getLeafToRootPaths();

        for(List<Integer> path : leafToRootPaths){
            placeModulesInPath(path);
        }

        for(int deviceId : getCurrentDeviceModuleMap().keySet()){
            for(String module : getCurrentDeviceModuleMap().get(deviceId)){
                if(!getCurrentDeviceModuleInstanceNum().get(deviceId).containsKey(module)
                        || getCurrentDeviceModuleInstanceNum().get(deviceId).get(module) == 0){
                    continue;
                }

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
                return sensor.getTransmitDistribution().getMeanInterTransmitTime();
        }
        return 0;
    }

    private List<Integer> getInstanceListToPlace(List<Integer> path) {
        loadFrequencyModuleEmitter();
        List<Integer> instanceList = new ArrayList<>();
        for(Sensor sensor : getSensors()) {
            if (path.contains(sensor.getGatewayDeviceId())
                    && !instanceList.contains(sensor.getAssociatedAppInstance())) {

                instanceList.add(sensor.getAssociatedAppInstance());
            }
        }
        return  instanceList;
    }

    private void placeModulesInPath(List<Integer> path) {
        if(path.size()==0)return;
        List<String> placedModules = new ArrayList<String>();
        List<Integer> instanceToPlace = getInstanceListToPlace(path);

        // for each device in the path
        for(Integer deviceId : path){

            FogDevice device = getFogDeviceById(deviceId);
            Map<String, Integer> sensorsAssociated = getAssociatedSensors(device);
            Map<String, Integer> actuatorsAssociated = getAssociatedActuators(device);

            // add all sensors and actuators to the placed list
            placedModules.addAll(sensorsAssociated.keySet());
            placedModules.addAll(actuatorsAssociated.keySet());

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
                //TODO: verificar se está pegando o dispostivo em nível mais elevado
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
                        if(!putModule(moduleName, deviceId, instanceToPlace)){
                            double totalCpuNeededRate = cpuNeeded(getApplication().getAppId(), moduleName);
                            if (this.debugMode) {
                                System.out.println("Placement of module " + moduleName + " NOT POSSIBLE on device " + device.getName());
                                System.out.println("Module CPU load = " + totalCpuNeededRate);
                                System.out.println("Current CPU load = " + getCurrentDeviceCpuLoadRate().get(deviceId));
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
                            //Conseguiu alocar
                            placedModules.add(moduleName);
                        }
                    }
                }else{
                    System.out.println("ERRRRORRRR");
//					// FINDING OUT WHETHER PLACEMENT OF MODULE ON DEVICE IS POSSIBLE
                    if (this.debugMode) {
                        System.out.println("Trying to place module "+moduleName);
                    }
                    //////****** CAUTION: HARD HACK ******//////
                    totalCpuLoad = getApplication().getModuleByName(moduleName).getMips();
                    System.out.println("CPU load for module " + moduleName + ": " + totalCpuLoad);
                    //////****** CAUTION: HARD HACK ******//////

                    if(putModule(moduleName, deviceId, instanceToPlace)) {
                        placedModules.add(moduleName);
                    } else {
                        System.out.println("ERRRRORRRR");
                    }

//                    if(totalCpuLoad + getCurrentDeviceCpuLoad().get(deviceId) > (device.getHost().getTotalMips()*1.2)){
//
//                        System.out.println("Placement of module "+moduleName+ " NOT POSSIBLE on device "+device.getName());
//                        System.out.println("CPU load = "+totalCpuLoad);
//                        System.out.println("Current CPU load = "+ getCurrentDeviceCpuLoad().get(deviceId));
//                        System.out.println("Max mips = "+device.getHost().getTotalMips());
//                    } else{
//
//                        System.out.println("Placement of module "+moduleName+ " on device "+device.getName());
//                        // TODO: não utilizar o valor do módulo, utilizar utilizacao p/ms
//                        getCurrentDeviceCpuLoad().put(deviceId, totalCpuLoad + getCurrentDeviceCpuLoad().get(deviceId));
//
//                        System.out.println("Updated CPU load = "+ getCurrentDeviceCpuLoad().get(deviceId));
//                        if(!currentDeviceModuleMap.containsKey(deviceId))
//                            currentDeviceModuleMap.put(deviceId, new ArrayList<String>());
//                        currentDeviceModuleMap.get(deviceId).add(moduleName);
//                        placedModules.add(moduleName);
//                        modulesToPlace = getModulesToPlace(placedModules);
//                        getCurrentDeviceModuleLoadMap().get(device.getId()).put(moduleName, totalCpuLoad);
//
//                        int max = 1;
//                        for(AppEdge edge : getApplication().getEdges()){
//                            if(edge.getSource().equals(moduleName) && actuatorsAssociated.containsKey(edge.getDestination()))
//                                max = Math.max(actuatorsAssociated.get(edge.getDestination()), max);
//                            if(edge.getDestination().equals(moduleName) && sensorsAssociated.containsKey(edge.getSource()))
//                                max = Math.max(sensorsAssociated.get(edge.getSource()), max);
//                        }
//
////                        if(!currentDeviceModuleInstanceNumPersistent.containsKey(deviceId)){
////                            currentDeviceModuleInstanceNumPersistent.put(deviceId, new HashMap<>());
////                        }
////                        currentDeviceModuleInstanceNumPersistent.get(deviceId).put(moduleName, max);
//
//                        getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, max);
//                        System.out.println("Number of instances for module "+moduleName+" = "+max);
//
//                    }
                }

                if(shouldRemoveModuleToPlace) {
                    modulesToPlace.remove(moduleName);
                }
            }
        }
    }

    Map<String, Double> sensorFrequencyEmmiter = new HashMap<>();

    private void loadFrequencyModuleEmitter(){
        Application application = getApplication();
        application.getEdges().forEach(appEdge -> {
            if(appEdge.getEdgeType() == AppEdge.SENSOR){
                sensorFrequencyEmmiter.put(application.getAppId(), getRateOfSensor(appEdge.getTupleType())*0.85);
            }
        });
    }

    public double getFrequency(String application, String module){
        return sensorFrequencyEmmiter.get(application);
    }

    private double cpuNeeded(String applicationName, String module){
        if(useRealCpu) {
            double cpuNeed = applications.get(applicationName).getEdges().stream()
                    .filter(appEdge -> appEdge.getEdgeType() == AppEdge.MODULE)
                    .filter(appEdge -> appEdge.getDestination().equals(module))
                    .map(appEdge -> appEdge.getTupleCpuLength().getMeanInterTransmitTime() /
                            getFrequency(applicationName, module))
                    .mapToDouble(Double::doubleValue)
                    .sum();

            return cpuNeed;
        } else {
            return applications.get(applicationName).getModuleByName(module).getMips();
        }
    }


    private List<Integer> removeModulesInstancesFromDevice(int deviceId, String moduleName, int numberOfModules){
        double cpuOfModule = cpuNeeded(getApplication().getAppId(), moduleName);

        if(getCurrentDeviceModuleLoadMap().get(deviceId).containsKey(moduleName)){
            double actualCPULoad = getCurrentDeviceModuleLoadMap().get(deviceId).get(moduleName);
            double finalCpuload = actualCPULoad - (cpuOfModule * numberOfModules);

            if(finalCpuload<=0){
                getCurrentDeviceModuleLoadMap().get(deviceId).remove(moduleName);
            } else {
                getCurrentDeviceModuleLoadMap().get(deviceId).put(moduleName, finalCpuload);
            }

        }
        if(getCurrentDeviceCpuLoadRate().containsKey(deviceId)){
            double actualCPULoad = getCurrentDeviceCpuLoadRate().get(deviceId);
            double finalCpuload = actualCPULoad - (cpuOfModule * numberOfModules);

            if(finalCpuload<=0){
                getCurrentDeviceCpuLoadRate().put(deviceId, 0.0);
            } else {
                getCurrentDeviceCpuLoadRate().put(deviceId, finalCpuload);
            }

        }
        if(getCurrentDeviceModuleLoadMapRate().get(deviceId).containsKey(moduleName)){
            double actualCPULoad = getCurrentDeviceModuleLoadMapRate().get(deviceId).get(moduleName);
            double finalCpuload = actualCPULoad - (cpuOfModule * numberOfModules);

            if(finalCpuload<=0){
                getCurrentDeviceModuleLoadMapRate().get(deviceId).remove(moduleName);
            } else {
                getCurrentDeviceModuleLoadMapRate().get(deviceId).put(moduleName, finalCpuload);
            }
        }
        if(getCurrentDeviceModuleInstanceNum().get(deviceId).containsKey(moduleName)){
            int actualCount = getCurrentDeviceModuleInstanceNum().get(deviceId).get(moduleName);
            int finalCount = actualCount -  numberOfModules;

            if(finalCount<=0){
                getCurrentDeviceModuleInstanceNum().get(deviceId).remove(moduleName);
            } else {
                getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, finalCount);
            }
        }

        if(!getCurrentDeviceModuleInstanceNum().get(deviceId).containsKey(moduleName) || getCurrentDeviceModuleInstanceNum().get(deviceId).get(moduleName)==0){
            getCurrentDeviceModuleMap().get(deviceId).remove(moduleName);
        }

        List<Integer> instancesToPlace = new ArrayList<>();
        List<Integer> currentInstances = getCurrentDeviceModuleInstanceList().get(deviceId).get(moduleName);

        for(int i=0;i< numberOfModules; i++){
            Integer instance = currentInstances.remove(currentInstances.size()-1);
            instancesToPlace.add(instance);
        }

        Collections.sort(instancesToPlace);

        return instancesToPlace;
    }



    private boolean putModule(String moduleName, Integer deviceId, List<Integer> instanceToPlace){
        FogDevice device = getFogDeviceById(deviceId);


        double currentCpuLoadRate = getCurrentDeviceCpuLoadRate().get(deviceId);
        double totalCpuNeededRate = cpuNeeded(getApplication().getAppId(), moduleName);
        double totalDeviceMips = device.getHost().getTotalMips();

        System.out.println("Trying to place " + moduleName + " on device " + device.getName()
                + " - current CPU load = " + currentCpuLoadRate);

        if (this.debugMode) {
            System.out.println("CPU load for module " + moduleName + ": " + totalCpuNeededRate + "mips/ms");
        }

        boolean temEspaco = totalCpuNeededRate + currentCpuLoadRate <= (getFogDeviceById(deviceId).getHost().getTotalMips());

        //caso exista espaço
        if(temEspaco){
            currentCpuLoadRate += totalCpuNeededRate;
            getCurrentDeviceCpuLoadRate().put(deviceId, currentCpuLoadRate);

            if(!getCurrentDeviceModuleInstanceNum().containsKey(deviceId)){
                getCurrentDeviceModuleInstanceNum().put(deviceId, new HashMap<>());
            }

            int finalInstancesCount = getCurrentDeviceModuleInstanceNum().get(deviceId).getOrDefault(moduleName, 0)+1;
            getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, finalInstancesCount);

            //getCurrentDeviceModuleMap().get(deviceId).remove(moduleName);
            if(!getCurrentDeviceModuleMap().get(deviceId).contains(moduleName)){
                getCurrentDeviceModuleMap().get(deviceId).add(moduleName);
            }

            if(!getCurrentDeviceModuleInstanceList().get(deviceId).containsKey(moduleName)){
                getCurrentDeviceModuleInstanceList().get(deviceId).put(moduleName, new ArrayList<>());
            }

            getCurrentDeviceModuleInstanceList().get(deviceId).get(moduleName).addAll(instanceToPlace);
            Collections.sort(getCurrentDeviceModuleInstanceList().get(deviceId).get(moduleName));

            if (this.debugMode) {
                System.out.println(moduleName+" can be created in "+device.getName()
                        + " (" + currentCpuLoadRate/totalDeviceMips +"% cpu in use = "
                        + currentCpuLoadRate + " / " + totalDeviceMips + ")");
            }

            return true;
        } else {

            if (this.debugMode) {
                System.out.println("Placement of module " + moduleName + " NOT POSSIBLE on device " + device.getName());
                System.out.println("Module CPU load = " + totalCpuNeededRate);
                System.out.println("Current CPU load = " + getCurrentDeviceCpuLoadRate().get(deviceId));
                System.out.println("Max mips = " + device.getHost().getTotalMips());
                System.out.println("Need to shift module " + moduleName + " somewhere upstream.");
            }


            int parentDevice = getParentDevice(deviceId);

            if(!networkAnalise){
                return putModule(moduleName, parentDevice, instanceToPlace);
            }

            List<String> modulesToShift = findModulesWithLessCommunicationImpactToShift(moduleName, deviceId);

            //Se o módulo foi selecionado para subir, apenas encaminha para cima
            if(modulesToShift.contains(moduleName)){
                System.out.println("Module "+ moduleName + "select to up");
                return putModule(moduleName, parentDevice, instanceToPlace);
            }

            //Neeed to shift some modulesToshift
            int instancesToShift = 0;
            if (this.debugMode) {
                System.out.println("Modules to shift northwards : "+modulesToShift);
            }
            if(modulesToShift.size()>0) {
                double cpuLoadRateNeed = cpuNeeded(getApplication().getAppId(), moduleName);

                double cpuLoadRateModuleToShift = modulesToShift.stream()
                                .map(module -> cpuNeeded(getApplication().getAppId(), module))
                                .reduce(0., Double::sum);

                System.out.println("TotalCPUNeed:" + getCurrentDeviceCpuLoadRate().get(deviceId) + " ,TotalCPUOnDevice:" + getDeviceById(deviceId).getHost().getTotalMips());
                System.out.println("CPU Need:" + cpuLoadRateNeed + " ,CPUMOduleToShift:" + cpuLoadRateModuleToShift);
                double totalCPUNeedToDeployOnDevice = ((getCurrentDeviceCpuLoadRate().get(deviceId)+cpuLoadRateNeed)-getDeviceById(deviceId).getHost().getTotalMips());
                System.out.println("Diference CPU Need:" + totalCPUNeedToDeployOnDevice);
                instancesToShift = (int) Math.ceil(totalCPUNeedToDeployOnDevice/cpuLoadRateModuleToShift);
                System.out.println("Number of Modules to Shift:" + instancesToShift);
            }

            if(!individualUp) {
                instancesToShift = getCurrentDeviceModuleInstanceNum().get(deviceId).getOrDefault(modulesToShift.get(0),0);
            }

            //verifica se da pra subir
            int finalInstancesToShift = instancesToShift;
            boolean existemModulosSuficientesParaElevar = modulesToShift.stream()
                    .map(module -> {
                        return getCurrentDeviceModuleInstanceNum().get(deviceId).getOrDefault(module,0)>= finalInstancesToShift;
                    })
                    .reduce(true, Boolean::logicalAnd);

            if(existemModulosSuficientesParaElevar) {
                modulesToShift.forEach(module -> {
                    List<Integer> instances = removeModulesInstancesFromDevice(deviceId, module, finalInstancesToShift);
                    for (int i = 0; i < finalInstancesToShift; i++) {
                        putModule(module, parentDevice, Arrays.asList(instances.get(i)));
                    }
                });
                return putModule(moduleName, deviceId, instanceToPlace);
            } else {
                System.out.println("Sem módulos para elevar, "+ moduleName + "select to up");
                //force up
                return putModule(moduleName, parentDevice, instanceToPlace);
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

        int instancesToShift = 0;
        if (this.debugMode) {
            System.out.println("Modules to shift northwards : "+modulesToShift);
        }
        if(modulesToShift.size()>0) {
            double cpuLoadRateNeed = cpuNeeded(getApplication().getAppId(), moduleName);
            double cpuLoadRateModuleToShift = cpuNeeded(getApplication().getAppId(), modulesToShift.get(0));

            System.out.println("TotalCPUNeed:" + getCurrentDeviceCpuLoadRate().get(deviceId) + " ,TotalCPUOnDevice:" + getDeviceById(deviceId).getHost().getTotalMips());
            System.out.println("CPU Need:" + cpuLoadRateNeed + " ,CPUMOduleToShift:" + cpuLoadRateModuleToShift);
            double totalCPUNeedToDeployOnDevice = ((getCurrentDeviceCpuLoadRate().get(deviceId)+cpuLoadRateNeed)-getDeviceById(deviceId).getHost().getTotalMips());
            System.out.println("Diference CPU Need:" + totalCPUNeedToDeployOnDevice);
            instancesToShift = (int) Math.ceil(totalCPUNeedToDeployOnDevice/cpuLoadRateModuleToShift);
            System.out.println("Number of Modules to Shift:" + instancesToShift);
        }

        Map<String, Integer> moduleToNumInstances = new HashMap<String, Integer>();
        double totalCpuLoad = 0;
        double totalCpuLoadRate = 0;

        // updated module load map
        Map<String, Double> loadMap = new HashMap<String, Double>();
        Map<String, Double> loadMapRate = new HashMap<String, Double>();
        // move the modules
        for(String module : modulesToShift){
            loadMap.put(module, getCurrentDeviceModuleLoadMap().get(deviceId).getOrDefault(module,0.0));
            loadMapRate.put(module, getCurrentDeviceModuleLoadMapRate().get(deviceId).getOrDefault(module,0.0));
            // updates the module instance count
            moduleToNumInstances.put(module, instancesToShift);

            /* totalCpuLoad += getCurrentDeviceModuleLoadMap().get(deviceId).get(module); */

            //////****** CAUTION: HARD HACK ******//////
            // get quantity of modules to shift and the total module load
            int moduleCount = moduleToNumInstances.get(module);
            double moduleCpuLoad = getApplication().getModuleByName(module).getMips()*moduleCount;
            double moduleCpuLoadRate = cpuNeeded(getApplication().getAppId(), module)*moduleCount;
            if (this.debugMode) {
                System.out.println("CPU load for " + moduleCount + " " + module + " modules: " + moduleCpuLoadRate);
            }
            //////****** CAUTION: HARD HACK ******//////

            // updates the total moved CPU load
            totalCpuLoad += moduleCpuLoad;
            totalCpuLoadRate += moduleCpuLoadRate;

            // remove the module from the original device
            removeModulesInstancesFromDevice(deviceId, module, moduleCount);
//            getCurrentDeviceModuleLoadMap().get(deviceId).remove(module);
//
//            getCurrentDeviceModuleLoadMapRate().get(deviceId).remove(module);
//
//            getCurrentDeviceModuleMap().get(deviceId).remove(module);
//            getCurrentDeviceModuleInstanceNum().get(deviceId).remove(module);

            //LEO
//            currentDeviceModuleInstanceNumPersistent.get(deviceId).remove(module);

            // updates the module load
            loadMap.put(module, loadMap.get(module)+getApplication().getModuleByName(module).getMips());
            loadMapRate.put(module, loadMapRate.get(module)+ cpuNeeded(getApplication().getAppId(), module));
        }

        if (this.debugMode) {
            System.out.println("Current device load on " + getDeviceById(deviceId).getName() + ": " + getCurrentDeviceCpuLoadRate().get(deviceId));
            System.out.println("Load after removal on " + getDeviceById(deviceId).getName() + ": " + (getCurrentDeviceCpuLoadRate().get(deviceId) - totalCpuLoadRate));
        }

        // reduce the load of the original device
        // TODO: não utilizar o valor do módulo, utilizar utilizacao p/ms
        getCurrentDeviceCpuLoad().put(deviceId, getCurrentDeviceCpuLoad().get(deviceId)-totalCpuLoad);
        getCurrentDeviceCpuLoadRate().put(deviceId, getCurrentDeviceCpuLoadRate().get(deviceId)-totalCpuLoadRate);

        int lowestInstanceNumber = moduleToNumInstances.get(modulesToShift.get(0));
        for(String module : modulesToShift) {
            if(moduleToNumInstances.get(module) < lowestInstanceNumber) {
                lowestInstanceNumber = moduleToNumInstances.get(module);
            }
        }

        for(String module : modulesToShift) {
            if(moduleToNumInstances.get(module) == lowestInstanceNumber) {
                moduleToNumInstances.put(module, moduleToNumInstances.get(module) +1);
                double moduleCpuLoad = getApplication().getModuleByName(module).getMips();
                double moduleCpuLoadRate = cpuNeeded(getApplication().getAppId(), module);
                totalCpuLoad += moduleCpuLoad;
                totalCpuLoadRate += moduleCpuLoadRate;
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
            if(getCurrentDeviceCpuLoadRate().get(id) + totalCpuLoadRate > fogDevice.getHost().getTotalMips()){
                // if not, update the list of modules that should be shifted
                // because some of the modules at this device might depend on the result of some modules to shift
                //List<String> _modulesToShift = findModulesToShift(modulesToShift, id);
                // the total cpu load shifted from device id to its parent
                List<String> _modulesToShift = modulesToShift;
                double cpuLoadShifted = 0;
                double cpuLoadShiftedRate = 0;
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
                        double moduleCpuLoadRate = cpuNeeded(getApplication().getAppId(), module)*deviceModuleCount;
                        if (this.debugMode) {
                            System.out.println("CPU load for " + deviceModuleCount + " " + module + " modules: " + moduleCpuLoad);
                        }
                        cpuLoadShifted += moduleCpuLoad;
                        cpuLoadShiftedRate += moduleCpuLoadRate;
                        totalCpuLoad += moduleCpuLoad;
                        totalCpuLoadRate += moduleCpuLoadRate;
                        //////****** CAUTION: HARD HACK ******//////

                        // remove the module from the device
                        getCurrentDeviceModuleLoadMap().get(id).remove(module);
                        getCurrentDeviceModuleLoadMapRate().get(id).remove(module);
                        getCurrentDeviceModuleMap().get(id).remove(module);
                        getCurrentDeviceModuleInstanceNum().get(id).remove(module);

//                        //LEO
//                        if(!currentDeviceModuleInstanceNumPersistent.containsKey(id)){
//                            currentDeviceModuleInstanceNumPersistent.put(id, new HashMap<>());
//                        }
//                        currentDeviceModuleInstanceNumPersistent.get(id).remove(module);
                    }
                }
                // updates the device load
                // TODO: não utilizar o valor do módulo, utilizar utilizacao p/ms
                getCurrentDeviceCpuLoad().put(id, getCurrentDeviceCpuLoad().get(id)-cpuLoadShifted);
                getCurrentDeviceCpuLoadRate().put(id, getCurrentDeviceCpuLoadRate().get(id)-cpuLoadShiftedRate);

                if (this.debugMode) {
                    System.out.println("CPU load after module removal on device "+CloudSim.getEntityName(id)+" = "+ getCurrentDeviceCpuLoadRate().get(id));
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
                double totalLoadRate = getCurrentDeviceCpuLoadRate().get(id);
                if (this.debugMode) {
                    System.out.println("Current CPU load on device "+CloudSim.getEntityName(id)+" = "+ totalLoadRate);
                }
                // get total load and final instance count
                for(String module : loadMap.keySet()){
                    double moduleCpuLoad = loadMap.get(module);
                    double moduleCpuLoadRate = loadMapRate.get(module);
                    int modulesToPlaceCount = moduleToNumInstances.get(module);
                    totalLoad += moduleCpuLoad * modulesToPlaceCount;
                    totalLoadRate += moduleCpuLoadRate * modulesToPlaceCount;

                    getCurrentDeviceModuleLoadMap().get(id).put(module, moduleCpuLoad);
                    getCurrentDeviceModuleLoadMapRate().get(id).put(module, moduleCpuLoadRate);
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

                    //LEO
//                    currentDeviceModuleInstanceNumPersistent.get(deviceId).put(module_, finalNumInstances);
                }

                // updates device load
                // TODO: não utilizar o valor do módulo, utilizar utilizacao p/ms
                getCurrentDeviceCpuLoad().put(id, totalLoad);
                getCurrentDeviceCpuLoadRate().put(id, totalLoadRate);
                modulesToPlace.removeAll(loadMap.keySet());

                if (this.debugMode) {
                    System.out.println("Final CPU load on device "+CloudSim.getEntityName(id)+" = "+ getCurrentDeviceCpuLoadRate().get(id));
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

        //Identifica os módulos presentes no dispostivo atual
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

    public Map<Integer, Double> getCurrentDeviceCpuLoadRate() {
        return currentDeviceCpuLoadRate;
    }

    public void setCurrentDeviceCpuLoadRate(Map<Integer, Double> currentDeviceCpuLoadRate) {
        this.currentDeviceCpuLoadRate = currentDeviceCpuLoadRate;
    }

    public Map<Integer, Map<String, Double>> getCurrentDeviceModuleLoadMapRate() {
        return currentDeviceModuleLoadMapRate;
    }

    public void setCurrentDeviceModuleLoadMapRate(Map<Integer, Map<String, Double>> currentDeviceModuleLoadMapRate) {
        this.currentDeviceModuleLoadMapRate = currentDeviceModuleLoadMapRate;
    }

    public Map<Integer, Map<String, List<Integer>>> getCurrentDeviceModuleInstanceList() {
        return currentDeviceModuleInstanceList;
    }

    public void setCurrentDeviceModuleInstanceList(Map<Integer, Map<String, List<Integer>>> currentDeviceModuleInstanceList) {
        this.currentDeviceModuleInstanceList = currentDeviceModuleInstanceList;
    }
}
