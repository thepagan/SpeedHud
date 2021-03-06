/*
 * Copyright (C) 2013 EU Edge LLC
 *
 * This code is modification of a work of The Android Open Source Project,
 * see the original license statement below.
 *
 * Copyright (C) 2013 The Android Open Source Project
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
package com.euedge.glass.speedhud;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This activity manages the options menu that appears when the user taps on the
 * speed hud's live card.
 */
public class SpeedHudMenuActivity extends Activity {

    private final Handler mHandler = new Handler();
    private SpeedHudService.SpeedHudBinder mSpeedHudService;
    private boolean mAttachedToWindow;
    private boolean mOptionsMenuOpen;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof SpeedHudService.SpeedHudBinder) {
                mSpeedHudService = (SpeedHudService.SpeedHudBinder) service;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Do nothing.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, SpeedHudService.class), mConnection, 0);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        openOptionsMenu();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
    }

    @Override
    public void openOptionsMenu() {
        if (!mOptionsMenuOpen && mAttachedToWindow && mSpeedHudService != null) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.speed_hud, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.uom_kmh:
            mSpeedHudService.getSpeedHudService().setUom(SpeedHudView.UOM_KMH);
            return true;
        case R.id.uom_mph:
            mSpeedHudService.getSpeedHudService().setUom(SpeedHudView.UOM_MPH);
            return true;
        case R.id.uom_kt:
            mSpeedHudService.getSpeedHudService().setUom(SpeedHudView.UOM_KT);
            return true;
        case R.id.uom_mps:
            mSpeedHudService.getSpeedHudService().setUom(SpeedHudView.UOM_MPS);
            return true;
        case R.id.stop:
            // Stop the service at the end of the message queue for proper options menu
            // animation. This is only needed when starting an Activity or stopping a Service
            // that published a LiveCard.
            mHandler.post(new Runnable() {
                @Override
                 public void run() {
                     stopService(new Intent(SpeedHudMenuActivity.this, SpeedHudService.class));
                 }
             });
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        mOptionsMenuOpen = false;

        unbindService(mConnection);

        // We must call finish() from this method to ensure that the activity ends either when an
        // item is selected from the menu or when the menu is dismissed by swiping down.
        finish();
    }
}
