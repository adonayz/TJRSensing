package edu.wpi.alcogaitdatagatherer.models;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Adonay on 11/30/2017.
 */

public class WalkHolder implements Serializable {
    private WalkType walkType;
    private LinkedList<Walk> walks;

    public WalkHolder(WalkType walkType) {
        walks = new LinkedList<>();
        this.walkType = walkType;
    }

    public int getWalkNumber() {
        return walks.size()+1;
    }

    public Walk get(int walkNumber) {
        return walks.get(walkNumber-1);
    }

    public WalkHolder addWalk(Walk walk) {
        walks.add(walk);
        return this;
    }

    public WalkHolder removeWalk(int walkNumber) {
        walks.remove(walkNumber-1);
        return this;
    }

    public int getSampleSize() {
        int total = 0;
        for (Walk walk : walks) {
            total+=walk.getSampleSize();
        }
        return total;
    }

    public WalkType getWalkType() {
        return walkType;
    }

    public void setWalkType(WalkType walkType) {
        this.walkType = walkType;
    }
}
