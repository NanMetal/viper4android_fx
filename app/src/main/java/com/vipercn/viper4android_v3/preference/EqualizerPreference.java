package com.vipercn.viper4android_v3.preference;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import com.vipercn.viper4android_v3.R;

public class EqualizerPreference extends DialogPreference implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
{
    private EqualizerSurface mEqualizer;

    public EqualizerPreference(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        setLayoutResource(R.layout.equalizer);
    }

    private void updateListEqualizerFromValue()
    {
        String value = getPersistedString(null);
        if (value != null && mEqualizer != null)
        {
            String[] levelsStr = value.split(";");
            for (int i = 0; i < 10; i++)
                mEqualizer.setBand(i, Float.valueOf(levelsStr[i]));
        }
    }

    public void setValue(String value)
    {
        this.persistString(value);
        updateListEqualizerFromValue();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
    {
        String value = restorePersistedValue ? getPersistedString(null) : (String) defaultValue;
        if (shouldPersist())
            persistString(value);
    }

    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat preferenceFragmentCompat, Preference preference)
    {
        EqualizerPreferenceDialogFragment mFragment = EqualizerPreferenceDialogFragment.newInstance(preference.getKey());
        mFragment.setTargetFragment(preferenceFragmentCompat, 0);
        mFragment.show(preferenceFragmentCompat.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        return true;
    }

    public void refreshFromPreference() {
        onSetInitialValue(true, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);
        mEqualizer = (EqualizerSurface)holder.findViewById(R.id.FrequencyResponse);
        updateListEqualizerFromValue();
    }

    EqualizerSurface getEqualizer()
    {
        return mEqualizer;
    }
}
