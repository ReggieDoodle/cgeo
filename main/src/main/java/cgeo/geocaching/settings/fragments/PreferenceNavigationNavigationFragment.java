package cgeo.geocaching.settings.fragments;

import cgeo.geocaching.R;
import cgeo.geocaching.apps.navi.NavigationAppFactory;
import cgeo.geocaching.utils.PreferenceUtils;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class PreferenceNavigationNavigationFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.preferences_navigation_navigation, rootKey);

        initNavigationMenuPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.settings_title_navigation_menu);
    }

    private void initNavigationMenuPreferences() {
        for (final NavigationAppFactory.NavigationAppsEnum appEnum : NavigationAppFactory.NavigationAppsEnum.values()) {
            final Preference preference = findPreference(getString(appEnum.preferenceKey));
            if (appEnum.app.isInstalled()) {
                PreferenceUtils.setEnabled(preference, true);
            } else {
                PreferenceUtils.setSummary(preference, getString(R.string.settings_navigation_disabled));
            }
        }
    }
}
