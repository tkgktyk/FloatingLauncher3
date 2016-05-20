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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.common.base.Objects;

import java.util.ArrayList;

/**
 * Created by tkgktyk on 2015/06/15.
 */
public class FloatingNavigation implements View.OnClickListener {

    private final WindowManager mWindowManager;
    private final Point mDisplaySize;
    private final WindowManager.LayoutParams mLayoutParams;
    private final CircleLayoutForFAB mCircleLayout;

    private final PointF mFraction = new PointF();

    private boolean mNavigationShown;

    private ArrayList<ActionInfo> mActionList;
    private Intent mOriginalIntent;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.equal(action, FTD.ACTION_FLOATING_NAVIGATION)) {
                mOriginalIntent = intent;
                mFraction.x = intent.getFloatExtra(FTD.EXTRA_FRACTION_X, 0.0f);
                mFraction.y = intent.getFloatExtra(FTD.EXTRA_FRACTION_Y, 0.0f);
                show();
            } else if (Objects.equal(action, MyApp.ACTION_SETTINGS_CHANGED)) {
                loadActions(context);
            }
        }
    };

    public FloatingNavigation(Context context) {
        context.setTheme(R.style.AppTheme);
        mCircleLayout = (CircleLayoutForFAB) LayoutInflater.from(context)
                .inflate(R.layout.view_floating_navigation, null);

        mCircleLayout.hide();
        mCircleLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return true;
            }
        });
        loadActions(context);

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplaySize = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(mDisplaySize);
        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowManager.addView(mCircleLayout, mLayoutParams);
        show();

        IntentFilter filter = new IntentFilter();
        filter.addAction(FTD.ACTION_FLOATING_NAVIGATION);
        filter.addAction(MyApp.ACTION_SETTINGS_CHANGED);
        context.registerReceiver(mBroadcastReceiver, filter);
    }

    private void loadActions(Context context) {
        mCircleLayout.removeAllViews();
        // TODO: load actions
        mActionList = MyApp.loadActionList(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        for (ActionInfo action : mActionList) {
            FloatingActionButton button = (FloatingActionButton) inflater
                    .inflate(R.layout.view_floating_action, mCircleLayout, false);
            button.setOnClickListener(this);
            button.setTag(action);
            button.setImageBitmap(action.getIcon());
            mCircleLayout.addView(button);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO: perform action
        hide();
        ActionInfo action = (ActionInfo) v.getTag();
        FTD.performAction(v.getContext(), mOriginalIntent, action.getIntent());
    }

    private void show() {
        hide();
        float x;
        float y = mFraction.y * mDisplaySize.y;
        float rotation;
        if (mFraction.x > 0.5) {
            x = mDisplaySize.x;
            rotation = 180.0f;
            mCircleLayout.setReverseDirection(true);
        } else {
            x = 0.0f;
            rotation = 0.0f;
            mCircleLayout.setReverseDirection(false);
        }
        mNavigationShown = true;
        mCircleLayout.show(x, y, rotation);
        mCircleLayout.setVisibility(View.VISIBLE);
    }

    private void hide() {
        if (mNavigationShown) {
            mCircleLayout.setVisibility(View.GONE);
            mCircleLayout.hide();
            mNavigationShown = false;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        mWindowManager.getDefaultDisplay().getRealSize(mDisplaySize);
        if (mNavigationShown) {
            show();
        }
    }

    public void onDestroy(Context context) {
        hide();
        context.unregisterReceiver(mBroadcastReceiver);
    }
}
