package com.vipercn.viper4android_v3.activity;

public class DDC
{
    private String mID;
    private String mName;
    private String mBrand;

    public DDC(String id, String brand, String name)
    {
        mID = id;
        mName = name;
        mBrand = brand;
    }

    public String getID()
    {
        return mID;
    }

    public String getName()
    {
        return mName;
    }

    public String getBrand()
    {
        return mBrand;
    }
}
