package com.vipercn.viper4android_v3.preference;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.vipercn.viper4android_v3.activity.DDC;
import com.vipercn.viper4android_v3.activity.DDCDatabase;
import com.vipercn.viper4android_v3.activity.StaticEnvironment;
import com.vipercn.viper4android_v3.activity.Utils;

import java.io.File;
import java.util.ArrayList;

public class DDCListPreference extends DialogPreference implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
{
    private DDC mValue;
    private ArrayList<DDC> mDDCMMData;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public DDCListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mDDCMMData = DDCDatabase.queryManufacturerAndModel(getContext());
        if (mDDCMMData == null)
            mDDCMMData = new ArrayList<>();

        final String customDDCPath = StaticEnvironment.getV4aCustomDDCPath();
        File customDDCFile = new File(customDDCPath);
        if (!customDDCFile.exists())
        {
            Log.i("ViPER4Android", "Custom DDC directory does not exists");
            customDDCFile.mkdirs();
            customDDCFile.mkdir();
        }
        else
            Log.i("ViPER4Android", "Custom DDC directory exists");

        ArrayList<String> customDDCList = new ArrayList<>();
        Utils.getFileNameList(customDDCFile, ".vdc", customDDCList);
        if (!customDDCList.isEmpty())
            for (int i = 0; i < customDDCList.size(); i++)
                mDDCMMData.add(new DDC("FILE:" + customDDCList.get(i), customDDCList.get(i), customDDCList.get(i)));

        /*try
        {
            final String[] entriesArray = new String[mDDCMMData.size()];
            final String[] valuesArray = new String[mDDCMMData.size()];
            int index = 0;
            for (DDC ddc : mDDCMMData)
            {
                entriesArray[index] = ddc.getBrand() + " - " + ddc.getName();
                valuesArray[index] = ddc.getID();
                index++;
            }
            //setEntries(entriesArray);
            //setEntryValues(valuesArray);
        }
        catch (Exception e)
        {
            //setEntries(new String[0]);
            //setEntryValues(new String[0]);
        }*/
    }

    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat preferenceFragmentCompat, Preference preference)
    {
        DDCListPreferenceDialogFragment mFragment = DDCListPreferenceDialogFragment.newInstance(preference.getKey());
        mFragment.setTargetFragment(preferenceFragmentCompat, 0);
        if (mDDCMMData != null)
            mFragment.setDDCEntries(mDDCMMData);
        mFragment.show(preferenceFragmentCompat.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        return true;
    }

    public void setValue(DDC value)
    {
        this.mValue = value;
        this.persistString(value.getID());
        this.updateSummary();
        this.notifyChanged();
    }

    public DDC getCurrentValue()
    {
        return mValue;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        this.setValue(restoreValue ? getDDCbyID(this.getPersistedString(null)) : getDDCbyID(String.valueOf(defaultValue)));
    }

    private void updateSummary()
    {
        DDC ddc = mValue;
        if (ddc != null)
            setSummary(ddc.getBrand() + " - " + ddc.getName());
        else
            setSummary("");
    }

    public DDC getDDCbyID(String id)
    {
        for (DDC ddc : mDDCMMData)
            if (ddc.getID().equals(id))
                return ddc;
        return null;
    }
}
