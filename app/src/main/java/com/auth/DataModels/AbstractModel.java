package com.auth.DataModels;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class AbstractModel {
    AbstractModel(){}

    public void saveToFile(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AbstractModel loadFromFile(String filename) {
        try {
            FileInputStream ios = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(ios);
            return (AbstractModel) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
