package com.vipercn.viper4android_v3.preference;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.vipercn.viper4android_v3.R;

public class CategorySwitchPreference extends SwitchPreference
{
    private final Listener mListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener
    {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                buttonView.setChecked(!isChecked);
                return;
            }

            CategorySwitchPreference.this.setChecked(isChecked);
        }
    }

    public CategorySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.preference_category_switch);
    }

    public CategorySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CategorySwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                android.support.v7.preference.R.attr.switchPreferenceStyle,
                android.R.attr.switchPreferenceStyle));
    }

    public CategorySwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);
        Switch mSwitch = (Switch)holder.findViewById(R.id.switch_widget);
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch.setChecked(isChecked());
        mSwitch.setOnCheckedChangeListener(mListener);
        mSwitch.setText(getTitle());
    }
}
