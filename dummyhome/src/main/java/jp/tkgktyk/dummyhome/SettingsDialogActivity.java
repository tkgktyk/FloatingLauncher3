package jp.tkgktyk.dummyhome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;

import com.google.common.base.Strings;

import java.net.URISyntaxException;

import jp.tkgktyk.lib.BaseSettingsDialogActivity;

/**
 * Created by tkgktyk on 2015/06/23.
 */
public class SettingsDialogActivity extends BaseSettingsDialogActivity {
    @Override
    protected BaseFragment newRootFragment() {
        return new SettingsFragment();
    }

    public static String getUri(Intent intent) {
        return intent.toUri(0);
    }

    public static Intent getIntent(String uri) {
        try {
            return Intent.parseUri(uri, 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAppName(Context context, String uri) {
        return getAppName(context, getIntent(uri));
    }

    public static String getAppName(Context context, Intent intent) {
        String name;
        String packageName = getPackageName(intent);
        if (Strings.isNullOrEmpty(packageName)) {
            name = context.getString(R.string.not_found);
        } else {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
            }
            if (ai != null) {
                name = pm.getApplicationLabel(ai).toString();
            } else {
                name = context.getString(R.string.not_found);
            }
        }
        return name;
    }

    public static String getPackageName(Intent intent) {
        if (intent != null && intent.getComponent() != null) {
            return intent.getComponent().getPackageName();
        }
        return "";
    }

    public static class SettingsFragment extends BaseFragment {
        private static final int REQUEST_PICK_APP = 1;

        private String mPrefKey;

        public SettingsFragment() {
        }

        @Override
        protected String getTitle() {
            return getString(R.string.app_name);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);

            final Preference pref = findPreference(R.string.key_launcher);
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                    intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER));
                    intent.putExtra(Intent.EXTRA_TITLE, preference.getTitle());

                    mPrefKey = preference.getKey();
                    startActivityForResult(intent, REQUEST_PICK_APP);
                    return true;
                }
            });
            updateSummary(pref);
        }

        private void updateSummary(String key, String uri) {
            Preference pref = findPreference(key);
            pref.setSummary(getAppName(pref.getContext(), uri));
        }

        private void updateSummary(Preference pref) {
            String uri = pref.getSharedPreferences().getString(pref.getKey(), "");
            pref.setSummary(getAppName(pref.getContext(), uri));
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQUEST_PICK_APP:
                    if (RESULT_OK == resultCode) {
                        String uri = getUri(data);
                        getPreferenceManager().getSharedPreferences().edit()
                                .putString(mPrefKey, uri)
                                .apply();
                        updateSummary(mPrefKey, uri);
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
