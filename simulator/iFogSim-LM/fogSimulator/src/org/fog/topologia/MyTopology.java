package org.fog.topologia;

public class MyTopology {

    public static Node getTopology(char type){
        switch (type){
            case 'A':
                return getTopologyA();
            case 'C':
                return getTopologyC();
            case 'D':
                return getTopologyD();
            default:
                return null;
        }
    }

    private static Node getTopologyA(){
        Node level1 = new Node(75, 2000);
        return level1;
    }

    private static Node getTopologyC(){
        Node level1 = new Node(50, 4000);
        Node level2 = new Node(25, 2000);
        level1.setNext(level2);
        return level1;
    }

    private static Node getTopologyD(){
        Node level0 = new Node(50, 4000);
        Node level1 = new Node(25, 3000);
        Node level2 = new Node(25, 2000);

        level0.setNext(level1);
        level1.setNext(level2);
        return level0;
    }
}
