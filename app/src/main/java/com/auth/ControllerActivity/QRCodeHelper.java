package com.auth.ControllerActivity;

import android.content.Intent;

import com.yzq.zxinglibrary.common.Constant;

public class QRCodeHelper {

    public QRCodeHelper(){

    }
    /**
     * 处理二维码扫描结果
     */
    public String QRcode(Intent data) {
        //String[] result = new String[3];
        String content = "";
        if (data != null) {
            content = data.getStringExtra(Constant.CODED_CONTENT);
            //result = content.split("|");

//            //用默认浏览器打开扫描得到的地址
//            Intent intent = new Intent();
//            intent.setAction("android.intent.action.VIEW");
//            Uri content_url = Uri.parse(content);
//            intent.setData(content_url);
//            startActivity(intent);
        }
        return content;
    }
}
