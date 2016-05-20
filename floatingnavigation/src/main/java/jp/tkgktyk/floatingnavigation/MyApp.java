package jp.tkgktyk.floatingnavigation;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import jp.tkgktyk.lib.BaseApplication;

/**
 * Created by tkgktyk on 2015/07/02.
 */
public class MyApp extends BaseApplication {
    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    protected void onVersionUpdated(MyVersion next, MyVersion old) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Utilities.setIconSize();
    }

    private static final String KEY_RECORDS = "key_records";
    private static final String KEY_ENABLED = "key_enabled";

    public static final String FTD_PACKAGE_NAME = MyApp.class.getPackage().getName();
    public static final String PREFIX_ACTION = FTD_PACKAGE_NAME + ".intent.action.";
    public static final String ACTION_SETTINGS_CHANGED = PREFIX_ACTION + "SETTINGS_CHANGED";

    private static class RecordList extends ArrayList<ActionInfo.Record> {
        public RecordList(int capacity) {
            super(capacity);
        }

        public RecordList() {
            super();
        }
    }

    public static void saveActionList(Context context, List<ActionInfo> actionList) {
        RecordList records = new RecordList(actionList.size());
        for (ActionInfo info : actionList) {
            records.add(info.toRecord());
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_RECORDS, new Gson().toJson(records))
                .apply();

        context.sendBroadcast(new Intent(ACTION_SETTINGS_CHANGED));
    }

    @NonNull
    public static ArrayList<ActionInfo> loadActionList(Context context) {
        RecordList records = new RecordList();

        records = new Gson().fromJson(
                PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_RECORDS, ""),
                records.getClass());
        if (records == null) {
            return Lists.newArrayList();
        }

        ArrayList<ActionInfo> actions = Lists.newArrayListWithCapacity(records.size());
        for (ActionInfo.Record record : records) {
            actions.add(new ActionInfo(record));
        }
        return actions;
    }

    public static void setServiceEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY_ENABLED, enabled)
                .apply();
    }

    public static boolean isServiceEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ENABLED, false);
    }
}
