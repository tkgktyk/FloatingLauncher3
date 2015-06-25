package jp.tkgktyk.floatinglauncher3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import jp.tkgktyk.lib.BaseSettingsDialogActivity;

/**
 * Created by tkgktyk on 2015/06/23.
 */
public class SettingsDialogActivity extends BaseSettingsDialogActivity {
    public static final String LOCAL_SIZE_CHANGED = "SIZE_CHANGED";

    @Override
    protected BaseFragment newRootFragment() {
        return new SettingsFragment();
    }

    public static class SettingsFragment extends BaseFragment {

        private SharedPreferences.OnSharedPreferenceChangeListener mListener
                = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getString(R.string.key_size))) {
                    LocalBroadcastManager.getInstance(getActivity())
                            .sendBroadcast(new Intent(LOCAL_SIZE_CHANGED));
                }
            }
        };

        public SettingsFragment() {
        }

        @Override
        protected String getTitle() {
            return getString(R.string.app_name);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(mListener);
            addPreferencesFromResource(R.xml.pref_settings);

            showTextSummary(R.string.key_size, R.string.unit_size);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(mListener);
        }
    }
}
