package com.cjstorrs.cjsnighttorpor;

import zombie.SandboxOptions;
import zombie.characters.IsoZombie;

/** Public facade used by advice that ZombieBuddy inlines into vanilla classes. */
public final class NightTorporRuntime {
    private NightTorporRuntime() {
    }

    public static boolean replaceInactivityTransition(IsoZombie zombie, boolean requestedInactive) {
        if (requestedInactive == zombie.inactive) {
            return false;
        }

        int activeOnly = SandboxOptions.instance.lore.activeOnly.getValue();
        if (!NightTorporPolicy.shouldReplaceTransition(requestedInactive, zombie.inactive, activeOnly)) {
            return false;
        }

        // Preserve vanilla sensory torpor while bypassing makeInactive's speed mutations.
        zombie.inactive = requestedInactive;
        return true;
    }

    public static float adjustHearing(IsoZombie zombie, float multiplier) {
        if (!zombie.inactive) {
            return multiplier;
        }

        boolean torporActive = NightTorporPolicy.isNightTorporActive(
            zombie.inactive,
            SandboxOptions.instance.lore.activeOnly.getValue()
        );
        return NightTorporPolicy.adjustHearing(multiplier, torporActive);
    }
}
