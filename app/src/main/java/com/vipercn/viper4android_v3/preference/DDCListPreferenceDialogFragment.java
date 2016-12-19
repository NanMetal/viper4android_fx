package com.vipercn.viper4android_v3.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.vipercn.viper4android_v3.R;
import com.vipercn.viper4android_v3.activity.DDC;
import com.vipercn.viper4android_v3.activity.DDCDatabase;
import com.vipercn.viper4android_v3.activity.ViPER4Android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DDCListPreferenceDialogFragment extends PreferenceDialogFragmentCompat
{
    private Map<String, List<DDC>> mItems = new HashMap<>();
    private DDC mSelectedDDC, mInitialDDC;

    public static DDCListPreferenceDialogFragment newInstance(String key)
    {
        DDCListPreferenceDialogFragment fragment = new DDCListPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult)
    {
        DDCListPreference preference = (DDCListPreference)this.getPreference();
        if (positiveResult)
        {
            if (preference.callChangeListener(mSelectedDDC))
                preference.setValue(mSelectedDDC);

            getActivity().sendBroadcast(new Intent(ViPER4Android.ACTION_UPDATE_PREFERENCES));
        }
        else if(mSelectedDDC != mInitialDDC)
        {
            if (preference.callChangeListener(mInitialDDC))
                preference.setValue(mInitialDDC);

            getActivity().sendBroadcast(new Intent(ViPER4Android.ACTION_UPDATE_PREFERENCES));
        }
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        final DDCListPreference preference = (DDCListPreference)getPreference();
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.preference_ddclist, null);

        mInitialDDC = preference.getCurrentValue();

        final EditText mEditText = (EditText)view.findViewById(R.id.ddc_edittext);
        final Spinner mSpinner = (Spinner)view.findViewById(R.id.ddc_brand_spinner);
        final ListView mListView = (ListView)view.findViewById(R.id.ddc_list);

        mEditText.setVisibility(View.GONE); // TODO: filter

        final String[] brands = mItems.keySet().toArray(new String[mItems.keySet().size()]);
        Arrays.sort(brands);

        ArrayAdapter adapter = new ArrayAdapter<>(mSpinner.getContext(),
                android.R.layout.simple_spinner_dropdown_item, brands);

        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                int index = 0;
                for (; index < brands.length; index++)
                    if (brands[index].equalsIgnoreCase(mSpinner.getItemAtPosition(i).toString()))
                        break;

                ArrayList<DDC> allddcs = (ArrayList<DDC>)mItems.get(brands[index]);

                ArrayList<String> ddcs = new ArrayList<>();
                ArrayList<String> values = new ArrayList<>();
                int selection = 0;
                for (int a = 0; a < allddcs.size(); a++)
                {
                    DDC ddc = allddcs.get(a);
                    if(ddc.getID().equals(mInitialDDC.getID()))
                        selection = a;

                    ddcs.add(ddc.getName());
                    values.add(ddc.getID());
                }

                ArrayAdapter<String> items = new ArrayAdapter<>(view.getContext(),
                        android.R.layout.simple_list_item_single_choice, ddcs);

                mListView.setAdapter(items);
                mListView.setTag(values);
                mListView.setSelection(selection);
                mListView.setItemChecked(selection, true);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        int index = 0;
        for (; index < brands.length; index++)
            if (brands[index].equalsIgnoreCase(mInitialDDC.getBrand()))
                break;
        mSpinner.setSelection(index);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                mListView.setItemChecked(i, true);
                @SuppressWarnings("unchecked")
                ArrayList<String> values = (ArrayList<String>)mListView.getTag();
                mSelectedDDC = preference.getDDCbyID(values.get(i));


                //Log.d("viper", "SELECTED: " + mSelectedDDC.getBrand() + " - " + mSelectedDDC.getName() + " - " + mSelectedDDC.getID());
                //test
                String deviceDDCCoeffs = DDCDatabase.queryDDCBlock(mSelectedDDC.getID(), view.getContext().getApplicationContext());
                float[] ddcCoeffs = DDCDatabase.blockToFloatArray(deviceDDCCoeffs);
                ((ViPER4Android)getActivity()).getAudioService().setParameterPreference(preference.getKey(), ddcCoeffs);
            }
        });


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                DDCListPreferenceDialogFragment.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            }
        });
        builder.setView(view);
    }

    public void setDDCEntries(ArrayList<DDC> ddcEntries)
    {
        for(DDC ddc : ddcEntries)
        {
            if(!mItems.containsKey(ddc.getBrand()))
                this.mItems.put(ddc.getBrand(), new ArrayList<DDC>());

            this.mItems.get(ddc.getBrand()).add(ddc);
        }
    }
}