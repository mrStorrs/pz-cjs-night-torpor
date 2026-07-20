package com.cjstorrs.cjsnighttorpor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.PatchEngine;
import zombie.WorldSoundManager;
import zombie.characters.IsoZombie;

public final class NightTorporPatchTest {
    private NightTorporPatchTest() {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        testPolicy();
        testPatchMetadata();
        testZombieBuddyDiscovery();
        testRuntimeFacade();
        testGameApiLinkage();
        System.out.println("NightTorporPatchTest: PASS");
    }

    private static void testPolicy() {
        check(NightTorporPolicy.shouldReplaceTransition(true, false, 3), "night transition should be replaced");
        check(NightTorporPolicy.shouldReplaceTransition(false, true, 3), "day transition should be replaced");
        check(!NightTorporPolicy.shouldReplaceTransition(true, true, 3), "steady night state should pass through");
        check(!NightTorporPolicy.shouldReplaceTransition(true, false, 2), "night-active mode must retain vanilla behavior");
        check(NightTorporPolicy.isNightTorporActive(true, 3), "inactive day-active zombies should have torpor");
        check(!NightTorporPolicy.isNightTorporActive(false, 3), "active zombies should not have torpor");
        check(!NightTorporPolicy.isNightTorporActive(true, 2), "daytime inactivity must not count as night torpor");
        checkClose(0.5F, NightTorporPolicy.adjustHearing(2.0F, true), "hearing multiplier");
        checkClose(2.0F, NightTorporPolicy.adjustHearing(2.0F, false), "day hearing");
        checkClose(0.25F, NightTorporPolicy.DETECTION_MULTIPLIER, "vanilla inactive detection contract");
        checkEqual(25, NightTorporPolicy.MEMORY_CAP, "vanilla inactive memory contract");
    }

    private static void testPatchMetadata() throws ReflectiveOperationException {
        for (Class<?> patchClass : patchClasses()) {
            Patch patch = patchClass.getAnnotation(Patch.class);
            check(patch != null, patchClass.getName() + " must carry @Patch");
            check(patch.strictMatch(), patchClass.getName() + " must use strict overload matching");
            check(patch.warmUp(), patchClass.getName() + " must warm up");
        }

        assertTarget(NightTorporPatches.PreserveNightSpeed.class, "zombie.characters.IsoZombie", "makeInactive");
        assertTarget(NightTorporPatches.WeakenNightHearing.class, "zombie.WorldSoundManager", "getHearingMultiplier");
        Method speedEnter = NightTorporPatches.PreserveNightSpeed.class.getDeclaredMethod(
            "enter",
            IsoZombie.class,
            boolean.class
        );
        Patch.OnEnter onEnter = speedEnter.getAnnotation(Patch.OnEnter.class);
        check(onEnter != null && onEnter.skipOn(), "speed advice must skip vanilla on handled transitions");
        check(speedEnter.getReturnType() == boolean.class, "speed advice skip result must be boolean");
        check(speedEnter.getParameters()[0].isAnnotationPresent(Patch.This.class), "speed advice must bind the zombie");
        assertArgument(speedEnter.getParameters()[1], 0, "inactive argument");
        Method hearingExit = NightTorporPatches.WeakenNightHearing.class.getDeclaredMethod(
            "exit",
            IsoZombie.class,
            float.class
        );
        assertArgument(hearingExit.getParameters()[0], 0, "hearing zombie");
        assertMutableReturn(hearingExit.getParameters()[1], "hearing result");
    }

    private static void testZombieBuddyDiscovery() {
        List<Class<?>> discovered = PatchEngine.collectPatches(
            "com.cjstorrs.cjsnighttorpor",
            NightTorporPatchTest.class.getClassLoader()
        );
        check(discovered.size() == patchClasses().size(), "ZombieBuddy must discover exactly two patches");
        for (Class<?> patchClass : patchClasses()) {
            check(discovered.contains(patchClass), "ZombieBuddy missed " + patchClass.getName());
        }
    }

    private static void testRuntimeFacade() {
        check(Modifier.isPublic(NightTorporRuntime.class.getModifiers()), "runtime facade must be public");
        for (Method method : NightTorporRuntime.class.getDeclaredMethods()) {
            check(Modifier.isPublic(method.getModifiers()), method.getName() + " must be public for inlined advice");
            check(Modifier.isStatic(method.getModifiers()), method.getName() + " must be static");
        }
    }

    private static void testGameApiLinkage() throws ReflectiveOperationException {
        check(IsoZombie.class.getDeclaredMethod("makeInactive", boolean.class).getReturnType() == void.class,
            "IsoZombie inactivity signature changed");
        check(WorldSoundManager.class.getDeclaredMethod("getHearingMultiplier", IsoZombie.class).getReturnType() == float.class,
            "WorldSoundManager hearing signature changed");
        Method memory = IsoZombie.class.getDeclaredMethod("getSandboxMemoryDuration");
        check(memory.getReturnType() == int.class, "IsoZombie memory signature changed");
        check(Modifier.isProtected(memory.getModifiers()), "IsoZombie memory method visibility changed");
    }

    private static List<Class<?>> patchClasses() {
        return List.of(
            NightTorporPatches.PreserveNightSpeed.class,
            NightTorporPatches.WeakenNightHearing.class
        );
    }

    private static void assertTarget(Class<?> patchClass, String className, String methodName) {
        Patch patch = patchClass.getAnnotation(Patch.class);
        check(className.equals(patch.className()), patchClass.getName() + " target class changed");
        check(methodName.equals(patch.methodName()), patchClass.getName() + " target method changed");
    }

    private static void assertArgument(Parameter parameter, int index, String label) {
        Patch.Argument argument = parameter.getAnnotation(Patch.Argument.class);
        check(argument != null, label + " must carry @Patch.Argument");
        check(argument.value() == index, label + " index changed");
        check(argument.readOnly(), label + " must be read-only");
    }

    private static void assertMutableReturn(Parameter parameter, String label) {
        Patch.Return returnValue = parameter.getAnnotation(Patch.Return.class);
        check(returnValue != null, label + " must carry @Patch.Return");
        check(!returnValue.readOnly(), label + " must remain mutable");
    }

    private static void checkClose(float expected, float actual, String label) {
        check(Math.abs(expected - actual) < 0.0001F, label + " changed: expected " + expected + ", got " + actual);
    }

    private static void checkEqual(int expected, int actual, String label) {
        check(expected == actual, label + " changed: expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
