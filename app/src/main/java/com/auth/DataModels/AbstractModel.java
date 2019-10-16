package com.auth.DataModels;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;

public abstract class AbstractModel {
    protected JSONObject modelData = new JSONObject();

    AbstractModel(String id) {
        this.setUniqueIdent(id);
    }

    protected abstract String getUniqueIdent();

    protected abstract void setUniqueIdent(String id);

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
        JSONParser jparser = new JSONParser();
        try (FileReader fr = new FileReader(filename)) {
            modelData = (JSONObject) jparser.parse(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
