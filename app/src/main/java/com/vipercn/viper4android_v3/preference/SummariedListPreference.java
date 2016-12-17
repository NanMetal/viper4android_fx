package com.vipercn.viper4android_v3.preference;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

public class SummariedListPreference extends ListPreference
{

    public SummariedListPreference(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);

        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        if(getEntries() != null && getValue() != null)
        for (int i = 0; i < entryValues.length; i++) {
            if (entryValues[i].equals(value)) {
                setSummary(entries[i]);
                break;
            }
        }
    }

    public void refreshFromPreference() {
        onSetInitialValue(true, null);
    }
}
