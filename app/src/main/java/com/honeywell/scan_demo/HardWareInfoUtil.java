package com.honeywell.scan_demo;

import android.content.Context;
import android.os.Build;

public class HardWareInfoUtil {
    private static HardWareInfoUtil hwi;
    private Context context;

    private HardWareInfoUtil(Context context){
        this.context = context;
    }

    public static HardWareInfoUtil gethardwareinfo(Context context){
        if (hwi==null){
            synchronized (HardWareInfoUtil.class){
                hwi = new HardWareInfoUtil(context);
            }
        }
        return hwi;
    }

    // 获取主板名字
    public String getBordName(){
        return Build.BOARD;
    }

    // 获取手机出厂商品牌
    public String getBrand(){
        return Build.BRAND;
    }

    // 获取手机型号
    public String getPhoneVersionName(){
        return Build.PRODUCT;
    }

    // 获取制造商
    public String getManufacturer(){
        return Build.MANUFACTURER;
    }
}
