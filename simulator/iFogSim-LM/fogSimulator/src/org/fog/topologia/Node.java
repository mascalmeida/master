package org.fog.topologia;

public class Node {
    private long upLinkLatency;
    private long totalMips;
    private Node next;

    public Node(long upDelay, long totalMips) {
        this.upLinkLatency = upDelay;
        this.totalMips = totalMips;
    }

    public Node(long upDelay, long totalMips, Node next) {
        this.upLinkLatency = upDelay;
        this.totalMips = totalMips;
        this.next = next;
    }

    public long getUpLinkLatency() {
        return upLinkLatency;
    }

    public long getTotalMips() {
        return totalMips;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
