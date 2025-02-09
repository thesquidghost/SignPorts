package net.farhaven.SignPorts;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ExclusionStrategyImpl implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        // Check if the declaring class has a package; if not, don't skip.
        if (f.getDeclaringClass().getPackage() == null) {
            return false;
        }
        // Skip fields from classes not in the 'net.farhaven' package.
        return !f.getDeclaringClass().getPackage().getName().startsWith("net.farhaven");
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}