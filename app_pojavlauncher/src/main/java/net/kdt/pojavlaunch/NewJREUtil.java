package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Architecture.archAsString;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.utils.MathUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NewJREUtil {
    private static boolean checkInternalRuntime(AssetManager assetManager, InternalRuntime internalRuntime)
            throws RuntimeInstallationException {
        String launcher_runtime_version;
        String installed_runtime_version = MultiRTUtils.readInternalRuntimeVersion(internalRuntime.name);
        boolean installedRuntimeValid = MultiRTUtils.isRuntimeValid(internalRuntime.name,
                internalRuntime.majorVersion, Tools.DEVICE_ARCHITECTURE);
        try {
            launcher_runtime_version = Tools.read(assetManager.open(internalRuntime.path+"/version"));
        }catch (IOException exc) {
            if (installedRuntimeValid) return true;
            throw new RuntimeInstallationException("Java " + internalRuntime.majorVersion
                    + " is not bundled in this APK (missing " + internalRuntime.path
                    + "/version) and no valid installed copy is available.", exc);
        }
        if(!launcher_runtime_version.equals(installed_runtime_version) || !installedRuntimeValid)
            return unpackInternalRuntime(assetManager, internalRuntime, launcher_runtime_version);
        return true;
    }

    private static boolean unpackInternalRuntime(AssetManager assetManager, InternalRuntime internalRuntime, String version)
            throws RuntimeInstallationException {
        String architecture = archAsString(Tools.DEVICE_ARCHITECTURE);
        if (Tools.DEVICE_ARCHITECTURE == Architecture.UNSUPPORTED_ARCH) {
            throw new RuntimeInstallationException("Java " + internalRuntime.majorVersion
                    + " cannot be installed on unsupported CPU architecture: " + architecture);
        }
        try {
            MultiRTUtils.installRuntimeNamedBinpackAtomically(
                    assetManager.open(internalRuntime.path+"/universal.tar.xz"),
                    assetManager.open(internalRuntime.path+"/bin-" + architecture + ".tar.xz"),
                    internalRuntime.name, version, internalRuntime.majorVersion,
                    Tools.DEVICE_ARCHITECTURE);
            return true;
        }catch (IOException e) {
            String runtimeRoot = new java.io.File(Tools.MULTIRT_HOME).getAbsolutePath();
            RuntimeInstallationException failure = new RuntimeInstallationException(
                    "Failed to install Java " + internalRuntime.majorVersion + " for " + architecture
                            + " in " + runtimeRoot + ": " + e.getMessage(), e);
            Log.e("NewJREAuto", failure.getMessage(), failure);
            throw failure;
        }
    }

    private static InternalRuntime getInternalRuntime(Runtime runtime) {
        for(InternalRuntime internalRuntime : InternalRuntime.values()) {
            if(internalRuntime.name.equals(runtime.name)) return internalRuntime;
        }
        return null;
    }

    private static MathUtils.RankedValue<Runtime> getNearestInstalledRuntime(int targetVersion) {
        List<Runtime> runtimes = new java.util.ArrayList<>();
        for (Runtime runtime : MultiRTUtils.getRuntimes()) {
            if (MultiRTUtils.isRuntimeUsable(runtime.name, targetVersion, Tools.DEVICE_ARCHITECTURE)) {
                runtimes.add(runtime);
            }
        }
        return MathUtils.findNearestPositive(targetVersion, runtimes, (runtime)->runtime.javaVersion);
    }

    private static MathUtils.RankedValue<InternalRuntime> getNearestInternalRuntime(int targetVersion) {
        List<InternalRuntime> runtimeList = Arrays.asList(InternalRuntime.values());
        return MathUtils.findNearestPositive(targetVersion, runtimeList, (runtime)->runtime.majorVersion);
    }


    /** @return true if everything is good, false otherwise.  */
    public static boolean installNewJreIfNeeded(Activity activity, JMinecraftVersionList.Version versionInfo,
                                                String profileKey) throws RuntimeInstallationException {
        //Now we have the reliable information to check if our runtime settings are good enough
        int gameRequiredVersion = versionInfo.javaVersion == null
                || "jre-legacy".equalsIgnoreCase(versionInfo.javaVersion.component)
                ? 8 : versionInfo.javaVersion.majorVersion;

        LauncherProfiles.load();
        AssetManager assetManager = activity.getAssets();
        MinecraftProfile minecraftProfile = LauncherProfiles.getProfile(profileKey);
        if (minecraftProfile == null) {
            Tools.dialogOnUiThread(activity, activity.getString(R.string.global_error),
                    activity.getString(R.string.flint_launch_profile_missing));
            return false;
        }
        String profileRuntime = Tools.getSelectedRuntime(minecraftProfile);
        Runtime runtime = MultiRTUtils.read(profileRuntime);
        // Partly trust the user with his own selection, if the game can even try to run in this case
        if (runtime.javaVersion >= gameRequiredVersion) {
            // Check whether the selection is an internal runtime
            InternalRuntime internalRuntime = getInternalRuntime(runtime);
            // If it is, check if updates are available from the APK file
            if(internalRuntime != null) {
                // Not calling showRuntimeFail on failure here because we did, technically, find the compatible runtime
                return checkInternalRuntime(assetManager, internalRuntime);
            }
            if (MultiRTUtils.isRuntimeUsable(runtime.name, gameRequiredVersion,
                    Tools.DEVICE_ARCHITECTURE)) return true;
        }

        // If the runtime version selected by the user is not appropriate for this version (which means the game won't run at all)
        // automatically pick from either an already installed runtime, or a runtime packed with the launcher
        MathUtils.RankedValue<?> nearestInstalledRuntime = getNearestInstalledRuntime(gameRequiredVersion);
        MathUtils.RankedValue<?> nearestInternalRuntime = getNearestInternalRuntime(gameRequiredVersion);

        MathUtils.RankedValue<?> selectedRankedRuntime = MathUtils.objectMin(
                nearestInternalRuntime, nearestInstalledRuntime, (value)->value.rank
        );

        // No possible selections
        if(selectedRankedRuntime == null) {
            showRuntimeFail(activity, versionInfo);
            return false;
        }

        Object selected = selectedRankedRuntime.value;
        String appropriateRuntime;
        InternalRuntime internalRuntime;

        // Perform checks on the picked runtime
        if(selected instanceof Runtime) {
            // If it's an already installed runtime, save its name and check if
            // it's actually an internal one (just in case)
            Runtime selectedRuntime = (Runtime) selected;
            appropriateRuntime = selectedRuntime.name;
            internalRuntime = getInternalRuntime(selectedRuntime);
        } else if (selected instanceof InternalRuntime) {
            // If it's an internal runtime, set it's name as the appropriate one.
            internalRuntime = (InternalRuntime) selected;
            appropriateRuntime = internalRuntime.name;
        } else {
            throw new RuntimeException("Unexpected type of selected: "+selected.getClass().getName());
        }

        // If it turns out the selected runtime is actually an internal one, attempt automatic installation or update
        if(internalRuntime != null && !checkInternalRuntime(assetManager, internalRuntime)) {
            // Not calling showRuntimeFail here because we did, technically, find the compatible runtime
            return false;
        }

        minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + appropriateRuntime;
        LauncherProfiles.write();
        return true;
    }

    private static void showRuntimeFail(Activity activity, JMinecraftVersionList.Version verInfo) {
        Tools.dialogOnUiThread(activity, activity.getString(R.string.global_error),
                activity.getString(R.string.multirt_nocompatiblert, verInfo.javaVersion.majorVersion));
    }

    private enum InternalRuntime {
        JRE_8(8, "Internal", "components/jre"),
        JRE_17(17, "Internal-17", "components/jre-new"),
        JRE_21(21, "Internal-21", "components/jre-21");
        public final int majorVersion;
        public final String name;
        public final String path;
        InternalRuntime(int majorVersion, String name, String path) {
            this.majorVersion = majorVersion;
            this.name = name;
            this.path = path;
        }
    }

}
