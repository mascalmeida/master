package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.utils.ListDevices;

public class MyModulePlacement extends ModulePlacement{

    private ModuleMapping moduleMapping;
    protected Map<Integer, Double> currentCpuLoad;

    private List<Sensor> sensores;
    private List<Actuator> atuadores;


    protected Map<Integer, List<String>> currentDeviceModuleMap;
//    protected Map<Integer, Map<String, Double>> currentDeviceModuleLoadMap;
    protected Map<Integer, Map<String, Integer>> currentDeviceModuleInstanceNum;
//    protected Map<Integer, Map<String, List<Integer>>> currentDeviceModuleInstanceList;

    private boolean debugMode;

    public Map<Integer, List<String>> getCurrentDeviceModuleMap() {
        return currentDeviceModuleMap;
    }

    public void setCurrentDeviceModuleMap(Map<Integer, List<String>> currentDeviceModuleMap) {
        this.currentDeviceModuleMap = currentDeviceModuleMap;
    }

    public Map<Integer, Map<String, Integer>> getCurrentDeviceModuleInstanceNum() {
        return currentDeviceModuleInstanceNum;
    }

    public void setCurrentDeviceModuleInstanceNum(Map<Integer, Map<String, Integer>> currentDeviceModuleInstanceNum) {
        this.currentDeviceModuleInstanceNum = currentDeviceModuleInstanceNum;
    }

    private void iniciaValores(){
        for(FogDevice dev : getFogDevices()) {
//            getCurrentDeviceModuleLoadMap().put(dev.getId(), new HashMap<>());
            getCurrentDeviceModuleMap().put(dev.getId(), new ArrayList<>());
            getCurrentDeviceModuleInstanceNum().put(dev.getId(), new HashMap<>());
//            getCurrentDeviceModuleInstanceList().put(dev.getId(), new HashMap<>());
            getModuleInstanceCountMap().put(dev.getId(), new HashMap<String, Integer>());
        }

        // get all devices
        for(String deviceName : getModuleMapping().getNodeToModuleCountMapping().keySet()) {
            // get all modules for the device
            for(String moduleName : getModuleMapping().getNodeToModuleCountMapping().get(deviceName).keySet()) {
                // update the current device to module map
                int deviceId = CloudSim.getEntityId(deviceName);
                getCurrentDeviceModuleMap().get(deviceId).add(moduleName);
//                getCurrentDeviceModuleLoadMap().get(deviceId).put(moduleName, 0.0);
                getCurrentDeviceModuleInstanceNum().get(deviceId).put(moduleName, 0);
//                getCurrentDeviceModuleInstanceList().get(deviceId).put(moduleName, new ArrayList<>());
            }
        }
    }

    class InstanceCount {
        AppModule module;
        int count;
    }
    private void teste(){
       /* getFogDevices().stream()
                .forEach(device -> {
                    System.out.println(device);
                });*/


        getModuleMapping();

    }

    protected List<ListDevices> getPaths(int fogDeviceId) {
        FogDevice device = (FogDevice) CloudSim.getEntity(fogDeviceId);
        // check if is leaf node
        if(device.getChildrenIds().size() == 0) {
            // if is leaf node, create a path with just this node and return
            final ListDevices path = new ListDevices() {{
                add(device);
            }};
            List<ListDevices> paths = (new ArrayList<ListDevices>() {{
                add(path);
            }});
            return paths;
        }
        List<ListDevices> paths = new ArrayList<>();
        // get all paths from leaves nodes to children of this node
        for(int childId : device.getChildrenIds()) {
            // get all paths from leaves nodes to the current child
            List<ListDevices> childPaths = getPaths(childId);
            // insert the current device in the paths
            for(List<FogDevice> childPath : childPaths) {
                childPath.add(device);
            }
            paths.addAll(childPaths);
        }
        return paths;
    }

