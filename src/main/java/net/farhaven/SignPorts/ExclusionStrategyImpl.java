package net.farhaven.SignPorts;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ExclusionStrategyImpl implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        // Skip fields that are not accessible
        return !f.getDeclaringClass().getPackage().getName().startsWith("net.farhaven");
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}