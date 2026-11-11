package com.craisinlord.integrated_api.utils;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public final class PlatformHooks {

    private static Predicate<String> modLoaded = modid -> false;
    private static BooleanSupplier devEnvironment = () -> false;

    private PlatformHooks() {
    }

    public static void init(Predicate<String> modLoaded, BooleanSupplier devEnvironment) {
        PlatformHooks.modLoaded = Objects.requireNonNull(modLoaded);
        PlatformHooks.devEnvironment = Objects.requireNonNull(devEnvironment);
    }

    public static boolean isModLoaded(String modid) {
        return modLoaded.test(modid);
    }

    public static boolean isDevEnvironment() {
        return devEnvironment.getAsBoolean();
    }
}
