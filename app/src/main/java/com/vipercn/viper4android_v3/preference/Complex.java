package com.vipercn.viper4android_v3.preference;

class Complex
{
    private double mReal, mIm;

    Complex(double real, double im)
    {
        mReal = real;
        mIm = im;
    }

    Complex()
    {

    }

    double rho()
    {
        return Math.sqrt(mReal * mReal + mIm * mIm);
    }

    protected double theta()
    {
        return Math.atan2(mIm, mReal);
    }

    private Complex con()
    {
        return new Complex(mReal, -mIm);
    }

    Complex add(Complex other)
    {
        return new Complex(mReal + other.mReal, mIm + other.mIm);
    }

    Complex mul(Complex other)
    {
        return new Complex(mReal * other.mReal - mIm * other.mIm, mReal * other.mIm + mIm * other.mReal);
    }

    protected Complex mul(double a)
    {
        return new Complex(mReal * a, mIm * a);
    }

    Complex div(Complex other)
    {
        double lengthSquared = other.mReal * other.mReal + other.mIm * other.mIm;
        return mul(other.con()).div(lengthSquared);
    }

    private Complex div(double a)
    {
        return new Complex(mReal / a, mIm / a);
    }

    void set(double real, double im)
    {
        mReal = real;
        mIm = im;
    }
}