package com.vipercn.viper4android_v3.preference;

import android.content.Context;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.vipercn.viper4android_v3.R;
import com.vipercn.viper4android_v3.activity.StaticEnvironment;
import com.vipercn.viper4android_v3.activity.Utils;
import com.vipercn.viper4android_v3.activity.ViPER4Android;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class KernelListPreference extends ListPreference
{
    private String mValue;
    private String mDefaultValue;

    public KernelListPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setup(this);
    }

    private void setup(KernelListPreference pref)
    {
        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.i("ViPER4Android", "External storage not mounted");
                pref.setEntries(new String[0]);
                pref.setEntryValues(new String[0]);

                Snackbar.make(((ViPER4Android)getContext()).getFragmentContainerView(),
                        getContext().getResources().getString(R.string.text_ir_dir_isempty), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                /*String tip = getContext().getResources().getString(R.string.text_ir_dir_isempty);
                tip = String.format(tip, StaticEnvironment.getV4aKernelPath());
                Toast.makeText(getContext(), tip, Toast.LENGTH_LONG).show();*/
                return;
            }

            final String kernelPath = StaticEnvironment.getV4aKernelPath();
            File kernelFile = new File(kernelPath);

            if (!kernelFile.exists()) {
                Log.i("ViPER4Android", "Kernel directory does not exists");
                kernelFile.mkdirs();
                kernelFile.mkdir();
            } else Log.i("ViPER4Android", "Kernel directory exists");

            ArrayList<String> kernelList = new ArrayList<>();
            Utils.getFileNameList(kernelFile, ".irs", kernelList);
            Utils.getFileNameList(kernelFile, ".wav", kernelList);

            if (kernelList.isEmpty()) {
                String tip = getContext().getResources().getString(R.string.text_ir_dir_isempty);
                tip = String.format(tip, StaticEnvironment.getV4aKernelPath());
                Toast.makeText(getContext(), tip, Toast.LENGTH_LONG).show();
            } else Collections.sort(kernelList);

            final String[] kernelArray = new String[kernelList.size()];
            final String[] arrayValue = new String[kernelList.size()];
            for (int i = 0; i < kernelList.size(); i++) {
                kernelArray[i] = kernelList.get(i);
                arrayValue[i] = kernelPath + kernelList.get(i);
            }

            pref.setEntries(kernelArray);
            pref.setEntryValues(arrayValue);
        } catch (Exception e) {
            pref.setEntries(new String[0]);
            pref.setEntryValues(new String[0]);
            String tip = getContext().getResources().getString(R.string.text_ir_dir_isempty);
            tip = String.format(tip, StaticEnvironment.getV4aKernelPath());
            Toast.makeText(getContext(), tip, Toast.LENGTH_LONG).show();
        }
    }

    public void setValue(String value)
    {
        this.mValue = value;
        this.persistString(value);
        this.updateSummary();
        this.notifyChanged();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        this.setValue(restoreValue ? this.getPersistedString(this.mDefaultValue) : String.valueOf(defaultValue));
    }

    public String getCurrentValue()
    {
        return mValue;
    }

    @Override
    public void setDefaultValue(Object defaultValue)
    {
        mValue = mDefaultValue = String.valueOf(defaultValue);
        super.setDefaultValue(defaultValue);
    }

    private void updateSummary()
    {
        if (getEntries() == null || getEntryValues() == null)
        {
            setSummary("");
            return;
        }
        if (mValue != null)
        {
            if (mValue.contains("/"))
            {
                String fileName = mValue.substring(mValue.lastIndexOf("/") + 1);
                setSummary(fileName);
                return;
            }
            setSummary(mValue);
        }
        else
            for (CharSequence evalue : getEntryValues())
                if (evalue.toString().equals(mValue))
                {
                    setSummary(evalue);
                    return;
                }

    }
}
