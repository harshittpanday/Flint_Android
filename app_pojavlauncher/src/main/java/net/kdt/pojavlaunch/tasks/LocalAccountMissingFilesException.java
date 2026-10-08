package net.kdt.pojavlaunch.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import net.kdt.pojavlaunch.LauncherActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.ShowErrorActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment;
import net.kdt.pojavlaunch.lifecycle.ContextExecutorTask;

import java.io.IOException;
import java.io.Serializable;

/** Expected launch stop when a local account needs a file it is not allowed to download. */
public final class LocalAccountMissingFilesException extends IOException
        implements ContextExecutorTask, Serializable {
    private static final long serialVersionUID = 1L;

    public enum Requirement {
        VERSION_METADATA,
        ASSET_INDEX,
        CLIENT,
        LIBRARY,
        ASSET,
        LOGGING_CONFIG
    }

    private final Requirement mRequirement;
    private final MinecraftInstallationPreflight.Failure mFailure;
    private final String mFilePath;

    public LocalAccountMissingFilesException(Requirement requirement,
                                             MinecraftInstallationPreflight.Failure failure,
                                             String filePath) {
        super(requirement + " " + failure + ": " + filePath);
        mRequirement = requirement;
        mFailure = failure;
        mFilePath = filePath;
    }

    private int requirementLabel() {
        switch (mRequirement) {
            case VERSION_METADATA: return R.string.flint_preflight_version_metadata;
            case ASSET_INDEX: return R.string.flint_preflight_asset_index;
            case CLIENT: return R.string.flint_preflight_client;
            case LIBRARY: return R.string.flint_preflight_library;
            case ASSET: return R.string.flint_preflight_asset;
            case LOGGING_CONFIG: return R.string.flint_preflight_logging;
            default: throw new IllegalStateException("Unknown requirement " + mRequirement);
        }
    }

    private int failureLabel() {
        switch (mFailure) {
            case MISSING: return R.string.flint_preflight_missing;
            case NOT_A_FILE: return R.string.flint_preflight_not_file;
            case UNREADABLE: return R.string.flint_preflight_unreadable;
            case HASH_MISMATCH: return R.string.flint_preflight_hash_mismatch;
            default: throw new IllegalStateException("Unknown failure " + mFailure);
        }
    }

    @Override
    public void executeWithActivity(Activity activity) {
        String detail = activity.getString(R.string.flint_preflight_detail,
                activity.getString(requirementLabel()), activity.getString(failureLabel()), mFilePath);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.flint_preflight_title)
                .setMessage(detail + "\n\n" + activity.getString(R.string.flint_local_missing_files))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.flint_sign_in_microsoft, (dialog, which) -> {
                    if (activity instanceof LauncherActivity) {
                        Tools.swapFragment((LauncherActivity) activity, MicrosoftLoginFragment.class,
                                MicrosoftLoginFragment.TAG, null);
                    } else {
                        Intent intent = new Intent(activity, LauncherActivity.class);
                        intent.putExtra(LauncherActivity.EXTRA_OPEN_MICROSOFT_LOGIN, true);
                        activity.startActivity(intent);
                    }
                });
        ShowErrorActivity.installRemoteDialogHandling(activity, builder);
        builder.show();
    }

    @Override
    public void executeWithApplication(Context context) {
        Intent intent = new Intent(context, LauncherActivity.class);
        intent.putExtra(LauncherActivity.EXTRA_OPEN_MICROSOFT_LOGIN, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
