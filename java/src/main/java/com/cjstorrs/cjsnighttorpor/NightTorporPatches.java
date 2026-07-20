package com.cjstorrs.cjsnighttorpor;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.characters.IsoZombie;

public final class NightTorporPatches {
    private NightTorporPatches() {
    }

    @Patch(
        className = "zombie.characters.IsoZombie",
        methodName = "updateActiveState",
        warmUp = true,
        strictMatch = true
    )
    public static class PreserveNightSpeed {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter(@Patch.This IsoZombie zombie) {
            return NightTorporRuntime.updateActivityWithoutChangingSpeed(zombie);
        }
    }

    @Patch(
        className = "zombie.WorldSoundManager",
        methodName = "getHearingMultiplier",
        warmUp = true,
        strictMatch = true
    )
    public static class WeakenNightHearing {
        @Patch.OnExit
        public static void exit(
            @Patch.Argument(0) IsoZombie zombie,
            @Patch.Return(readOnly = false) float multiplier
        ) {
            multiplier = NightTorporRuntime.adjustHearing(zombie, multiplier);
        }
    }
}
