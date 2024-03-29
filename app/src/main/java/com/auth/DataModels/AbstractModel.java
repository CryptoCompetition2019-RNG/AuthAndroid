package com.auth.DataModels;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public abstract class AbstractModel {
    protected boolean loadStatus = false;

    protected JSONObject modelData = new JSONObject();

    protected abstract String getUniqueIdent();

    protected abstract void setUniqueIdent(String id);

    protected AbstractModel(String id) {
        this.setUniqueIdent(id);
    }

    public boolean checkLoaded(){return loadStatus;}

    public void saveToFile() {
        String filename = ModelsManager.getModelFilename(this);
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(modelData.toString());
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile() {
        String filename = ModelsManager.getModelFilename(this);
        try {
            String content = new Scanner(new File(filename)).useDelimiter("\\Z").next();
            modelData = new JSONObject(content);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        this.loadStatus = true;
    }
}
