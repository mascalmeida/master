package org.fog.utils;

import org.fog.entities.FogDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListDevices extends ArrayList<FogDevice> {
    public List<Integer> getIds(){
        return this.stream().map(device -> device.getId()).collect(Collectors.toList());
    }

}
