package com.vipercn.viper4android_v3.preference;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.vipercn.viper4android_v3.R;
import com.vipercn.viper4android_v3.service.ViPER4AndroidService;

import java.util.Locale;

public class EqualizerPreferenceDialogFragment extends PreferenceDialogFragmentCompat
{
    private EqualizerSurface /*mListEqualizer, */mDialogEqualizer;
    private ViPER4AndroidService mAudioService;

    public static EqualizerPreferenceDialogFragment newInstance(String key)
    {
        EqualizerPreferenceDialogFragment fragment = new EqualizerPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    private final ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            mAudioService = ((ViPER4AndroidService.LocalBinder)binder).getService();
            updateDspFromDialogEqualizer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mAudioService = null;
        }
    };

    @Override
    public void onDialogClosed(boolean positiveResult)
    {
        EqualizerPreference preference = (EqualizerPreference)this.getPreference();
        if (positiveResult)
        {
            StringBuilder value = new StringBuilder();
            for (int i = 0; i < 10; i++)
            {
                value.append(String.format(Locale.ROOT, "%.1f", mDialogEqualizer.getBand(i)));
                value.append(';');
            }
            preference.setValue(value.toString());
        }

        //if (mAudioService != null)
        //    mAudioService.setEqualizerLevels(null);

        getContext().unbindService(mConnection);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        final EqualizerPreference preference = (EqualizerPreference)getPreference();
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.equalizer, null);

        mDialogEqualizer = (EqualizerSurface)view.findViewById(R.id.FrequencyResponse);
        mDialogEqualizer.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                float x = event.getX();
                float y = event.getY();

                int band = mDialogEqualizer.findClosest(x);

                int wy = v.getHeight();
                float level = y / wy * (EqualizerSurface.MIN_DB - EqualizerSurface.MAX_DB) - EqualizerSurface.MIN_DB;
                if (level < EqualizerSurface.MIN_DB)
                    level = EqualizerSurface.MIN_DB;
                else if (level > EqualizerSurface.MAX_DB)
                    level = EqualizerSurface.MAX_DB;

                mDialogEqualizer.setBand(band, level);
                updateDspFromDialogEqualizer();
                return true;
            }
        });

        if (preference.getEqualizer() != null)
            for (int i = 0; i < 10; i++)
                mDialogEqualizer.setBand(i, preference.getEqualizer().getBand(i));

        Intent serviceIntent = new Intent(getContext(), ViPER4AndroidService.class);
        getContext().bindService(serviceIntent, mConnection, 0);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                EqualizerPreferenceDialogFragment.this.onClick(dialog, AlertDialog.BUTTON_POSITIVE);
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(R.string.text_normal, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                EqualizerPreferenceDialogFragment.this.onClick(dialog, AlertDialog.BUTTON_NEUTRAL);
                mDialogEqualizer.reset();
                preference.getEqualizer().reset();
                updateDspFromDialogEqualizer();
                dialog.dismiss();
            }
        });
        builder.setTitle(R.string.pref_equalizer_title);
        builder.setView(view);
    }

    private void updateDspFromDialogEqualizer()
    {
        if (mAudioService != null)
        {
            float[] levels = new float[10];
            for (int i = 0; i < levels.length; i++)
                levels[i] = mDialogEqualizer.getBand(i);
            mAudioService.setEqualizerLevels(levels);
        }
    }
}