package net.kdt.pojavlaunch.value.launcherprofiles;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.profiles.ProfileSelection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class LauncherProfiles {
    public static MinecraftLauncherProfiles mainProfileJson;

    private static File getLauncherProfilesFile() {
        return new File(Tools.GAME_PROFILES_FILE);
    }

    /** Reload the profile from the file, creating a default one if necessary */
    public static synchronized void load(){
        File launcherProfilesFile = getLauncherProfilesFile();
        MinecraftLauncherProfiles loadedProfiles = null;
        if (launcherProfilesFile.exists()) {
            try {
                loadedProfiles = Tools.GLOBAL_GSON.fromJson(
                        Tools.read(launcherProfilesFile.getAbsolutePath()), MinecraftLauncherProfiles.class);
            } catch (IOException e) {
                Log.e(LauncherProfiles.class.toString(), "Failed to load file: ", e);
                throw new RuntimeException(e);
            }
        }

        // Fill with default
        mainProfileJson = loadedProfiles == null ? new MinecraftLauncherProfiles() : loadedProfiles;
        if (mainProfileJson.profiles == null) mainProfileJson.profiles = new HashMap<>();
        boolean shouldWrite = false;
        if (mainProfileJson.profiles.size() == 0) {
            mainProfileJson.profiles.put(UUID.randomUUID().toString(), MinecraftProfile.getDefaultProfile());
            shouldWrite = true;
        }

        // Normalize profile names from mod installers
        if(normalizeProfileIds(mainProfileJson)) {
            shouldWrite = true;
        }

        if (shouldWrite) write();
        repairSelectedProfile();
    }

    /** Apply the current configuration into a file */
    public static synchronized void write() {
        try {
            Tools.write(getLauncherProfilesFile().getAbsolutePath(), mainProfileJson.toJson());
        } catch (IOException e) {
            Log.e(LauncherProfiles.class.toString(), "Failed to write profile file", e);
            throw new RuntimeException(e);
        }
    }

    public static @NonNull MinecraftProfile getCurrentProfile() {
        LauncherProfiles.load();
        String profileKey = repairSelectedProfile();
        MinecraftProfile profile = mainProfileJson.profiles.get(profileKey);
        if(profile == null) throw new IllegalStateException("No launcher profile is available");
        return profile;
    }

    public static @Nullable MinecraftProfile getProfile(String profileKey) {
        LauncherProfiles.load();
        if (profileKey == null) return null;
        return mainProfileJson.profiles.get(profileKey);
    }

    public static @NonNull String getCurrentProfileKey() {
        LauncherProfiles.load();
        String profileKey = repairSelectedProfile();
        if (profileKey == null) throw new IllegalStateException("No launcher profile is available");
        return profileKey;
    }

    public static boolean selectProfile(String profileKey) {
        if (mainProfileJson == null) load();
        if (!mainProfileJson.profiles.containsKey(profileKey)) return false;
        LauncherPreferences.DEFAULT_PREF.edit()
                .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, profileKey)
                .commit();
        return true;
    }

    public static String deleteProfile(String profileKey) {
        load();
        if (mainProfileJson.profiles.size() <= 1 || !mainProfileJson.profiles.containsKey(profileKey)) {
            return getCurrentProfileKey();
        }
        mainProfileJson.profiles.remove(profileKey);
        write();
        return repairSelectedProfile();
    }

    public static List<String> getSortedProfileKeys() {
        load();
        List<String> keys = new ArrayList<>(mainProfileJson.profiles.keySet());
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String firstKey, String secondKey) {
                MinecraftProfile firstProfile = mainProfileJson.profiles.get(firstKey);
                MinecraftProfile secondProfile = mainProfileJson.profiles.get(secondKey);
                String firstName = firstProfile == null || firstProfile.name == null
                        ? "" : firstProfile.name.toLowerCase(Locale.ROOT);
                String secondName = secondProfile == null || secondProfile.name == null
                        ? "" : secondProfile.name.toLowerCase(Locale.ROOT);
                int nameComparison = firstName.compareTo(secondName);
                return nameComparison != 0 ? nameComparison : firstKey.compareTo(secondKey);
            }
        });
        return keys;
    }

    private static String repairSelectedProfile() {
        if (LauncherPreferences.DEFAULT_PREF == null || mainProfileJson == null) return null;
        String requestedKey = LauncherPreferences.DEFAULT_PREF.getString(
                LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "");
        String resolvedKey = ProfileSelection.resolve(requestedKey, mainProfileJson.profiles.keySet());
        if (resolvedKey != null && !resolvedKey.equals(requestedKey)) {
            LauncherPreferences.DEFAULT_PREF.edit()
                    .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, resolvedKey)
                    .commit();
        }
        return resolvedKey;
    }

    /**
     * Insert a new profile into the profile map
     * @param minecraftProfile the profile to insert
     */
    public static void insertMinecraftProfile(MinecraftProfile minecraftProfile) {
        mainProfileJson.profiles.put(getFreeProfileKey(), minecraftProfile);
    }

    /**
     * Pick an unused normalized key to store a new profile with
     * @return an unused key
     */
    public static String getFreeProfileKey() {
        Map<String, MinecraftProfile> profileMap = mainProfileJson.profiles;
        String freeKey = UUID.randomUUID().toString();
        while(profileMap.get(freeKey) != null) freeKey = UUID.randomUUID().toString();
        return freeKey;
    }

    /**
     * For all keys to be UUIDs, effectively isolating profile created by installers
     * This avoids certain profiles to be erased by the installer
     * @return Whether some profiles have been normalized
     */
    private static boolean normalizeProfileIds(MinecraftLauncherProfiles launcherProfiles){
        boolean hasNormalized = false;
        ArrayList<String> keys = new ArrayList<>();

        // Detect denormalized keys
        for(String profileKey : launcherProfiles.profiles.keySet()){
            try{
                if(!UUID.fromString(profileKey).toString().equals(profileKey)) keys.add(profileKey);
            }catch (IllegalArgumentException exception){
                keys.add(profileKey);
                Log.w(LauncherProfiles.class.toString(), "Illegal profile uuid: " + profileKey);
            }
        }

        // Swap the new keys
        for(String profileKey : keys){
            MinecraftProfile currentProfile = launcherProfiles.profiles.get(profileKey);
            insertMinecraftProfile(currentProfile);
            launcherProfiles.profiles.remove(profileKey);
            hasNormalized = true;
        }

        return hasNormalized;
    }
}
