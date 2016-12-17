package com.vipercn.viper4android_v3.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;

import com.vipercn.viper4android_v3.R;

import java.text.DecimalFormat;

public class SeekBarPreference extends DialogPreference implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
{
    private String mValue;
    private String mDefaultValue;
    private String mLeftText, mRightText, mString;

    private String[] mEntries, mValues;
    private int mStep = 1, mMinimum = 0, mMaximum = 100, mOffset = 0;
    private int mDivider = 1;
    private boolean mSPL = false;

    public SeekBarPreference(Context context)
    {
        super(context);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs)
    {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
        try
        {
            if (ta.hasValue(R.styleable.SeekBarPreference_android_defaultValue))
                mValue = mDefaultValue = ta.getString(R.styleable.SeekBarPreference_android_defaultValue);
            if (ta.hasValue(R.styleable.SeekBarPreference_maximum))
                mMaximum = ta.getInt(R.styleable.SeekBarPreference_maximum, mMaximum);
            if (ta.hasValue(R.styleable.SeekBarPreference_formatString))
                mString = ta.getString(R.styleable.SeekBarPreference_formatString);
            if (ta.hasValue(R.styleable.SeekBarPreference_minimum))
                mMinimum = ta.getInt(R.styleable.SeekBarPreference_minimum, mMinimum);
            if (ta.hasValue(R.styleable.SeekBarPreference_step))
                mStep = ta.getInt(R.styleable.SeekBarPreference_step, mStep);
            if (ta.hasValue(R.styleable.SeekBarPreference_divisor))
                mDivider = ta.getInt(R.styleable.SeekBarPreference_divisor, mDivider);
            if (ta.hasValue(R.styleable.SeekBarPreference_spl))
                mSPL = ta.getBoolean(R.styleable.SeekBarPreference_spl, mSPL);
            if (ta.hasValue(R.styleable.SeekBarPreference_offset))
                mOffset = ta.getInt(R.styleable.SeekBarPreference_offset, mOffset);
            if (ta.hasValue(R.styleable.SeekBarPreference_android_entries))
            {
                int entriesId = ta.getResourceId(R.styleable.SeekBarPreference_android_entries, 0);
                if (entriesId > 0)
                    mEntries = context.getResources().getStringArray(entriesId);
            }
            if (ta.hasValue(R.styleable.SeekBarPreference_android_entryValues))
            {
                int valuesId = ta.getResourceId(R.styleable.SeekBarPreference_android_entryValues, 0);
                if (valuesId > 0)
                    mValues = context.getResources().getStringArray(valuesId);
                mMaximum = mValues.length - 1;
            }
            if (ta.hasValue(R.styleable.SeekBarPreference_rightText))
                mRightText = ta.getString(R.styleable.SeekBarPreference_rightText);
            if (ta.hasValue(R.styleable.SeekBarPreference_leftText))
                mLeftText = ta.getString(R.styleable.SeekBarPreference_leftText);
        }
        finally
        {
            ta.recycle();
        }
        updateSummary();
    }

    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat preferenceFragmentCompat, Preference preference)
    {
        SeekBarPreferenceDialogFragment mFragment = SeekBarPreferenceDialogFragment.newInstance(preference.getKey());
        mFragment.setTargetFragment(preferenceFragmentCompat, 0);
        if (mEntries != null && mValues != null)
            mFragment.setEntriesAndValues(mEntries, mValues);
        mFragment.show(preferenceFragmentCompat.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        return true;
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
        this.setValue(restoreValue ? this.getPersistedString(this.mDefaultValue) : (String)defaultValue);
    }

    public String getCurrentValue()
    {
        return mValue;
    }

    @Override
    public void setDefaultValue(Object defaultValue)
    {
        mValue = mDefaultValue = (String)defaultValue;
        super.setDefaultValue(defaultValue);
    }

    public String getDefaultValue()
    {
        return mDefaultValue;
    }

    public int getMaximum()
    {
        return mMaximum;
    }

    public String getString()
    {
        return mString;
    }

    public int getMinimum()
    {
        return mMinimum;
    }

    public int getOffset()
    {
        return mOffset;
    }

    public int getStep()
    {
        return this.mStep;
    }

    public int getDivider()
    {
        return this.mDivider;
    }

    public String getRightText()
    {
        return this.mRightText;
    }

    public String getLeftText()
    {
        return this.mLeftText;
    }

    public boolean isSPL()
    {
        return mSPL;
    }

    private void updateSummary()
    {
        if(mEntries != null)
        {
            int i;
            for (i = 0; i < mValues.length; i++)
                if (mValues[i].equals(String.valueOf(getCurrentValue())))
                    break;
            this.setSummary(mEntries[i]);
        }
        else if(mString != null)
            this.setSummary(String.format(mString, Integer.valueOf(mValue) / getDivider()));
        else if(mSPL)
        {
            double spl = 20 * Math.log10((Integer.valueOf(mValue) + getOffset()) / 100d); // dB
            this.setSummary(new DecimalFormat("##.#").format(spl) + " dB");
        }
        else
            this.setSummary(mValue);
    }
}
