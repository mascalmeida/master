package org.fog.test.my.topology;

import org.fog.test.my.utils.HostConfig;
import org.fog.test.my.utils.Level;

public class TopologyA2 extends Topology {

    private void configNetwork(){
        Level top = new Level(1, new HostConfig(44800, 40000, 10000, 10000, 0.011), "cloud",0);
        Level botton = top
                .addNext(new Level(1, new HostConfig(1, 4000, 10000, 10000, 0.0), "proxy-server",100))
                .addNext(new Level(3, new HostConfig(2000, 4000, 10000, 10000,0.0), "cloudlet",75));

        setTop(top);
        setBottom(botton);
    }

    public TopologyA2() {
        super("A2", 4);
        configNetwork();
    }
}
