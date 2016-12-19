package com.vipercn.viper4android_v3.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vipercn.viper4android_v3.R;
import com.vipercn.viper4android_v3.preference.DDCListPreference;
import com.vipercn.viper4android_v3.preference.EqualizerPreference;
import com.vipercn.viper4android_v3.preference.SeekBarPreference;
import com.vipercn.viper4android_v3.preference.SummariedListPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public final class MainDSPScreen extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener, PreferenceManager.OnPreferenceTreeClickListener
{

    private static final String PREF_KEY_EQ = "viper4android.headphonefx.fireq";
    private static final String PREF_KEY_CUSTOM_EQ = "viper4android.headphonefx.fireq.custom";
    private static final String EQ_VALUE_CUSTOM = "custom";
    private static final String PREF_KEY_DDC = "viper4android.headphonefx.viperddc.enable";
    private static final String PREF_KEY_VSE = "viper4android.headphonefx.vse.enable";

    @Override
    public void onCreatePreferences(Bundle bundle, String s)
    {
        PreferenceManager prefManager = getPreferenceManager();
        //prefManager.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        prefManager.setSharedPreferencesName(ViPER4Android.SHARED_PREFERENCES_BASENAME + ".settings");

        SharedPreferences prefSettings = prefManager.getSharedPreferences();
        String mControlLevel = "0";
        try
        {
            mControlLevel = prefSettings.getString("viper4android.settings.uiprefer", mControlLevel);
        }
        catch (ClassCastException ex)
        {
            int ctrlLvl = prefSettings.getInt("viper4android.settings.uiprefer", -1);
            if(ctrlLvl > -1)
                mControlLevel = String.valueOf(ctrlLvl);
        }

        int id = getArguments().getInt("id", -1);
        String config = getArguments().getString("config");
        if(id > -1 && id < 4)
            prefManager.setSharedPreferencesName(ViPER4Android.SHARED_PREFERENCES_BASENAME + "." + config);

        try
        {
            int xmlId = R.xml.class.getField(id < 4? config + "_preferences_l" + mControlLevel : config + "_preferences").getInt(null);
            addPreferencesFromResource(xmlId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(config != null && config.equals("general"))
            checkDriverStatus();

        checkPermissions();

        prefManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        prefManager.setOnPreferenceTreeClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("InflateParams")
    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        // Enable scroll indicator with a custom layout
        RecyclerView mRecyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return mRecyclerView;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference)
    {
        if (preference instanceof SeekBarPreference)
            ((SeekBarPreference)preference).onPreferenceDisplayDialog(this, preference);
        else if (preference instanceof EqualizerPreference)
            ((EqualizerPreference)preference).onPreferenceDisplayDialog(this, preference);
        else if (preference instanceof DDCListPreference)
            ((DDCListPreference)preference).onPreferenceDisplayDialog(this, preference);
        else
            super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        //Log.i("ViPER4Android", "Update key = " + key);
        switch (key)
        {
            /* If the equalizer surface is updated, select matching pref entry or "custom". */
            case PREF_KEY_CUSTOM_EQ:
            {
                String newValue = sharedPreferences.getString(key, null);
                String desiredValue = EQ_VALUE_CUSTOM;
                SummariedListPreference preset = (SummariedListPreference)findPreference(PREF_KEY_EQ);
                for (CharSequence entry : preset.getEntryValues())
                    if (entry.equals(newValue))
                    {
                        desiredValue = newValue;
                        break;
                    }
                /* Tell listpreference that it must display something else. */
                if (!desiredValue.equals(preset.getEntry()))
                {
                    sharedPreferences.edit().putString(PREF_KEY_EQ, desiredValue).apply();
                    preset.refreshFromPreference();
                }
                break;
            }
            case PREF_KEY_EQ:
            {
                String newValue = sharedPreferences.getString(key, null);
                if (!EQ_VALUE_CUSTOM.equals(newValue))
                {
                    sharedPreferences.edit().putString(PREF_KEY_CUSTOM_EQ, newValue).apply();
                    /* Now tell the equalizer that it must display something else. */
                    EqualizerPreference eq = (EqualizerPreference)findPreference(PREF_KEY_CUSTOM_EQ);
                    eq.refreshFromPreference();
                }
                break;
            }
            case PREF_KEY_VSE:
            {
                if (sharedPreferences.getBoolean(key, false))
                {
                    SharedPreferences prefSettings = getActivity().getSharedPreferences(ViPER4Android.SHARED_PREFERENCES_BASENAME + ".settings", 0);
                    if (!prefSettings.getBoolean("viper4android.settings.vse.notice", false))
                    {
                        prefSettings.edit().putBoolean("viper4android.settings.vse.notice", true).apply();
                        AlertDialog.Builder mNotice = new AlertDialog.Builder(getActivity());
                        mNotice.setTitle("ViPER4Android");
                        mNotice.setMessage(getActivity().getResources().getString(R.string.pref_vse_tips));
                        mNotice.setNegativeButton(getActivity().getResources().getString(R.string.text_ok), null);
                        mNotice.show();
                    }
                }
                break;
            }
            case PREF_KEY_DDC:
            {
                if (sharedPreferences.getBoolean(key, false))
                {
                    SharedPreferences prefSettings = getActivity().getSharedPreferences(ViPER4Android.SHARED_PREFERENCES_BASENAME + ".settings", 0);
                    if (!prefSettings.getBoolean("viper4android.settings.viperddc.notice", false))
                    {
                        prefSettings.edit().putBoolean("viper4android.settings.viperddc.notice", true).apply();
                        AlertDialog.Builder mNotice = new AlertDialog.Builder(getActivity());
                        mNotice.setTitle("ViPER4Android");
                        mNotice.setMessage(getActivity().getResources().getString(R.string.pref_viperddc_tips));
                        mNotice.setNegativeButton(getActivity().getResources().getString(R.string.text_ok), null);
                        mNotice.show();
                    }
                }
                break;
            }
        }
        getActivity().sendBroadcast(new Intent(ViPER4Android.ACTION_UPDATE_PREFERENCES));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference)
    {
        switch (preference.getKey())
        {
            case "viper4android.settings.checkupdate":
                Uri uri = Uri.parse(getResources().getString(R.string.text_updatelink));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case "viper4android.settings.changelog":
            {
                String mChangelog_AssetsName = "Changelog_en_US";
                mChangelog_AssetsName = mChangelog_AssetsName + ".txt";
                String mChangeLog = "";
                InputStream isChglogHandle;
                try
                {
                    isChglogHandle = getContext().getAssets().open(mChangelog_AssetsName);
                    mChangeLog = readTextFile(isChglogHandle);
                    isChglogHandle.close();
                }
                catch (IOException e)
                {
                    Log.i("ViPER4Android", "Can not read changelog");
                }

                if (mChangeLog.equalsIgnoreCase(""))
                    break;
                AlertDialog.Builder mChglog = new AlertDialog.Builder(getContext());
                mChglog.setTitle(R.string.text_changelog);
                mChglog.setMessage(mChangeLog);
                mChglog.setNegativeButton(getResources().getString(R.string.text_ok), null);
                mChglog.show();
                return true;
            }
            case "viper4android.settings.driverinstall":
            {
                String menuText = preference.getTitle().toString();
                if (getResources().getString(R.string.text_uninstall).equals(menuText))
                {
                    // Please confirm the process
                    AlertDialog.Builder mConfirm = new AlertDialog.Builder(getActivity());
                    mConfirm.setTitle("ViPER4Android");
                    mConfirm.setMessage(getResources().getString(R.string.text_drvuninst_confim));
                    mConfirm.setPositiveButton(getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // Uninstall driver
                            Utils.uninstallDrv_FX();
                            AlertDialog.Builder mResult = new AlertDialog.Builder(getActivity());
                            mResult.setTitle("ViPER4Android");
                            mResult.setMessage(getResources().getString(R.string.text_drvuninst_ok));
                            mResult.setNegativeButton(getResources().getString(R.string.text_ok), null);
                            mResult.show();
                        }
                    });
                    mConfirm.setNegativeButton(getResources().getString(R.string.text_no), null);
                    mConfirm.show();
                }
                else if (getResources().getString(R.string.text_install).equals(menuText))
                {
                    Message message = new Message();
                    message.what = 0xA00A;
                    message.obj = getContext();
                    ViPER4Android.mDriverHandler.sendMessage(message);
                }
                else
                {
                    String szTip = getResources().getString(R.string.text_service_error);
                    Toast.makeText(getContext(), szTip, Toast.LENGTH_LONG).show();
                }
                return true;
            }
            case "viper4android.settings.loadprofile":
            {
                ViPER4Android v4a = (ViPER4Android)getActivity();
                v4a.loadProfileDialog();
                return true;
            }
            case "viper4android.settings.saveprofile":
            {
                ViPER4Android v4a = (ViPER4Android)getActivity();
                v4a.saveProfileDialog();
                return true;
            }
            case "viper4android.settings.visitblog":
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://vipersaudio.com/blog/")));
                return true;
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private static String readTextFile(InputStream inputStream)
    {
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            return "";
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuilder build = new StringBuilder("");
        String line;
        try {
            while ((line = reader.readLine()) != null)
            {
                build.append(line);
                build.append("\n");
            }
            reader.close();
            inputStreamReader.close();
        } catch (IOException e) {
            return "";
        }
        return build.toString();
    }

    private void checkDriverStatus()
    {
        ViPER4Android v4a = (ViPER4Android)getActivity();
        Preference pref = findPreference("viper4android.settings.driverinstall");
        if (v4a.getAudioService() == null)
        {
            pref.setSummary(R.string.summary_driver_notinstalled);
            pref.setTitle(R.string.text_install);
            pref.setEnabled(StaticEnvironment.isEnvironmentInitialized());
        }
        else
        {
            Utils.AudioEffectUtils aeuUtils = new Utils().new AudioEffectUtils();
            int[] iaDrvVer = aeuUtils.getViper4AndroidEngineVersion();
            String mDriverVersion = iaDrvVer[0] + "." + iaDrvVer[1] + "." + iaDrvVer[2] + "." + iaDrvVer[3];
            pref.setSummary(getString(R.string.summary_driver_installed, mDriverVersion));
            pref.setTitle(getResources().getString(v4a.getAudioService().getDriverIsReady()? R.string.text_uninstall: R.string.text_install));
            pref.setEnabled(StaticEnvironment.isEnvironmentInitialized());
        }
    }

    private void checkPermissions()
    {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        String[] ids = new String[] { "viper4android.headphonefx.viperddc.enable",
                "viper4android.headphonefx.viperddc.device",
                "viper4android.headphonefx.convolver.enable",
                "viper4android.headphonefx.convolver.kernel",
                "viper4android.headphonefx.convolver.crosschannel",
                "viper4android.settings.loadprofile",
                "viper4android.settings.saveprofile" };

        for(String key : ids)
        {
            Preference pref = findPreference(key);
            if(pref != null)
                pref.setEnabled(false);
        }
    }
}