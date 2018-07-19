package edu.wpi.tjr_sensing.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Adonay on 11/30/2017.
 */

public class WalkHolder implements Serializable {
    private WalkType walkType;
    private HashMap<WalkType, Walk> walkMap;

    public WalkHolder() {
        walkMap = new HashMap<>();
    }

    public Walk getWalk(WalkType walkType) {
        return walkMap.get(walkType);
    }

    public WalkHolder addWalk(Walk walk) {
        walkMap.put(walk.getWalkType(), walk);
        return this;
    }

    public WalkHolder removeWalk(WalkType walkType) {
        walkMap.remove(walkType);
        return this;
    }

    public boolean hasWalk(WalkType walkType) {
        return walkMap.containsKey(walkType) || walkMap.get(walkType)!=null;
    }

    public int getSampleSize() {
        int total = 0;
        for (WalkType walkType : WalkType.values()) {
            if (walkMap.containsKey(walkType)) {
                total += walkMap.get(walkType).getSampleSize();
            }
        }
        return total;
    }

    public WalkType getWalkType() {
        return walkType;
    }

    public WalkHolder setWalkType(WalkType walkType) {
        this.walkType = walkType;
        return this;
    }

    public LinkedList<WalkType> getRecordedWalkTypeList(){
        LinkedList<WalkType> walkTypes = new LinkedList<>();
        for(WalkType type: WalkType.values()){
            if(hasWalk(type)){
                walkTypes.add(type);
            }
        }
        return walkTypes;
    }
}
