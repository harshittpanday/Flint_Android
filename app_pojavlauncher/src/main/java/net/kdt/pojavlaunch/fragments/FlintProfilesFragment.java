package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.profiles.ProfileIconCache;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.util.ArrayList;
import java.util.List;

public class FlintProfilesFragment extends Fragment {
    public static final String TAG = "FlintProfilesFragment";
    private final List<String> profileKeys = new ArrayList<>();
    private ProfilesAdapter adapter;
    private String currentProfileKey;

    public FlintProfilesFragment() {
        super(R.layout.fragment_flint_profiles);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView list = view.findViewById(R.id.flint_profiles_list);
        adapter = new ProfilesAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) -> select(profileKeys.get(position)));
        view.findViewById(R.id.flint_profile_add).setOnClickListener(v -> Tools.swapFragment(
                requireActivity(), ProfileTypeSelectFragment.class, ProfileTypeSelectFragment.TAG, null));
        reload();
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        LauncherProfiles.load();
        profileKeys.clear();
        profileKeys.addAll(LauncherProfiles.getSortedProfileKeys());
        currentProfileKey = LauncherProfiles.getCurrentProfileKey();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void select(String profileKey) {
        if (LauncherProfiles.selectProfile(profileKey)) {
            ExtraCore.setValue(ExtraConstants.REFRESH_VERSION_SPINNER, profileKey);
            currentProfileKey = profileKey;
            adapter.notifyDataSetChanged();
        }
    }

    private void edit(String profileKey) {
        select(profileKey);
        Tools.swapFragment(requireActivity(), ProfileEditorFragment.class, ProfileEditorFragment.TAG, null);
    }

    private void confirmDelete(String profileKey) {
        if (profileKeys.size() <= 1) {
            Toast.makeText(requireContext(), R.string.flint_profile_keep_one, Toast.LENGTH_SHORT).show();
            return;
        }
        MinecraftProfile profile = LauncherProfiles.mainProfileJson.profiles.get(profileKey);
        String name = profile == null ? profileKey : profile.name;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.flint_profile_delete_title)
                .setMessage(getString(R.string.flint_profile_delete_message, name))
                .setPositiveButton(R.string.global_delete, (dialog, which) -> {
                    ProfileIconCache.dropIcon(profileKey);
                    String selected = LauncherProfiles.deleteProfile(profileKey);
                    ExtraCore.setValue(ExtraConstants.REFRESH_VERSION_SPINNER, selected);
                    reload();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private final class ProfilesAdapter extends BaseAdapter {
        @Override public int getCount() { return profileKeys.size(); }
        @Override public String getItem(int position) { return profileKeys.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flint_profile, parent, false);
            }
            String key = getItem(position);
            MinecraftProfile profile = LauncherProfiles.mainProfileJson.profiles.get(key);
            ImageView icon = convertView.findViewById(R.id.flint_profile_icon);
            TextView name = convertView.findViewById(R.id.flint_profile_name);
            TextView details = convertView.findViewById(R.id.flint_profile_details);
            TextView selected = convertView.findViewById(R.id.flint_profile_selected);
            ImageButton edit = convertView.findViewById(R.id.flint_profile_edit);
            ImageButton delete = convertView.findViewById(R.id.flint_profile_delete);

            icon.setImageDrawable(ProfileIconCache.fetchIcon(getResources(), key, profile.icon));
            name.setText(profile.name);
            details.setText(profile.lastVersionId == null ? getString(R.string.home_profile_unavailable) : profile.lastVersionId);
            selected.setVisibility(key.equals(currentProfileKey) ? View.VISIBLE : View.GONE);
            convertView.setSelected(selected.getVisibility() == View.VISIBLE);
            edit.setOnClickListener(v -> edit(key));
            delete.setOnClickListener(v -> confirmDelete(key));
            return convertView;
        }
    }
}
