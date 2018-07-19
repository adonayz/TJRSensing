package edu.wpi.tjr_sensing.models;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import edu.wpi.tjr_sensing.R;

/**
 * Created by Adonay on 11/21/2017.
 */

public enum WalkType {
    SBS("SIDE BY SIDE", "SIDE_BY_SIDE", 30),
    TS("TANDEM STANCE", "TANDEM_STANCE", 30),
    OLS("ONE LEG STANCE", "ONE_LEG_STANCE", 30),
    WT("36 METER WALK", "36M_WALK_TEST", 60),
    CRT("CHAIR RISE TEST", "CHAIR_RISE_TEST", 30);

    String walkTypeString;
    String walkTypeStringNoSpace;
    int testTime;

    WalkType(String walkTypeString, String walkTypeStringNoSpace, int testTime) {
        this.walkTypeString = walkTypeString;
        this.walkTypeStringNoSpace = walkTypeStringNoSpace;
        this.testTime = testTime;
    }

    @Override
    public String toString() {
        return walkTypeString;
    }

    public String toNoSpaceString() {
        return walkTypeStringNoSpace;
    }

    public WalkType next(WalkType prevWalkType) {
        for (int i = 0; i < WalkType.values().length - 1; i++) {
            if (prevWalkType == WalkType.values()[i]) {
                return WalkType.values()[i + 1];
            }
        }
        return null;
    }

    @NonNull
    public String getInstructions(Context context) {
        switch (this) {
            case SBS:
                return context.getResources().getString(R.string.sbs_instructions);
            case TS:
                return context.getResources().getString(R.string.ts_instructions);
            case WT:
                return context.getResources().getString(R.string.wt_instructions);
            case CRT:
                return context.getResources().getString(R.string.crt_instructions);
            case OLS:
                return context.getResources().getString(R.string.ols_instructions);
            default:
                return context.getResources().getString(R.string.walk_instructions_detail);
        }
    }

    public int getTime(){
        return testTime;
    }

}
