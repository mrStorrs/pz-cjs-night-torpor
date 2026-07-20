package com.cjstorrs.cjsnighttorpor;

public final class NightTorporPolicy {
    public static final int ACTIVE_DURING_DAY = 3;
    public static final float HEARING_MULTIPLIER = 0.25F;
    public static final float DETECTION_MULTIPLIER = 0.25F;
    public static final int MEMORY_CAP = 25;

    private NightTorporPolicy() {
    }

    public static boolean shouldReplaceActivityUpdate(int activeOnly) {
        return activeOnly == ACTIVE_DURING_DAY;
    }

    public static boolean isNightTorporActive(boolean inactive, int activeOnly) {
        return inactive && activeOnly == ACTIVE_DURING_DAY;
    }

    public static float adjustHearing(float multiplier, boolean torporActive) {
        return torporActive ? multiplier * HEARING_MULTIPLIER : multiplier;
    }
}
