
package com.vipercn.viper4android_v3.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DDCDatabase
{
	@SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean initializeDatabase(Context ctx)
    {
        String szLocalDatabasePathName = Utils.getBasePath(ctx);
        if (!szLocalDatabasePathName.endsWith("/"))
            szLocalDatabasePathName += "/";
        szLocalDatabasePathName += "ViPERDDC.db";

        File fDBFile = new File(szLocalDatabasePathName);
        if (fDBFile.exists())
            fDBFile.delete();

        return Utils.copyAssetsToLocal(ctx, "ViPERDDC.db", "ViPERDDC.db");
    }

	public static ArrayList<DDC> queryManufacturerAndModel(Context ctx)
    {
        String szLocalDatabasePathName = Utils.getBasePath(ctx);
        if (!szLocalDatabasePathName.endsWith("/"))
            szLocalDatabasePathName += "/";
        szLocalDatabasePathName += "ViPERDDC.db";

        ArrayList<DDC> mManufacturerAndModel = new ArrayList<>();
        SQLiteDatabase dbViPERDDC;
        Cursor mDDCData;
        try
        {
            String[] szColumns = new String[] { "ID", "Company", "Model" };
            dbViPERDDC = SQLiteDatabase.openDatabase(szLocalDatabasePathName, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            mDDCData = dbViPERDDC.query("DDCData", szColumns, null, null, null, null, "Company COLLATE NOCASE");
            if ((mDDCData != null) && mDDCData.moveToFirst())
            {
                do
                {
                    if (mDDCData.isNull(0) || mDDCData.isNull(1) || mDDCData.isNull(2))
                        continue;
                    mManufacturerAndModel.add(new DDC(mDDCData.getString(0).trim(), mDDCData.getString(1).trim(), mDDCData.getString(2).trim()));
                } while (mDDCData.moveToNext());
                mDDCData.close();
            }
            dbViPERDDC.close();
            return mManufacturerAndModel;
        }
        catch (SQLiteException ex)
        {
            Log.i("ViPER4Android", "queryManufacturerAndModel[ViPER-DDC] :" + ex.getMessage());
            return mManufacturerAndModel;
        }
    }

	public static String queryDDCBlock(String szKeyID, Context ctx)
    {
        if (szKeyID == null)
            return "";
        if (szKeyID.isEmpty())
            return "";

        if (szKeyID.startsWith("FILE:"))
        {
            String szDDCFilePath = StaticEnvironment.getV4aCustomDDCPath() + szKeyID.substring(5);
            File mDDCFileHandle = new File(szDDCFilePath);

            if (!mDDCFileHandle.exists())
                return "";
            if (!mDDCFileHandle.canRead())
                return "";

            String szSR44100Coeffs = "";
            String szSR48000Coeffs = "";

            FileReader frDDCReader = null;
            BufferedReader brDDCBufferedReader = null;
            try
            {
                frDDCReader = new FileReader(mDDCFileHandle);
                brDDCBufferedReader = new BufferedReader(frDDCReader);
                while (true)
                {
                    String szLine = brDDCBufferedReader.readLine();
                    if (szLine == null)
                        break;
                    szLine = szLine.trim();
                    if (szLine.startsWith("SR_44100:"))
                        szSR44100Coeffs = szLine.substring(9);
                    else if (szLine.startsWith("SR_48000:"))
                        szSR48000Coeffs = szLine.substring(9);
                }
                brDDCBufferedReader.close();
                frDDCReader.close();
                brDDCBufferedReader = null;
                frDDCReader = null;
            }
            catch (IOException e1)
            {
                try
                {
                    if (brDDCBufferedReader != null)
                        brDDCBufferedReader.close();
                    if (frDDCReader != null)
                        frDDCReader.close();
                    return "";
                }
                catch (IOException e2)
                {
                    return "";
                }
            }

            if (szSR44100Coeffs.isEmpty())
                return "";
            if (szSR48000Coeffs.isEmpty())
                return "";
            return szSR44100Coeffs + "," + szSR48000Coeffs;
        }

        String szLocalDatabasePathName = Utils.getBasePath(ctx);
        szLocalDatabasePathName += (szLocalDatabasePathName.endsWith("/") ? "" : "/") + "ViPERDDC.db";

        SQLiteDatabase dbViPERDDC;
        Cursor mDDCData;
        try
        {
            String[] szColumns = new String[] { "ID", "SR_44100_Coeffs", "SR_48000_Coeffs" };
            String[] szQueryArgs = new String[] { szKeyID };
            dbViPERDDC = SQLiteDatabase.openDatabase(szLocalDatabasePathName, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            mDDCData = dbViPERDDC.query("DDCData", szColumns, "ID=?", szQueryArgs, null, null, null);
            if (mDDCData == null || mDDCData.getCount() != 1)
            {
                dbViPERDDC.close();
                return "";
            }
            mDDCData.moveToFirst();
            String szC44100 = mDDCData.getString(1).trim();
            String szC48000 = mDDCData.getString(2).trim();
            mDDCData.close();
            dbViPERDDC.close();
            if (szC44100.length() <= 0 || szC48000.length() <= 0)
                return "";
            return szC44100 + "," + szC48000;
        }
        catch (Exception ex)
        {
            Log.i("ViPER4Android", "queryDDCBlock[ViPER-DDC] :" + ex.getMessage());
            return "";
        }
    }

	public static float[] blockToFloatArray(String szDDCBlock)
    {
        if ((szDDCBlock == null) || (szDDCBlock.length() < 3) || (!szDDCBlock.contains(",")))
            return null;

        try
        {
            String[] szCoeffs = szDDCBlock.split(",");
            float[] faCoeffsArray = new float[szCoeffs.length];
            for (int i = 0; i < szCoeffs.length; i++)
                faCoeffsArray[i] = Float.valueOf(szCoeffs[i]);
            return faCoeffsArray;
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
