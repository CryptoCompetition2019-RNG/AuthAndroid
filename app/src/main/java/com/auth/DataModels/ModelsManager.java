package com.auth.DataModels;

import org.apache.commons.codec.binary.Hex;
import org.zz.gmhelper.SM3Util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ModelsManager {
    private static ModelsManager sharedInstance = new ModelsManager();

    private ModelsManager() {
    }

    public static ModelsManager getInstance() {
        return sharedInstance;
    }

    public static String getModelFilename(AbstractModel model) {
        byte[] filename = SM3Util.hash(
                model.getUniqueIdent().getBytes(StandardCharsets.US_ASCII)
        );
        return "UserModelSavedMessage/" + Hex.encodeHexString(filename);
    }
}
