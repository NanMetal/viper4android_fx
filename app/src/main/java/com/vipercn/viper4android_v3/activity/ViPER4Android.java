package com.vipercn.viper4android_v3.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vipercn.viper4android_v3.R;
import com.vipercn.viper4android_v3.service.ViPER4AndroidService;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class ViPER4Android extends AppCompatActivity
{

    //==================================
    // Static Fields
    //==================================
    private static final String PREF_IS_TABBED = "pref_is_tabbed";
    private static final String PREF_LAST_POSITION = "lastposition";
    //==================================
    public static final String SHARED_PREFERENCES_BASENAME = "com.vipercn.viper4android_v2";
    public static final String ACTION_UPDATE_PREFERENCES = "com.vipercn.viper4android_v2.UPDATE";
    public static final String ACTION_SHOW_NOTIFY = "com.vipercn.viper4android_v2.SHOWNOTIFY";
    public static final String ACTION_CANCEL_NOTIFY = "com.vipercn.viper4android_v2.CANCELNOTIFY";
    public static final int NOTIFY_FOREGROUND_ID = 1;
    private static final int REQUEST_PERMISSION = 4;
    private static final int PERMISSION_SETUPUI = 0;
    private static final int PERMISSION_SAVE = 1;
    private static final int PERMISSION_LOAD = 2;

    public static final String HEADSET = "headset";
    public static final String SPEAKER = "speaker";
    public static final String BLUETOOTH = "bluetooth";
    public static final String USB = "usb";



    //==================================
    private static String[] mEntries;
    private static List<HashMap<String, String>> mTitles;

    //==================================
    // Drawer
    //==================================
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    //==================================
    // Fields
    //==================================
    private SharedPreferences mPreferences;
    private boolean mIsTabbed = true;
    private CharSequence mTitle;

    private ViPER4AndroidService mAudioServiceInstance;

    private boolean checkFirstRun()
    {
        PackageManager packageMgr = getPackageManager();
        PackageInfo packageInfo;
        String mVersion;
        try {
            packageInfo = packageMgr.getPackageInfo(getPackageName(), 0);
            mVersion = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return false;
        }

        SharedPreferences prefSettings = getSharedPreferences(SHARED_PREFERENCES_BASENAME + ".settings", 0);
        String mLastVersion = prefSettings.getString("viper4android.settings.lastversion", null);
        return mLastVersion == null || mLastVersion.length() == 0 || !mLastVersion.equalsIgnoreCase(mVersion);
    }

    private boolean checkDDCDBVer() {
        PackageManager packageMgr = getPackageManager();
        PackageInfo packageInfo;
        String mVersion;
        try {
            packageInfo = packageMgr.getPackageInfo(getPackageName(), 0);
            mVersion = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return false;
        }

        SharedPreferences prefSettings = getSharedPreferences(SHARED_PREFERENCES_BASENAME + ".settings", 0);
        String mDBVersion = prefSettings.getString("viper4android.settings.ddc_db_compatible", null);
        return mDBVersion == null || mDBVersion.equals("") || !mDBVersion.equalsIgnoreCase(mVersion);
    }

    private void setFirstRun() {
        PackageManager packageMgr = getPackageManager();
        PackageInfo packageInfo;
        String mVersion;
        try {
            packageInfo = packageMgr.getPackageInfo(getPackageName(), 0);
            mVersion = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return;
        }

        SharedPreferences prefSettings = getSharedPreferences(
                SHARED_PREFERENCES_BASENAME + ".settings", 0);
        Editor editSettings = prefSettings.edit();
        if (editSettings != null) {
            editSettings.putString("viper4android.settings.lastversion", mVersion);
            editSettings.commit();
        }
    }

    private void setDDCDBVer()
    {
        PackageManager packageMgr = getPackageManager();
        PackageInfo packageInfo;
        String mVersion;
        try
        {
            packageInfo = packageMgr.getPackageInfo(getPackageName(), 0);
            mVersion = packageInfo.versionName;
        }
        catch (NameNotFoundException e)
        {
            return;
        }

        SharedPreferences prefSettings = getSharedPreferences(SHARED_PREFERENCES_BASENAME + ".settings", 0);
        Editor editSettings = prefSettings.edit();
        if (editSettings != null)
        {
            editSettings.putString("viper4android.settings.ddc_db_compatible", mVersion);
            editSettings.commit();
        }
    }

    private boolean checkSoftwareActive()
    {
        SharedPreferences prefSettings = getSharedPreferences(SHARED_PREFERENCES_BASENAME + ".settings", 0);
        boolean mActived = prefSettings.getBoolean("viper4android.settings.onlineactive", false);
        return !mActived;
    }

    private void setSoftwareActive()
    {
        SharedPreferences prefSettings = getSharedPreferences(SHARED_PREFERENCES_BASENAME + ".settings", 0);
        Editor editSettings = prefSettings.edit();
        if (editSettings != null)
        {
            editSettings.putBoolean("viper4android.settings.onlineactive", true);
            editSettings.commit();
        }
    }

    private boolean submitInformation() {
        String mCode = "";
        Random rndMachine = new Random();
        for (int i = 0; i < 8; i++) {
            int oneByte = rndMachine.nextInt(256);
            String byteHexString = Integer.toHexString(oneByte);
            if (byteHexString.length() < 2) {
            	byteHexString = "0" + byteHexString;
            }
            mCode = mCode + byteHexString;
        }
        mCode = mCode + "-";
        for (int i = 0; i < 4; i++) {
        	int oneByte = rndMachine.nextInt(256);
        	String byteHexString = Integer.toHexString(oneByte);
            if (byteHexString.length() < 2) {
            	byteHexString = "0" + byteHexString;
            }
            mCode = mCode + byteHexString;
        }
        mCode = mCode + "-" + Build.VERSION.SDK_INT;

        String mURL = "http://vipersaudio.com/stat/v4a_stat.php?code=" + mCode + "&ver=viper4android-fx";
        try {
            /*HttpGet httpRequest = new HttpGet(mURL);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpRequest);*/
            URL url = new URL(mURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int status = connection.getResponseCode();
            connection.disconnect();
            return status == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            Log.i("ViPER4Android", "Submit failed, error = " + e.getMessage());
            return false;
        }
    }

    private void processDriverCheck()
    {
        boolean isDriverUsable;
        String mDriverVersion = "Unknown";
        Utils.AudioEffectUtils aeuUtils = new Utils().new AudioEffectUtils();
        if (!aeuUtils.isViPER4AndroidEngineFound())
        {
            isDriverUsable = false;
        }
        else
        {
            int[] iaDrvVer = aeuUtils.getViper4AndroidEngineVersion();
            mDriverVersion = String.format("%s.%s.%s.%s", iaDrvVer[0], iaDrvVer[1], iaDrvVer[2], iaDrvVer[3]);
            isDriverUsable = isDriverCompatible(mDriverVersion);
        }

        if (!isDriverUsable)
        {
            Log.i("ViPER4Android", "Android audio effect engine reports the v4a driver is not usable");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("SCRUM");
            builder.setMessage("Android audio effect engine reports the V4A driver is not usable.\nVersion: " + mDriverVersion);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Message message = new Message();
                    message.what = 0xA00A;
                    message.obj = ViPER4Android.this;
                    mDriverHandler.sendMessage(message);
                }
            });
            builder.setNegativeButton("Ignore", null);
            builder.show();

            /*Snackbar.make(mFragmentContainerView, "Android audio effect engine reports the v4a driver is not usable. Click here for options.", Snackbar.LENGTH_LONG)
                    .setAction("", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Message message = new Message();
                            message.what = 0xA00A;
                            message.obj = v.getContext();
                            mDriverHandler.sendMessage(message);
                        }
                    }).show();*/
        }
        /*Snackbar.make(mFragmentContainerView, isDriverUsable? "Driver version: " + mDriverVersion : "Error: Driver not usable", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();*/
    }

    public static boolean isDriverCompatible(String szDrvVersion)
    {
        List<String> lstCompatibleList = new ArrayList<>();
        // TODO: <DO NOT REMOVE> add compatible driver version to lstCompatibleList

        if (lstCompatibleList.contains(szDrvVersion))
        {
            lstCompatibleList.clear();
            return true;
        }
        else
        {
            lstCompatibleList.clear();
            // Since we cant use getPackageManager in static method, we need to type the current version here
            // TODO: <DO NOT REMOVE> please make sure this string equals to current apk's version
            return szDrvVersion.equals("2.4.0.1");
        }
    }

    private static boolean cpuHasQualitySelection()
    {
        Utils.CpuInfo mCPUInfo = new Utils.CpuInfo();
        return mCPUInfo.hasNEON();
    }

    private static String determineCPUWithDriver(String mQual)
    {
        String mDriverFile = "libv4a_fx_";

        if (Build.VERSION.SDK_INT >= 18)
            mDriverFile += "jb_";
        else
            mDriverFile += "ics_";

        if (Build.CPU_ABI.toLowerCase().contains("x86"))
        {
            // x86 architecture
            mDriverFile += "X86.so";
            Log.i("ViPER4Android", "Driver selection = " + mDriverFile);
            return mDriverFile;
        }

        Utils.CpuInfo mCPUInfo = new Utils.CpuInfo();
        if (mCPUInfo.hasNEON())
        {
            if (mQual == null)
                mDriverFile += "NEON";
            else if (mQual.equals(""))
                mDriverFile += "NEON";
            else if (mQual.equalsIgnoreCase("sq"))
                mDriverFile += "NEON_SQ";
            else if (mQual.equalsIgnoreCase("hq"))
                mDriverFile += "NEON_HQ";
            else
                mDriverFile += "NEON";
        }
        else if (mCPUInfo.hasVFP())
            mDriverFile += "VFP";
        else
            mDriverFile += "NOVFP";

        mDriverFile += ".so";
        Log.i("ViPER4Android", "Driver selection = " + mDriverFile);
        return mDriverFile;
    }

    // Driver install handler
    public static final Handler mDriverHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 0xA00A) {
                    if (msg.obj == null)
                    {
                        super.handleMessage(msg);
                        return;
                    }
                    final Context ctxInstance = (Context) msg.obj;
                    AlertDialog.Builder mUpdateDrv = new AlertDialog.Builder(ctxInstance);
                    mUpdateDrv.setTitle("ViPER4Android");
                    mUpdateDrv.setMessage(ctxInstance.getResources().getString(R.string.text_drvvernotmatch));
                    mUpdateDrv.setPositiveButton(ctxInstance.getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // Install/Update driver
                                    String mDriverFileName = determineCPUWithDriver("");

                                    if (!Utils.isBusyBoxInstalled(ctxInstance))
                                    {
                                        AlertDialog.Builder mResult = new AlertDialog.Builder(ctxInstance);
                                        mResult.setTitle("ViPER4Android");
                                        mResult.setMessage(ctxInstance.getResources().getString(R.string.text_drvinst_busybox_not_installed));
                                        mResult.setNegativeButton(ctxInstance.getResources().getString(R.string.text_ok), null);
                                        mResult.show();
                                    }
                                    else
                                    {
                                        int drvInstResult = Utils.installDrv_FX(ctxInstance, mDriverFileName);
                                        if (drvInstResult == 0)
                                        {
                                            Utils.proceedBuildProp(ctxInstance);
                                            AlertDialog.Builder mResult = new AlertDialog.Builder(ctxInstance);
                                            mResult.setTitle("ViPER4Android");
                                            mResult.setMessage(ctxInstance.getResources().getString(R.string.text_drvinst_ok));
                                            mResult.setNegativeButton(ctxInstance.getResources().getString(R.string.text_ok), null);
                                            mResult.show();
                                        }
                                        else
                                        {
                                            AlertDialog.Builder mResult = new AlertDialog.Builder(ctxInstance);
                                            mResult.setTitle("ViPER4Android");
                                            switch (drvInstResult)
                                            {
                                                case 1:
                                                    mResult.setMessage(ctxInstance.getResources().getString(R.string.text_drvinst_acquireroot));
                                                    break;
                                                case 2:
                                                    mResult.setMessage(ctxInstance.getResources().getString(R.string.text_drvinst_sdnotmounted));
                                                    break;
                                                case 3:
                                                    mResult.setMessage(ctxInstance.getResources().getString(R.string.text_drvinst_dataioerr));
                                                    break;
                                                case 4:
                                                    mResult.setMessage(ctxInstance.getResources().getString(R.string.text_drvinst_cfg_unsup));
                                                    break;
                                                case 5:
                                                case 6:
                                                default:
                                                    mResult.setMessage(ctxInstance.getResources().getString(R.string.text_drvinst_failed));
                                                    break;
                                            }
                                            mResult.setNegativeButton(ctxInstance.getResources().getString(R.string.text_ok), null);
                                            mResult.show();
                                        }
                                    }
                                }
                            });
                    mUpdateDrv.setNegativeButton(ctxInstance.getResources().getString(R.string.text_no), new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                }
                            });
                    mUpdateDrv.show();
                }
                super.handleMessage(msg);
            } catch (Exception e) {
                Toast.makeText((Context) msg.obj, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load jni first
        boolean jniLoaded = V4AJniInterface.CheckLibrary();
        Log.i("ViPER4Android", "Jni library status = " + jniLoaded);

        // Welcome window
        if (checkFirstRun()) {
            // TODO: Welcome window
        }

        // Prepare ViPER-DDC database
        if (checkDDCDBVer())
        	if (DDCDatabase.initializeDatabase(this))
        		setDDCDBVer();

        // We should start the background service first
        Log.i("ViPER4Android", "Starting service, reason = ViPER4Android::onCreate");
        Intent serviceIntent = new Intent(this, ViPER4AndroidService.class);
        startService(serviceIntent);

        // Setup ui
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Start active thread
        // TODO: online check
        /*Thread activeThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                if (checkSoftwareActive())
                    if (submitInformation())
                        setSoftwareActive();
            }
        });
        activeThread.start();*/

        // Load UI
        setupUI();

        // Permission for save/load of profiles, etc
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_SETUPUI);
        else
            setUpDrawer();

        // Start post init thread
        Thread postInitThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                // Init environment
                Log.i("ViPER4Android", "Init environment");
                StaticEnvironment.initEnvironment();

                // Driver check loop
                Log.i("ViPER4Android", "Check driver");
                processDriverCheck();
            }
        });
        postInitThread.start();
    }

    private void setupUI()
    {
        setContentView(R.layout.activity_main);
        mEntries = getEntries();

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mFragmentContainerView = findViewById(R.id.fragment_container);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if(ab != null)
        {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
    }

    private void setUpDrawer()
    {
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        if (navigationView == null)
            return;

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                if (menuItem.getItemId() == 5)
                {
                    showAbout();
                    return true;
                }
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                setFragment(menuItem.getItemId());
                return true;
            }
        });
        navigationView.getMenu().add(1, 0, Menu.NONE, R.string.headset_title).setIcon(R.drawable.headphones);
        navigationView.getMenu().add(1, 1, Menu.NONE, R.string.speaker_title).setIcon(R.drawable.speaker);
        navigationView.getMenu().add(1, 2, Menu.NONE, R.string.bluetooth_title).setIcon(R.drawable.bluetooth);
        navigationView.getMenu().add(1, 3, Menu.NONE, R.string.usb_title).setIcon(R.drawable.usb);
        navigationView.getMenu().add(2, 4, Menu.NONE, R.string.settings_title).setIcon(R.drawable.settings);
        navigationView.getMenu().add(2, 5, Menu.NONE, R.string.about_title).setIcon(R.drawable.help);
        navigationView.getMenu().setGroupCheckable(1, true, true);
        navigationView.getMenu().setGroupCheckable(2, true, true);

        int last = mPreferences.getInt(PREF_LAST_POSITION, 0);
        navigationView.setCheckedItem(last);
        setFragment(last);
    }

    private void showAbout()
    {
        PackageManager packageMgr = getPackageManager();
        PackageInfo packageInfo;
        String mVersion;
        try
        {
            packageInfo = packageMgr.getPackageInfo(getPackageName(), 0);
            mVersion = packageInfo.versionName;
        }
        catch (NameNotFoundException e)
        {
            mVersion = "N/A";
        }
        String mAbout = getResources().getString(R.string.about_text);
        mAbout = String.format(mAbout, mVersion) + "\n";
        mAbout = mAbout + getResources().getString(R.string.text_help_content);

        AlertDialog.Builder mHelp = new AlertDialog.Builder(this);
        mHelp.setTitle(getResources().getString(R.string.about_title));
        mHelp.setMessage(mAbout);
        mHelp.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
            }
        });
        mHelp.setNegativeButton(getResources().getString(R.string.text_view_forum), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                Uri uri = Uri.parse(getResources().getString(R.string.text_forum_link));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        mHelp.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    int last = mPreferences.getInt(PREF_LAST_POSITION, 0);
                    setFragment(last);
                }
                break;
            case PERMISSION_SETUPUI:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    setUpDrawer();
                else
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle(getResources().getString(R.string.about_title));
                    dialog.setMessage(R.string.text_permission_external);
                    dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            arg0.dismiss();
                            setUpDrawer();
                        }
                    });
                    dialog.setNegativeButton(R.string.text_permission_grant, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            ActivityCompat.requestPermissions(ViPER4Android.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_SETUPUI);
                        }
                    });
                    dialog.show();
                }
                break;
        }
    }

    private void setFragment(int position)
    {
        PreferenceFragmentCompat fragment = null;
        int titleRes = R.string.pref_viperddc_title;
        if(position < 4)
            fragment = PlaceholderFragment.newInstance(position, null);
        switch(position)
        {
            case 0:
                titleRes = R.string.headset_title;
                break;
            case 1:
                titleRes = R.string.speaker_title;
                break;
            case 2:
                titleRes = R.string.bluetooth_title;
                break;
            case 3:
                titleRes = R.string.usb_title;
                break;
            case 4:
                fragment = PlaceholderFragment.newInstance(position, "general");
                titleRes = R.string.settings_title;
                break;
        }
        setTitle(titleRes);

        if(fragment == null)
            return;

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                fragmentTransaction.commit();
            }
        }, 50);

        if(position < 4)
            mPreferences.edit().putInt(PREF_LAST_POSITION, position).apply();
    }

    @Override
    public void onResume()
    {
        Log.i("ViPER4Android", "Main activity onResume()");
        super.onResume();

        ServiceConnection connection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder)
            {
                Log.i("ViPER4Android", "ViPER4Android service connected");
                mAudioServiceInstance = ((ViPER4AndroidService.LocalBinder)binder).getService();
                Log.i("ViPER4Android", "Unbinding service ...");
                unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                Log.e("ViPER4Android", "ViPER4Android service disconnected");
            }
        };

        Log.i("ViPER4Android", "onResume(), Binding service ...");
        Intent serviceIntent = new Intent(this, ViPER4AndroidService.class);
        bindService(serviceIntent, connection, Context.BIND_IMPORTANT);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.fragment_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_BASENAME + ".settings", MODE_PRIVATE);

        /* Force enable menu check */
        MenuItem forceEnable = menu.findItem(R.id.force_enable);
        if(forceEnable != null)
            forceEnable.setChecked(preferences.getBoolean("viper4android.global.forceenable.enable", false));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
    	if (item == null) return true;
        final SharedPreferences prefSettings = getSharedPreferences(SHARED_PREFERENCES_BASENAME + ".settings", MODE_PRIVATE);

        int choice = item.getItemId();
        switch (choice)
        {
            case R.id.force_enable:
                final boolean[] cancel = { false };
                if(!item.isChecked())
                {
                    AlertDialog.Builder mDlgWarning = new AlertDialog.Builder(this);
                    mDlgWarning.setTitle("ViPER4Android");
                    mDlgWarning.setMessage(getResources().getString(R.string.pref_force_enable_warn));
                    mDlgWarning.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if(which == DialogInterface.BUTTON_POSITIVE)
                            {
                                item.setChecked(true);
                                prefSettings.edit().putBoolean("viper4android.global.forceenable.enable", true).commit();
                            }
                        }
                    });
                    mDlgWarning.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if(which == DialogInterface.BUTTON_NEGATIVE)
                            {
                                cancel[0] = true;
                            }
                        }
                    });
                    mDlgWarning.show();
                }
                if(!cancel[0]  && item.isChecked())
                {
                    item.setChecked(!item.isChecked());
                    prefSettings.edit().putBoolean("viper4android.global.forceenable.enable", item.isChecked()).commit();
                }
                return true;

            case R.id.drvstatus:
            {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
                if (mAudioServiceInstance == null)
                    mDialog.setMessage(R.string.text_service_error);
                else
                {
                    mAudioServiceInstance.startStatusUpdating();
                    SystemClock.sleep(100);
                    mAudioServiceInstance.stopStatusUpdating();

                    String mDrvNEONEnabled = getResources().getString(R.string.text_yes);
                    if (!mAudioServiceInstance.getDriverNEON())
                    {
                        mDrvNEONEnabled = getResources().getString(R.string.text_no);
                    }
                    String mDrvEnabled = getResources().getString(R.string.text_yes);
                    if (!mAudioServiceInstance.getDriverEnabled())
                    {
                        mDrvEnabled = getResources().getString(R.string.text_no);
                    }
                    String mDrvUsable = getResources().getString(R.string.text_normal);
                    if (!mAudioServiceInstance.getDriverCanWork())
                    {
                        mDrvUsable = getResources().getString(R.string.text_abnormal);
                    }
                    String mDrvSupportFmt = getResources().getString(R.string.text_supported);
                    if (!mAudioServiceInstance.getDriverSupportFormat())
                    {
                        mDrvSupportFmt = getResources().getString(R.string.text_unsupported);
                    }
                    String mDrvProcess = getResources().getString(R.string.text_yes);
                    if (!mAudioServiceInstance.getDriverProcess())
                    {
                        mDrvProcess = getResources().getString(R.string.text_no);
                    }

                    Utils.AudioEffectUtils aeuUtils = new Utils().new AudioEffectUtils();
                    int[] iaDrvVer = aeuUtils.getViper4AndroidEngineVersion();
                    String mDriverVersion = iaDrvVer[0] + "." + iaDrvVer[1] + "." + iaDrvVer[2] + "." + iaDrvVer[3];

                    String mDrvStatus;
                    mDrvStatus = getResources().getString(R.string.text_drv_status_view);
                    mDrvStatus = String.format(mDrvStatus, mDriverVersion, mDrvNEONEnabled, mDrvEnabled, mDrvUsable, mDrvSupportFmt, mDrvProcess, mAudioServiceInstance.getDriverSamplingRate());

                    mDialog.setMessage(mDrvStatus);
                }
                mDialog.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                mDialog.setCancelable(true);
                mDialog.show();
                return true;
            }

            case R.id.compatible:
            {
                String mCompatibleMode = prefSettings.getString("viper4android.settings.compatiblemode", "global");
                int mSelectIndex = mCompatibleMode.equals("global") ? 0 : 1;
                Dialog selectDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.text_commode)
                        .setSingleChoiceItems(R.array.compatible_mode, mSelectIndex, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        prefSettings.edit().putString("viper4android.settings.compatiblemode", which == 0 ? "global" : "local").commit();
                        dialog.dismiss();
                    }
                }).setCancelable(true).create();
                selectDialog.show();
                return true;
            }

            case R.id.notify: {
                boolean enableNotify = prefSettings.getBoolean("viper4android.settings.show_notify_icon", false);
                enableNotify = !enableNotify;
                if (enableNotify) {
                    item.setTitle(getResources().getString(R.string.text_hidetrayicon));
                } else {
                    item.setTitle(getResources().getString(R.string.text_showtrayicon));
                }
                Editor edit = prefSettings.edit();
                edit.putBoolean("viper4android.settings.show_notify_icon", enableNotify);
                edit.commit();
                // Tell background service to deal with the notification icon
                if (enableNotify) {
                    sendBroadcast(new Intent(ACTION_SHOW_NOTIFY));
                } else {
                    sendBroadcast(new Intent(ACTION_CANCEL_NOTIFY));
                }
                return true;
            }

            case R.id.lockeffect: {
                String mLockedEffect = prefSettings.getString("viper4android.settings.lock_effect", "none").toLowerCase(Locale.US);
                int mLockIndex;

                switch (mLockedEffect)
                {
                    case "none": mLockIndex = 0; break;
                    case HEADSET: mLockIndex = 1; break;
                    case SPEAKER: mLockIndex = 2; break;
                    case BLUETOOTH: mLockIndex = 3; break;
                    case USB: mLockIndex = 4; break;
                    default: mLockIndex = 5; break;
                }

                String[] modeList = {
                    getResources().getString(R.string.text_disabled),
                    getResources().getString(R.string.text_headset),
                    getResources().getString(R.string.text_speaker),
                    getResources().getString(R.string.text_bluetooth),
                    getResources().getString(R.string.text_usb)
                };

                Dialog selectDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.text_lockeffect)
                        .setIcon(R.drawable.icon)
                        .setSingleChoiceItems(modeList, mLockIndex,
                        new DialogInterface.OnClickListener() {
                    @SuppressLint("CommitPrefEdits")
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefSettings = getSharedPreferences(SHARED_PREFERENCES_BASENAME + ".settings", MODE_PRIVATE);
                        Editor edit = prefSettings.edit();
                        switch (which) {
                            case 0:
                                edit.putString("viper4android.settings.lock_effect", "none");
                                break;
                            case 1:
                                edit.putString("viper4android.settings.lock_effect", HEADSET);
                                break;
                            case 2:
                                edit.putString("viper4android.settings.lock_effect", SPEAKER);
                                break;
                            case 3:
                                edit.putString("viper4android.settings.lock_effect", BLUETOOTH);
                                break;
                            case 4:
                                edit.putString("viper4android.settings.lock_effect", USB);
                                break;
                        }
                        edit.commit();

                        // Tell background service to change the mode
                        sendBroadcast(new Intent(ACTION_UPDATE_PREFERENCES));
                        dialog.dismiss();
                    }
                }).setCancelable(false).create();
                selectDialog.show();
                return true;
            }

            default:
                return false;
        }
    }

    //==================================
    // Methods
    //==================================

    public void saveProfileDialog()
    {
        // We first list existing profiles
        File profileDir = new File(StaticEnvironment.getV4aProfilePath());
        profileDir.mkdirs();

        Log.i("ViPER4Android", "Saving preset to " + profileDir.getAbsolutePath());

        // The first entry is "New profile", so we offset
        File[] profiles = profileDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        final String[] names = new String[profiles != null ? profiles.length + 1 : 1];
        names[0] = getString(R.string.text_newfxprofile);
        if (profiles != null) {
            for (int i = 0; i < profiles.length; i++) {
                names[i + 1] = profiles[i].getName();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_savefxprofile)
                .setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // New profile, we ask for the name
                            AlertDialog.Builder inputBuilder =
                                    new AlertDialog.Builder(ViPER4Android.this);

                            inputBuilder.setTitle(R.string.text_newfxprofile);

                            // Set an EditText view to get user input
                            final EditText input = new EditText(ViPER4Android.this);
                            inputBuilder.setView(input);

                            inputBuilder.setPositiveButton(
                            		getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString();
                                    saveProfile(value);
                                }
                            });
                            inputBuilder.setNegativeButton(
                            		getResources().getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });

                            inputBuilder.show();
                        } else {
                        	// Overwrite exist profile?
                        	final String profileName = names[which];
                            AlertDialog.Builder mOverwriteProfile = new AlertDialog.Builder(ViPER4Android.this);
                            mOverwriteProfile.setTitle("ViPER4Android");
                            mOverwriteProfile.setMessage(getResources().getString(R.string.text_profilesaved_overwrite));
                            mOverwriteProfile.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									saveProfile(profileName);
								}
                            }).setNegativeButton(getResources().getString(R.string.text_cancel), null);
                            mOverwriteProfile.show();
                        }
                    }
                });
        Dialog dlg = builder.create();
        dlg.show();
    }

    public void loadProfileDialog()
    {
        File profileDir = new File(StaticEnvironment.getV4aProfilePath());
        profileDir.mkdirs();

        /* Scan version 1 profiles */
        final ArrayList<String> profilenames = Utils.getProfileList(StaticEnvironment.getV4aProfilePath());

        /* Scan version 2 profiles */
        File[] profiles = profileDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (profiles != null)
            for (File profile : profiles)
                profilenames.add(profile.getName());

        /* Write all profiles to a new array */
        final String[] names = new String[profilenames.size()];
        for (int i = 0; i < profilenames.size(); i++) {
        	names[i] = profilenames.get(i);
        }

        /* Show profile list box */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_loadfxprofile)
                .setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        loadProfile(names[which]);
                    }
                });
        builder.create().show();
    }

    private void saveProfile(String name) {
        final String spDir = getApplicationInfo().dataDir + "/shared_prefs/";

        // Copy the SharedPreference to our output directory
        File profileDir = new File(StaticEnvironment.getV4aProfilePath() + "/" + name);
        profileDir.mkdirs();

        Log.i("ViPER4Android", "Saving profile to " + profileDir.getAbsolutePath());

        final String packageName = SHARED_PREFERENCES_BASENAME + ".";

        try {
            copy(new File(spDir + packageName + BLUETOOTH + ".xml"), new File(profileDir, packageName + BLUETOOTH + ".xml"));
            copy(new File(spDir + packageName + HEADSET + ".xml"), new File(profileDir, packageName + HEADSET + ".xml"));
            copy(new File(spDir + packageName + SPEAKER + ".xml"), new File(profileDir, packageName + SPEAKER + ".xml"));
            copy(new File(spDir + packageName + USB + ".xml"), new File(profileDir, packageName + USB + ".xml"));
        } catch (IOException e) {
            Log.e("ViPER4Android", "Cannot save preset: " + e.getMessage());
        }
    }

    private void loadProfile(String name)
    {
        // Copy the SharedPreference to our local directory
        File profileDir = new File(StaticEnvironment.getV4aProfilePath() + "/" + name);
        if (!profileDir.exists())
        {
        	/* If the profile directory does not exist, we load it as version 1 profile */
            final String[] audioDevices = new String[4];
            audioDevices[0] = getResources().getString(R.string.text_headset);
            audioDevices[1] = getResources().getString(R.string.text_speaker);
            audioDevices[2] = getResources().getString(R.string.text_bluetooth);
            audioDevices[3] = getResources().getString(R.string.text_usb);
            /* Which mode you want to apply the profile? */
            final String profilename = name;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.text_profileload_tip).setItems(audioDevices, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    String mode;
                    switch (which)
                    {
                        case 0: mode = HEADSET; break;
                        case 1: mode = SPEAKER; break;
                        case 2: mode =  BLUETOOTH; break;
                        case 3: mode = USB; break;
                        default:
                            Snackbar.make(getFragmentContainerView(), "Can't load selected profile", Snackbar.LENGTH_LONG).show();
                            return;
                    }
                    String applyPreference = String.format(Locale.US,  "%s.%s", SHARED_PREFERENCES_BASENAME, mode);
                    Utils.loadProfileV1(profilename, StaticEnvironment.getV4aProfilePath(), applyPreference, ViPER4Android.this);

                    // Reload preferences
                    startActivity(new Intent(ViPER4Android.this, ViPER4Android.class));
                    finish();

                }
            }).setNegativeButton(getResources().getString(R.string.text_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Log.i("ViPER4Android", "Load profile canceled");
                }
            });
            builder.create().show();
            return;
        }

        final String packageName = SHARED_PREFERENCES_BASENAME + ".";
        final String spDir = getApplicationInfo().dataDir + "/shared_prefs/";

        try
        {
            copy(new File(profileDir, packageName + "bluetooth.xml"), new File(spDir + packageName + "bluetooth.xml"));
            copy(new File(profileDir, packageName + "headset.xml"), new File(spDir + packageName + "headset.xml"));
            copy(new File(profileDir, packageName + "speaker.xml"), new File(spDir + packageName + "speaker.xml"));
            copy(new File(profileDir, packageName + "usb.xml"), new File(spDir + packageName + "usb.xml"));
        }
        catch (IOException e)
        {
            Log.e("ViPER4Android", "Cannot load preset");
        }

        // Update effects
        sendBroadcast(new Intent(ViPER4Android.ACTION_UPDATE_PREFERENCES));

        // Reload preferences
        startActivity(new Intent(this, ViPER4Android.class));
        finish();
    }

    private static void copy(File src, File dst) throws IOException
    {
        Log.i("ViPER4Android", "Copying " + src.getAbsolutePath() + " to " + dst.getAbsolutePath());
        try (FileInputStream inStream = new FileInputStream(src); FileOutputStream outStream = new FileOutputStream(dst))
        {
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        catch (IOException ex)
        {
            Log.i("ViPER4Android", "Failed copying " + src.getAbsolutePath() + " to " + dst.getAbsolutePath());
            throw ex;
        }
    }

    /**
     * List of available settings (headset, speaker, bluetooth & usb).
     * @return String[] containing titles
     */
    private String[] getEntries()
    {
        ArrayList<String> entryString = new ArrayList<>();
        entryString.add(HEADSET);
        entryString.add(SPEAKER);
        entryString.add(BLUETOOTH);
        entryString.add(USB);
        return entryString.toArray(new String[entryString.size()]);
    }

    public View getFragmentContainerView()
    {
        return mFragmentContainerView;
    }

    //==================================
    // Internal Classes
    //==================================

    /**
     * Loads our Fragments.
     */
    public static class PlaceholderFragment extends Fragment
    {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PreferenceFragmentCompat newInstance(int fragmentId, String prefName)
        {
            final MainDSPScreen dspFragment = new MainDSPScreen();
            Bundle bundle = new Bundle();
            bundle.putInt("id", fragmentId);
            if (fragmentId < 4)
                bundle.putString("config", mEntries[fragmentId]);
            else
                bundle.putString("config", prefName);

            dspFragment.setArguments(bundle);
            return dspFragment;
        }

        public PlaceholderFragment()
        {
            // intentionally left blank
        }
    }

    public ViPER4AndroidService getAudioService()
    {
        return mAudioServiceInstance;
    }

    private void checkPermission(int id, String permission)
    {
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] { permission }, id);
    }
}
