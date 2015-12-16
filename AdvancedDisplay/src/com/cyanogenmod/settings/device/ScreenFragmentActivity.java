/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.cyanogenmod.settings.device;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.cyanogenmod.settings.device.R;

public class ScreenFragmentActivity extends PreferenceFragment {

    private static final String PREF_ENABLED = "1";
    private static final String TAG = "DisplaySettings_Screen";

    private static final String FILE_USE_ACCELEROMETER_CALIB = "/sys/class/sec/gsensorcal/calibration";

    private mDNIeScenario mmDNIeScenario;
    private mDNIeMode mmDNIeMode;
    private mDNIeNegative mmDNIeNegative;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.screen_preferences);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Resources res = getResources();

        /* mDNIe */
        mmDNIeScenario = (mDNIeScenario) findPreference(DisplaySettings.KEY_MDNIE_SCENARIO);
        mmDNIeScenario.setEnabled(mDNIeScenario.isSupported(res.getString(R.string.mdnie_scenario_sysfs_file)));

        mmDNIeMode = (mDNIeMode) findPreference(DisplaySettings.KEY_MDNIE_MODE);
        mmDNIeMode.setEnabled(mDNIeMode.isSupported(res.getString(R.string.mdnie_mode_sysfs_file)));

        mmDNIeNegative = (mDNIeNegative) findPreference(DisplaySettings.KEY_MDNIE_NEGATIVE);
        mmDNIeNegative.setEnabled(mDNIeNegative.isSupported(res.getString(R.string.mdnie_negative_sysfs_file)));

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        String boxValue;
        String key = preference.getKey();
        Log.w(TAG, "key: " + key);

        if (key.compareTo(DisplaySettings.KEY_USE_ACCELEROMETER_CALIBRATION) == 0) {
            boxValue = (((CheckBoxPreference)preference).isChecked() ? "1" : "0");
            Utils.writeValue(FILE_USE_ACCELEROMETER_CALIB, boxValue);
        } else if (key.compareTo(DisplaySettings.KEY_CALIBRATE_ACCELEROMETER) == 0) {
            // when calibration data utilization is disablen and enabled back,
            // calibration is done at the same time by driver
            Utils.writeValue(FILE_USE_ACCELEROMETER_CALIB, "0");
            Utils.writeValue(FILE_USE_ACCELEROMETER_CALIB, "1");
            Utils.showDialog((Context)getActivity(), getString(R.string.accelerometer_dialog_head), getString(R.string.accelerometer_dialog_message));
        }

        return true;
    }

    public static boolean isSupported(String FILE) {
        return Utils.fileExists(FILE);
    }

    public static void restore(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean accelerometerCalib = sharedPrefs.getBoolean(DisplaySettings.KEY_USE_ACCELEROMETER_CALIBRATION, true);

        // When use accelerometer calibration value is set to 1, calibration is done at the same time, which
        // means it is reset at each boot, providing wrong calibration most of the time at each reboot.
        // So we only set it to "0" if user wants it, as it defaults to 1 at boot
        if (!accelerometerCalib)
            Utils.writeValue(FILE_USE_ACCELEROMETER_CALIB, "0");
    }
}
