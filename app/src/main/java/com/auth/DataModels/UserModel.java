package com.auth.DataModels;

import com.auth.Wrapper.ConvertUtil;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;

/*
 * 在客户端上，JsonObject dataModel 只保存 username 与 salt 两个字段
 */
public class UserModel extends AbstractModel {
    public String username;
    public String password;
    public String salt;
    public BigInteger biologic;
    public BigInteger imei;
    public byte[] randomToken;

    public UserModel(String username) {
        super(ConvertUtil.zeroRPad(username, 64));
    }

    @Override
    protected String getUniqueIdent() {
        return username;
    }

    @Override
    protected void setUniqueIdent(String id) {
        this.username = id;
    }

    @Override
    public void saveToFile() {
        try {
            this.modelData.put("username", username);
            this.modelData.put("salt", salt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.saveToFile();
    }

    @Override
    public void loadFromFile() {
        super.loadFromFile();
        try {
            username = this.modelData.getString("username");
            salt = this.modelData.getString("salt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getSaltSm4Key() {
        try {
            return Hex.decodeHex(ConvertUtil.zeroRPad(salt, 32));
        } catch (DecoderException de){
            return null;
        }
    }
}