    protected List<ListDevices> getLeafToRootPaths() {
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

    private void alocarModulosDoCaminho(ListDevices caminho){
        if(caminho.isEmpty())
            return;

        List<String> modulosAlocados = new ArrayList<>();
        //coleta inicialmente as instancias de sensores e atuadores que precisam ser alocados
        List<Integer> instanceToPlace = getInstanceListToPlace(caminho);

        for (FogDevice device : caminho) {
            Map<String, Integer> sensorsAssociated = getAssociatedSensors(device);
            Map<String, Integer> actuatorsAssociated = getAssociatedActuators(device);

            modulosAlocados.addAll(sensorsAssociated.keySet());
            modulosAlocados.addAll(actuatorsAssociated.keySet());


            List<String> modulesToPlace = getModulesToPlace(modulosAlocados);

            while(!modulesToPlace.isEmpty()){

            }
            System.out.println(modulesToPlace);
        }
    }


    /*
    retorna uma lista de modulos que precisam ser alocados
    * */
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

    private Map<String, Integer> getAssociatedActuators(FogDevice device) {
        Map<String, Integer> endpoints = new HashMap<>();
        for(Actuator actuator : getAtuadores()) {
            if(actuator.getGatewayDeviceId() == device.getId()) {
                if(!endpoints.containsKey(actuator.getActuatorType())) {
                    endpoints.put(actuator.getActuatorType(), 0);
                }
                endpoints.put(actuator.getActuatorType(), endpoints.get(actuator.getActuatorType()) + 1);
            }
        }
        return endpoints;
    }

    private Map<String, Integer> getAssociatedSensors(FogDevice device) {
        Map<String, Integer> endpoints = new HashMap<>();
        for(Sensor sensor : getSensores()) {
            if(sensor.getGatewayDeviceId() == device.getId()) {
                if(!endpoints.containsKey(sensor.getTupleType())) {
                    endpoints.put(sensor.getTupleType(), 0);
                }
                endpoints.put(sensor.getTupleType(), endpoints.get(sensor.getTupleType()) + 1);
            }
        }
        return endpoints;
    }

    private List<Integer> getInstanceListToPlace(ListDevices path) {
        List<Integer> instanceList = new ArrayList<>();
        for(Sensor sensor : getSensores()) {
            if (path.getIds().contains(sensor.getGatewayDeviceId())
                    && !instanceList.contains(sensor.getAssociatedAppInstance())) {

                instanceList.add(sensor.getAssociatedAppInstance());
            }
        }
        return  instanceList;
    }

    @Override
    protected void mapModules() {
        iniciaValores();

        //Seleciona todas as rotas disponíveis de cada folha (dispositivo mais próximo do usuário até a cloud)
        List<ListDevices> leafToRootPaths = getLeafToRootPaths();

        for (ListDevices caminho : leafToRootPaths) {
            alocarModulosDoCaminho(caminho);
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



//        Map<String, Map<String, Integer>> mapping = moduleMapping.getNodeToModuleCountMapping();
//        for(String deviceName : mapping.keySet()){
//            FogDevice device = getDeviceByName(deviceName);
//            for(String moduleName : mapping.get(deviceName).keySet()){
//                AppModule module = getApplication().getModuleByName(moduleName);
//                if(module == null)
//                    continue;
//                //TODO: compute rates for CPU load of incoming edges?
//                int numModules=mapping.get(deviceName).get(moduleName).intValue();
//                Double currentCpuLoad = getCurrentCpuLoad().get(device.getId());
//                Double newCpuLoad = currentCpuLoad + numModules * module.getMips();
//                if (this.debugMode) {
//                    System.out.println("MAPPING " + numModules + " " + module.getName() + " on " + device.getName()
//                            + " - old CPU load = "+currentCpuLoad + " new CPU load = "
//                            + newCpuLoad);
//                }
//                // updates the device cpu load
//                getCurrentCpuLoad().put(device.getId(), newCpuLoad);
//
//                // creates an instance of the module in the device if possible
//                createModuleInstanceOnDevice(module, device);
//                // updates the quantity of modules in the device
//                getModuleInstanceCountMap().get(device.getId()).put(moduleName, numModules);
//            }
//        }
    }

    public Map<Integer, Double> getCurrentCpuLoad() {
        return currentCpuLoad;
    }

    public void setCurrentCpuLoad(Map<Integer, Double> currentCpuLoad) {
        this.currentCpuLoad= currentCpuLoad;
    }

    List<Application> applications;

    public MyModulePlacement(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, List<Application> applications,
                             ModuleMapping moduleMapping, Map<Integer, Double> globalCPULoad, boolean debugMode){
        this.debugMode = debugMode;
        this.setFogDevices(fogDevices);
        this.applications = applications;
        this.setModuleMapping(moduleMapping);
        setCurrentCpuLoad(globalCPULoad);
        this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
        this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
        this.setModuleInstanceCountMap(new HashMap<Integer, Map<String, Integer>>());

        setCurrentDeviceModuleMap(new HashMap<>());
        setCurrentDeviceModuleInstanceNum(new HashMap<>());
        this.setSensores(sensors);
        this.setAtuadores(actuators);
        mapModules();
    }


    public ModuleMapping getModuleMapping() {
        return moduleMapping;
    }
    public void setModuleMapping(ModuleMapping moduleMapping) {
        this.moduleMapping = moduleMapping;
    }

    public List<Sensor> getSensores() {
        return sensores;
    }

    public void setSensores(List<Sensor> sensores) {
        this.sensores = sensores;
    }

    public List<Actuator> getAtuadores() {
        return atuadores;
    }

    public void setAtuadores(List<Actuator> atuadores) {
        this.atuadores = atuadores;
    }
}
