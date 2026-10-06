package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.openPath;
import static net.kdt.pojavlaunch.Tools.shareLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.CustomControlsActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;

public class FlintToolsFragment extends Fragment {
    public static final String TAG = "FlintToolsFragment";

    public FlintToolsFragment() {
        super(R.layout.fragment_flint_tools);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.tools_custom_controls).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CustomControlsActivity.class)));
        view.findViewById(R.id.tools_install_jar).setOnClickListener(v -> runInstaller(false));
        view.findViewById(R.id.tools_install_jar).setOnLongClickListener(v -> {
            runInstaller(true);
            return true;
        });
        view.findViewById(R.id.tools_share_logs).setOnClickListener(v -> shareLog(requireContext()));
        view.findViewById(R.id.tools_open_files).setOnClickListener(v -> {
            Tools.switchDemo(Tools.isDemoProfile(requireContext()));
            if (Tools.isDemoProfile(requireContext())) {
                Toast.makeText(requireContext(), R.string.toast_not_available_demo, Toast.LENGTH_LONG).show();
                return;
            }
            openPath(requireContext(), Tools.getGameDirPath(LauncherProfiles.getCurrentProfile()), false);
        });
    }

    private void runInstaller(boolean customArguments) {
        if (Tools.isLocalProfile(requireContext()) || Tools.isDemoProfile(requireContext())) {
            Toast.makeText(requireContext(), R.string.toast_not_available_demo, Toast.LENGTH_LONG).show();
            return;
        }
        if (ProgressKeeper.getTaskCount() == 0) {
            Tools.installMod(requireActivity(), customArguments);
        } else {
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
        }
    }
}
