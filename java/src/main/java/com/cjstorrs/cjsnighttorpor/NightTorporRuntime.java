package com.cjstorrs.cjsnighttorpor;

import zombie.GameTime;
import zombie.SandboxOptions;
import zombie.characters.IsoZombie;

/** Public facade used by advice that ZombieBuddy inlines into vanilla classes. */
public final class NightTorporRuntime {
    private NightTorporRuntime() {
    }

    public static boolean updateActivityWithoutChangingSpeed(IsoZombie zombie) {
        int activeOnly = SandboxOptions.instance.lore.activeOnly.getValue();
        if (!NightTorporPolicy.shouldReplaceActivityUpdate(activeOnly)) {
            return false;
        }

        // Keep vanilla sensory torpor while leaving every speed-related field untouched.
        zombie.inactive = GameTime.getInstance().isNight();
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
