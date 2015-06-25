package jp.tkgktyk.floatinglauncher3;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.android.launcher3.AppInfo;
import com.android.launcher3.InsettableFrameLayout;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by tkgktyk on 2015/06/16.
 */
public class Launcher extends com.android.launcher3.Launcher {

    private static final String FTD_EXTRA_PREFIX = "jp.tkgktyk.xposed.forcetouchdetector.intent.extra.";
    private static final String FTD_EXTRA_FRACTION_X = FTD_EXTRA_PREFIX + "FRACTION_X";
    private static final String FTD_EXTRA_FRACTION_Y = FTD_EXTRA_PREFIX + "FRACTION_Y";

    private static final String DH_EXTRA_PREFIX = "jp.tkgktyk.dummyhome.intent.extra.";
    private static final String DH_EXTRA_FROM_HOME = DH_EXTRA_PREFIX + "FROM_HOME";

    private Point mDisplaySize = new Point();

    private float mSize = 1.0f;
    private PointF mPointF = null;
    private PointF mFraction = new PointF();
    private Rect mContentRect = new Rect();

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadSize();
            changeSize();
        }
    };

    //------ Activity methods -------//
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setLauncherCallbacks(new LauncherCallbacks());
        super.onCreate(savedInstanceState);

        getWindowManager().getDefaultDisplay().getRealSize(mDisplaySize);

        setBackground();
        if (savedInstanceState == null) {
            parsePointFromIntent(getIntent());
            parseSizeFromIntent(getIntent());
        } else {
            parsePointFromBundle(savedInstanceState);
            mSize = savedInstanceState.getFloat(DH_EXTRA_FROM_HOME);
        }
        changeSize();
        mContentRect.set(0, 0, mDisplaySize.x, mDisplaySize.y);

        IntentFilter filter = new IntentFilter(SettingsDialogActivity.LOCAL_SIZE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        parsePointFromIntent(intent);
        parseSizeFromIntent(intent);
        changeSize();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(FTD_EXTRA_FRACTION_X, mFraction.x);
        outState.putFloat(FTD_EXTRA_FRACTION_Y, mFraction.y);
        outState.putFloat(DH_EXTRA_FROM_HOME, mSize);
    }

    private void setBackground() {
        final GradientDrawable boundary = new GradientDrawable();
        boundary.setColor(0x80000000); // dim 0.5
        boundary.setStroke(getResources().getDimensionPixelSize(R.dimen.boundary_width),
                Color.BLACK);
        FrameLayout content = (FrameLayout) findViewById(android.R.id.content);
        content.setBackground(boundary);
    }

    private void parsePointFromIntent(Intent intent) {
        if (intent != null) {
            parsePointFromBundle(intent.getExtras());
        } else {
            mPointF = null;
        }
    }

    private void parsePointFromBundle(Bundle bundle) {
        if (bundle != null &&
                bundle.containsKey(FTD_EXTRA_FRACTION_X) &&
                bundle.containsKey(FTD_EXTRA_FRACTION_Y)) {
            mPointF = new PointF();
            mFraction.x = bundle.getFloat(FTD_EXTRA_FRACTION_X);
            mFraction.y = bundle.getFloat(FTD_EXTRA_FRACTION_Y);
            if (mFraction.x > 0.5) {
                mPointF.x = mDisplaySize.x;
            } else {
                mPointF.x = 0.0f;
            }
            mPointF.y = mFraction.y * mDisplaySize.y;
        } else {
            mPointF = null;
        }
    }

    private void parseSizeFromIntent(Intent intent) {
        if (intent != null && intent.hasExtra(DH_EXTRA_FROM_HOME)) {
            mSize = 1.0f;
        } else {
            loadSize();
        }
    }

    private void loadSize() {
        mSize = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(getString(R.string.key_size), getString(R.string.default_size))
        ) / 100.0f;
    }

    private void changeSize() {
        View content = findViewById(android.R.id.content);
        content.setScaleX(mSize);
        content.setScaleY(mSize);
        if (mPointF != null) {
            content.setPivotX(mPointF.x);
            content.setPivotY(mPointF.y);
        }
        content.invalidate();
        content.getHitRect(mContentRect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mContentRect.contains(Math.round(event.getX()), Math.round(event.getY()))) {
            hide();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public class LauncherCallbacks implements com.android.launcher3.LauncherCallbacks {

        @Override
        public void preOnCreate() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {

        }

        @Override
        public void preOnResume() {

        }

        @Override
        public void onResume() {

        }

        @Override
        public void onStart() {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {

        }

        @Override
        public void onPostCreate(Bundle savedInstanceState) {

        }

        @Override
        public void onNewIntent(Intent intent) {

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {

        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            return false;
        }

        @Override
        public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {

        }

        @Override
        public void onHomeIntent() {

        }

        @Override
        public boolean handleBackPressed() {
            return false;
        }

        @Override
        public void onLauncherProviderChange() {

        }

        @Override
        public void finishBindingItems(boolean upgradePath) {

        }

        @Override
        public void onClickAllAppsButton(View v) {

        }

        @Override
        public void bindAllApplications(ArrayList<AppInfo> apps) {

        }

        @Override
        public void onClickFolderIcon(View v) {

        }

        @Override
        public void onClickAppShortcut(View v) {
            hide();
        }

        @Override
        public void onClickPagedViewIcon(View v) {

        }

        @Override
        public void onClickWallpaperPicker(View v) {

        }

        @Override
        public void onClickSettingsButton(View v) {
            Intent intent = new Intent(Launcher.this, SettingsDialogActivity.class);
            startActivity(intent);
        }

        @Override
        public void onClickAddWidgetButton(View v) {

        }

        @Override
        public void onPageSwitch(View newPage, int newPageIndex) {

        }

        @Override
        public void onWorkspaceLockedChanged() {

        }

        @Override
        public void onDragStarted(View view) {

        }

        @Override
        public void onInteractionBegin() {

        }

        @Override
        public void onInteractionEnd() {

        }

        @Override
        public boolean forceDisableVoiceButtonProxy() {
            return false;
        }

        @Override
        public boolean providesSearch() {
            return false;
        }

        @Override
        public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
            return false;
        }

        @Override
        public void startVoice() {

        }

        @Override
        public boolean hasCustomContentToLeft() {
            return false;
        }

        @Override
        public void populateCustomContentContainer() {

        }

        @Override
        public View getQsbBar() {
            return null;
        }

        @Override
        public Intent getFirstRunActivity() {
            return null;
        }

        @Override
        public boolean hasFirstRunActivity() {
            return false;
        }

        @Override
        public boolean hasDismissableIntroScreen() {
            return false;
        }

        @Override
        public View getIntroScreen() {
            return null;
        }

        @Override
        public boolean shouldMoveToDefaultScreenOnHomeIntent() {
            return false;
        }

        @Override
        public boolean hasSettings() {
            return true;
        }

        @Override
        public ComponentName getWallpaperPickerComponent() {
            return null;
        }

        @Override
        public boolean overrideWallpaperDimensions() {
            return false;
        }

        @Override
        public boolean isLauncherPreinstalled() {
            return false;
        }

        @Override
        public boolean hasLauncherOverlay() {
            return false;
        }

        @Override
        public LauncherOverlay setLauncherOverlayView(InsettableFrameLayout container, LauncherOverlayCallbacks callbacks) {
            return null;
        }
    }
}
