package com.auth.DataModels;

import java.util.HashMap;

public class ModelsManager {
    private static ModelsManager sharedInstance = new ModelsManager();

    private ModelsManager() {
    }

    public static ModelsManager getInstance() {
        return sharedInstance;
    }

    public static String getModelFilename(AbstractModel model) {
        return model.getClass().toString() + "/" + model.getUniqueIdent();
    }
}
