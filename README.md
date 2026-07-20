# CJS Night Torpor

A Project Zomboid Build 42.19 mod that replaces the vanilla **Active During Day** night penalty with sensory torpor while preserving every zombie's assigned speed.

At night:

- Zombie hearing radius is multiplied by `0.25`.
- Visual spotting chance is multiplied by `0.25`.
- Target memory is capped at `25` engine units.
- Zombie speed is not reassigned or forced to shambling speed.

The mod only takes over when `ZombieLore.ActiveOnly` is **Active During Day** and vanilla enters its inactive nighttime phase. Daytime behavior and the other activity modes pass through unchanged.

This is compatible by design with mods that assign individual zombie speeds, including Progressive Random Zombies: Night Torpor bypasses only vanilla's private activity update and does not intercept PRZ's own `makeInactive(true/false)` speed-assignment calls. It never chooses a speed itself. Its visual multiplier composes with CJS Stealth Overhaul instead of replacing that mod's result.

## Performance

Night Torpor keeps vanilla's inactive flag, which already supplies the 0.25 sight penalty and 25-unit memory cap, but skips the call that mutates speed state. Hearing adds one guarded multiplication to vanilla's existing sound calculation. The mod has no `OnTick` Lua callback, zombie-list scan, per-zombie `modData`, object allocation, or hot-path logging. The expected overhead is effectively immeasurable compared with the vanilla AI calculations it modifies.

## Requirements

- Project Zomboid 42.19 or newer
- ZombieBuddy 2.3.2 or newer

## Build

Run `scripts/build.sh`. B42.19 needs a Java 25-aware compiler; the script checks `.tools/ecj.jar`, then the existing sibling `cjsStealthOverhaul/.tools/ecj.jar`. Override `ECJ_JAR`, `PZ_JAR`, `PZ_JAVA`, or `ZOMBIE_BUDDY_JAR` when those paths differ.
