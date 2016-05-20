package jp.tkgktyk.floatingnavigation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Utilities;
import com.google.common.base.Strings;

import java.io.Serializable;
import java.net.URISyntaxException;

/**
 * Created by tkgktyk on 2015/07/02.
 */
public class ActionInfo {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_TOOL = 1;
    public static final int TYPE_APP = 2;
    public static final int TYPE_SHORTCUT = 3;

    private int mType;
    private Intent mIntent;
    private Bitmap mIcon;
    private String mName;

    public ActionInfo() {
        setNone();
    }

    public ActionInfo(Context context, Intent intent, int type) {
        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo, so we return null
            MyApp.logE("Can't construct ActionInfo with null intent");
            setNone();
            return;
        }
        mType = type;
        switch (type) {
            case TYPE_TOOL:
                fromToolIntent(context, intent);
                break;
            case TYPE_APP:
                fromAppIntent(context, intent);
                break;
            case TYPE_SHORTCUT:
                fromShortcutIntent(context, intent);
                break;
            case TYPE_NONE:
            default:
                setNone();
        }
    }

    public ActionInfo(Record record) {
        try {
            mIntent = Intent.parseUri(record.intentUri, 0);
            mType = record.type;
            if (!Strings.isNullOrEmpty(record.iconBase64)) {
                byte[] iconArray = Base64.decode(record.iconBase64, Base64.DEFAULT);
                mIcon = BitmapFactory.decodeByteArray(iconArray, 0, iconArray.length);
            }
            mName = record.name;
        } catch (URISyntaxException e) {
            MyApp.logE(e);
            setNone();
        }
    }

    public Record toRecord() {
        Record record = new Record();

        record.type = mType;
        record.intentUri = getUri();
        record.name = mName;

        if (mIcon != null) {
            byte[] iconByteArray = ItemInfo.flattenBitmap(mIcon);
            record.iconBase64 = Base64.encodeToString(iconByteArray, 0, iconByteArray.length,
                    Base64.DEFAULT);
        }

        return record;
    }

    private void setNone() {
        mIntent = null;
        mType = TYPE_NONE;
        mName = null;
        mIcon = null;
    }

    private void setNotFound() {
        mIntent = null;
        mName = null;
        mIcon = null;
    }

    private void fromToolIntent(Context context, @NonNull Intent intent) {
        mIntent = intent;
        mName = FTD.getActionName(context, intent.getAction());
        mIcon = BitmapFactory.decodeResource(context.getResources(),
                FTD.getActionIconResource(intent.getAction()));
    }

    private void fromAppIntent(Context context, @NonNull Intent intent) {
        mIntent = intent;
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo(intent.getComponent().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (ai != null) {
            mName = ai.loadLabel(pm).toString();
            mIcon = Utilities.createIconBitmap(ai.loadIcon(pm), context);
        } else {
            setNotFound();
        }
    }

    private void fromShortcutIntent(Context context, @NonNull Intent intent) {
        mIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        mName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Bitmap icon = null;

        if (bitmap instanceof Bitmap) {
            icon = Utilities.createIconBitmap((Bitmap) bitmap, context);
        } else {
            Parcelable extra = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra instanceof Intent.ShortcutIconResource) {
                Intent.ShortcutIconResource iconResource = (Intent.ShortcutIconResource) extra;
                PackageManager packageManager = context.getPackageManager();
                // the resource
                try {
                    Resources resources = packageManager
                            .getResourcesForApplication(iconResource.packageName);
                    if (resources != null) {
                        final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                        icon = Utilities.createIconBitmap(resources.getDrawable(id), context);
                    }
                } catch (Exception e) {
                    // Icon not found.
                }
            }
        }
        mIcon = icon;
    }

    @NonNull
    public String getUri() {
        if (mIntent == null) {
            return "";
        }
        return mIntent.toUri(0);
    }

    public Intent getIntent() {
        return mIntent;
    }

    public int getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public static class Record implements Serializable {
        private static final long serialVersionUID = 1L;

        int type;
        String intentUri;
        String iconBase64;
        String name;
    }
}
