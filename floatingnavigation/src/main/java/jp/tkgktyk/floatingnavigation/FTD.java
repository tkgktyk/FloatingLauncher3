/*
 * Copyright 2015 Takagi Katsuyuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.tkgktyk.floatingnavigation;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.google.common.base.Strings;

/**
 * Created by tkgktyk on 2015/06/03.
 */
public class FTD {
    public static final String FTD_PACKAGE_NAME = "jp.tkgktyk.xposed.forcetouchdetector";
    public static final String PREFIX_ACTION = FTD_PACKAGE_NAME + ".intent.action.";
    public static final String PREFIX_EXTRA = FTD_PACKAGE_NAME + ".intent.extra.";

    public static final String ACTION_BACK = PREFIX_ACTION + "BACK";
    public static final String ACTION_HOME = PREFIX_ACTION + "HOME";
    public static final String ACTION_RECENTS = PREFIX_ACTION + "RECENTS";
    public static final String ACTION_EXPAND_NOTIFICATIONS = PREFIX_ACTION + "EXPAND_NOTIFICATIONS";
    public static final String ACTION_EXPAND_QUICK_SETTINGS = PREFIX_ACTION + "EXPAND_QUICK_SETTINGS";
    public static final String ACTION_KILL = PREFIX_ACTION + "KILL";

    public static final String ACTION_FLOATING_NAVIGATION = PREFIX_ACTION + "FLOATING_NAVIGATION";

    public static final String EXTRA_FRACTION_X = PREFIX_EXTRA + "FRACTION_X";
    public static final String EXTRA_FRACTION_Y = PREFIX_EXTRA + "FRACTION_Y";

    public static String getActionName(Context context, String action) {
        if (action.equals(ACTION_BACK)) {
            return context.getString(R.string.action_back);
        } else if (action.equals(ACTION_HOME)) {
            return context.getString(R.string.action_home);
        } else if (action.equals(ACTION_RECENTS)) {
            return context.getString(R.string.action_recents);
        } else if (action.equals(ACTION_EXPAND_NOTIFICATIONS)) {
            return context.getString(R.string.action_expand_notifications);
        } else if (action.equals(ACTION_EXPAND_QUICK_SETTINGS)) {
            return context.getString(R.string.action_expand_quick_settings);
        } else if (action.equals(ACTION_KILL)) {
            return context.getString(R.string.action_kill);
        }
        return "";
    }

    public static @DrawableRes int getActionIconResource(String action) {
        if (action.equals(ACTION_BACK)) {
            return R.drawable.ic_sysbar_back;
        } else if (action.equals(ACTION_HOME)) {
            return R.drawable.ic_sysbar_home;
        } else if (action.equals(ACTION_RECENTS)) {
            return R.drawable.ic_sysbar_recent;
        } else if (action.equals(ACTION_EXPAND_NOTIFICATIONS)) {
            return R.drawable.ic_settings_white_24dp;
        } else if (action.equals(ACTION_EXPAND_QUICK_SETTINGS)) {
            return R.drawable.ic_notifications_none_white_24dp;
        } else if (action.equals(ACTION_KILL)) {
            return R.drawable.ic_close_white_24dp;
        }
        return 0;
    }

    public static boolean performAction(Context context, Intent original, Intent intent) {
        if (intent == null) {
            return false;
        }
        if (original != null) {
            if (original.hasExtra(EXTRA_FRACTION_X)) {
                intent.putExtra(EXTRA_FRACTION_X, original.getFloatExtra(EXTRA_FRACTION_X, 0.0f));
            }
            if (original.hasExtra(EXTRA_FRACTION_Y)) {
                intent.putExtra(EXTRA_FRACTION_Y, original.getFloatExtra(EXTRA_FRACTION_Y, 0.0f));
            }
        }
        if (isLocalAction(intent)) {
            context.sendBroadcast(intent);
            return true;
        }
        if (intent.getComponent() == null) {
            return false;
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MyApp.showToast(R.string.not_found);
        }
        return true;
    }

    public static boolean isLocalAction(@NonNull Intent intent) {
        return isLocalAction(intent.getAction());
    }

    public static boolean isLocalAction(String action) {
        return !Strings.isNullOrEmpty(action) && action.startsWith(PREFIX_ACTION);
    }

}
