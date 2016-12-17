package com.vipercn.viper4android_v3.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vipercn.viper4android_v3.R;
import com.vipercn.viper4android_v3.activity.ViPER4Android;

import java.text.DecimalFormat;

public class SeekBarPreferenceDialogFragment extends PreferenceDialogFragmentCompat
{
    private int mInitialValue = 0;
    private int mSeekBarValue = 0;
    private int mSeekBarMax = 0;
    private String[] mValues, mEntries;

    public static SeekBarPreferenceDialogFragment newInstance(String key)
    {
        SeekBarPreferenceDialogFragment fragment = new SeekBarPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult)
    {
        SeekBarPreference preference = (SeekBarPreference)this.getPreference();
        if (positiveResult)
        {
            if (preference.callChangeListener(mSeekBarValue))
                if (mValues != null)
                    preference.setValue(mValues[mSeekBarValue]);
                else
                    preference.setValue(String.valueOf(mSeekBarValue));

            getActivity().sendBroadcast(new Intent(ViPER4Android.ACTION_UPDATE_PREFERENCES));
        }
        else if(mSeekBarValue != mInitialValue)
        {
            if (preference.callChangeListener(mInitialValue))
                if (mValues != null)
                    preference.setValue(mValues[mInitialValue]);
                else
                    preference.setValue(String.valueOf(mInitialValue));

            getActivity().sendBroadcast(new Intent(ViPER4Android.ACTION_UPDATE_PREFERENCES));
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        final SeekBarPreference preference = (SeekBarPreference)getPreference();
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.seekbar_dialog, null);

        final TextView mText = (TextView)view.findViewById(R.id.value);
        final SeekBar mSeekBar = (SeekBar)view.findViewById(R.id.seekbar);

        if(preference.getLeftText() != null)
        {
            TextView left = (TextView)view.findViewById(R.id.left_text);
            left.setVisibility(View.VISIBLE);
            left.setText(preference.getLeftText());
        }
        if(preference.getRightText() != null)
        {
            TextView right = (TextView)view.findViewById(R.id.right_text);
            right.setVisibility(View.VISIBLE);
            right.setText(preference.getRightText());
        }
        mSeekBarMax = (preference.getMaximum() - preference.getMinimum()) / preference.getStep();
        mSeekBar.setMax(mSeekBarMax);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (!fromUser)
                    return;
                mSeekBarValue = progress;
                mSeekBarValue *= preference.getStep();
                mSeekBarValue += preference.getMinimum();

                // If it is sound pressure, can't be 0 as 0 = -infinity in dB
                if(preference.isSPL() & mSeekBarValue == 0)
                    mSeekBarValue = 1;

                ((ViPER4Android)getActivity()).getAudioService().setParameterPreference(preference.getKey(),
                            mValues != null? mValues[mSeekBarValue] : mSeekBarValue);

                updateText(preference, mText);
            }
        });

        if(preference.getCurrentValue() != null && mValues == null && mEntries == null)
        {
            mSeekBarValue = mInitialValue = Integer.parseInt(preference.getCurrentValue());
            mSeekBar.setProgress((mSeekBarValue - preference.getMinimum()) / preference.getStep());
            updateText(preference, mText);
        }
        else if(mValues != null)
        {
            int i;
            for (i = 0; i < mValues.length; i++)
                if (mValues[i].equals(preference.getCurrentValue()))
                    break;
            if(i > -1 && i <= mSeekBarMax)
            {
                mSeekBarValue = i;
                mSeekBar.setProgress(i);
                mText.setText(mEntries[i]);
            }
        }

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                SeekBarPreferenceDialogFragment.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            }
        });
        builder.setView(view);
    }

    public void setEntriesAndValues(String[] entries, String[] values)
    {
        this.mEntries = entries;
        this.mValues = values;
        this.mSeekBarMax = values.length - 1;
    }

    private void updateText(SeekBarPreference preference, TextView tv)
    {
        String text;
        if (preference.isSPL())
        {
            double spl = 20 * Math.log10((mSeekBarValue + preference.getOffset()) / 100d); // dB
            text = new DecimalFormat("##.#").format(spl) + " dB";
        }
        else if (mSeekBarValue > -1 && mEntries != null)
            text = mEntries[mSeekBarValue];
        else if (preference.getString() != null)
            text = String.format(preference.getString(), mSeekBarValue / preference.getDivider());
        else
            text = String.valueOf(mSeekBarValue);
        tv.setText(text);
    }
}