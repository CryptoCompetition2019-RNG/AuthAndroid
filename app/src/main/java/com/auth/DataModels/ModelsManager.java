package com.auth.DataModels;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.os.Environment;

import com.auth.Wrapper.ConvertUtil;

import org.apache.commons.codec.binary.Hex;
import org.zz.gmhelper.SM3Util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ModelsManager {
    private static Context appContext;
    private static ModelsManager sharedInstance;

    public ModelsManager(Context context) {
        appContext = context;
        sharedInstance = this;
    }

    public static ModelsManager getInstance() {
        return sharedInstance;
    }

    public static String getModelFilename(AbstractModel model) {
        byte[] filename = SM3Util.hash(
                model.getUniqueIdent().getBytes(StandardCharsets.US_ASCII)
        );
        if(appContext == null){
            return "UserModelSavedMessage/" + ConvertUtil.encodeHexString(filename);
        } else {
            return appContext.getDataDir().toString() + '/' + ConvertUtil.encodeHexString(filename);
        }
    }
}
