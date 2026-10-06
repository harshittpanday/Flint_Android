package net.kdt.pojavlaunch.profiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Pure profile-selection rules shared by the persistent store and unit tests. */
public final class ProfileSelection {
    private ProfileSelection() {}

    public static String resolve(String requestedKey, Collection<String> availableKeys) {
        if (requestedKey != null && availableKeys.contains(requestedKey)) return requestedKey;
        if (availableKeys.isEmpty()) return null;
        List<String> sortedKeys = new ArrayList<>(availableKeys);
        Collections.sort(sortedKeys);
        return sortedKeys.get(0);
    }
}
