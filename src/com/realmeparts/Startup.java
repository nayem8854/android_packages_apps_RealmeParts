/*
 * Copyright (C) 2013 The OmniROM Project
 * Copyright (C) 2024 Hyperteam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.realmeparts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.realmeparts.doze.DozeUtils;

public class Startup extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    private void restore(String file, boolean enabled) {
        if (file == null) {
            return;
        }
        if (enabled) {
            Utils.writeValue(file, "1");
        }
    }

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        // Doze gestures: OplusDoze only — do not start RealmeParts DozeService.
        // AOD panel light mode still uses AodLightService when system AOD is on.
        if (DozeUtils.isAlwaysOnEnabled(context)) {
            Intent serviceIntent = new Intent(context, AodLightService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }

        boolean enabled;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // CABC QS tile only on devices listed in realmeparts_features.json
        boolean cabc = Utils.isFeatureMatched(context, DeviceSettings.KEY_CABC);
        sharedPrefs.edit().putBoolean("CABC_DeviceMatched", cabc).apply();
        Utils.setComponentEnabled(context, CABCTileService.class, cabc);

        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_DC_SWITCH, false);
        restore(DCModeSwitch.getFile(), enabled);

        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_SRGB_SWITCH, false);
        restore(SRGBModeSwitch.getFile(), enabled);

        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_OTG_SWITCH, false);
        restore(OTGModeSwitch.getFile(), enabled);

        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_GAME_SWITCH, false);
        restore(GameModeSwitch.getFile(), enabled);
        if (enabled) {
            Utils.startService(context, GameModeRotationService.class);
        }

        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_HBM_SWITCH, false);
        restore(HBMModeSwitch.getFile(), enabled);
        if (enabled) {
            Utils.startService(context, HBMService.class);
        }

        enabled = sharedPrefs.getBoolean(DeviceSettings.KEY_FPS_INFO, false);
        if (enabled) {
            Utils.startService(context, FPSInfoService.class);
        }
    }
}
