package edu.wpi.alcogaitdatagatherer.models;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import edu.wpi.alcogaitdatagatherer.R;

/**
 * Created by Adonay on 11/21/2017.
 */

public enum WalkType {
    SBS("SIDE BY SIDE", "SIDE_BY_SIDE"),
    TS("TANDEM STANCE", "TANDEM_STANCE"),
    OLS("ONE LEG STANCE", "ONE_LEG_STANCE"),
    WT("36 METER WALK", "36M_WALK_TEST"),
    CRT("CHAIR RISE TEST", "CHAIR_RISE_TEST");

    String walkTypeString;
    String walkTypeStringNoSpace;

    WalkType(String walkTypeString, String walkTypeStringNoSpace) {
        this.walkTypeString = walkTypeString;
        this.walkTypeStringNoSpace = walkTypeStringNoSpace;
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

}
