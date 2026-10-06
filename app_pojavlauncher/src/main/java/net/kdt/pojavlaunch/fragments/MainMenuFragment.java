package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mDiscordButton = view.findViewById(R.id.discord_button);
        Button mWebsiteButton = view.findViewById(R.id.website_button);
        Button mPerformanceSetupButton = view.findViewById(R.id.performance_setup_button);
        TextView mProfileDetails = view.findViewById(R.id.selected_profile_details);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mDiscordButton.setOnClickListener(v -> Tools.openURL(requireActivity(), getString(R.string.flint_discord_url)));
        mWebsiteButton.setOnClickListener(v -> Tools.openURL(requireActivity(), getString(R.string.flint_website_url)));
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));
        mPerformanceSetupButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));
        mVersionSpinner.setOnProfileSelectedListener(profileKey -> updateProfileDetails(mProfileDetails, profileKey));

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

    }

    private void updateProfileDetails(TextView detailsView, String profileKey) {
        LauncherProfiles.load();
        MinecraftProfile profile = LauncherProfiles.mainProfileJson.profiles.get(profileKey);
        if (profile == null || profile.lastVersionId == null) {
            detailsView.setText(R.string.home_profile_unavailable);
            return;
        }

        String versionId = profile.lastVersionId;
        String normalized = versionId.toLowerCase();
        String loader = getString(R.string.home_loader_vanilla);
        if (normalized.contains("fabric")) loader = getString(R.string.home_loader_fabric);
        else if (normalized.contains("forge")) loader = getString(R.string.home_loader_forge);
        else if (normalized.contains("quilt")) loader = getString(R.string.home_loader_quilt);
        else if (normalized.contains("optifine")) loader = getString(R.string.home_loader_optifine);
        detailsView.setText(getString(R.string.home_profile_details, versionId, loader));
    }

    @Override
    public void onResume() {
        super.onResume();
        mVersionSpinner.reloadProfiles();
    }
}
