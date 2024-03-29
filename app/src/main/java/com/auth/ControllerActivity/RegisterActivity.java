package com.auth.ControllerActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.hardware.fingerprint.FingerprintManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.CancellationSignal;
import android.telephony.TelephonyManager;

import com.auth.DataModels.UserModel;
import com.auth.NetworkUtils.AbstractHandler;
import com.auth.Wrapper.ConvertUtil;
import com.auth.Wrapper.MD5Util;
import com.auth.NetworkUtils.RegisterHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Random;

import static junit.framework.Assert.assertTrue;

public class RegisterActivity extends AppCompatActivity {

    //用户名，密码，再次输入的密码的控件
    private EditText et_user_name,et_psw,et_psw_again;
    //用户名，密码，再次输入的密码的控件的获取值
    private String userName,psw,pswAgain;
    private FingerprintHelper helper;
    private FingerprintVerifyDialog fingerprintVerifyDialog;
    private FingerprintManager mfingerprintManager = null;
    private static final String TAG = RegisterActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面布局 ,注册界面
        setContentView(R.layout.activity_register);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    private void init() {

        //从activity_register.xml 页面中获取对应的UI控件
        Button btn_register = (Button) findViewById(R.id.btn_register);
        Button btn_fingerprint = (Button) findViewById(R.id.btn_fingerprint);
        et_user_name= (EditText) findViewById(R.id.et_user_name);
        et_psw= (EditText) findViewById(R.id.et_psw);
        et_psw_again= (EditText) findViewById(R.id.et_psw_again);
        helper = FingerprintHelper.getInstance();

        //录入指纹按钮
        btn_fingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fingerprintVerifyDialog == null) {
                    fingerprintVerifyDialog = new FingerprintVerifyDialog(RegisterActivity.this);
                }
                fingerprintVerifyDialog.setContentText("请验证指纹");
                fingerprintVerifyDialog.setOnCancelButtonClickListener(new FingerprintVerifyDialog.OnDialogCancelButtonClickListener() {
                    @Override
                    public void onCancelClick(View v) {
                        helper.stopAuthenticate();
                    }
                });
                fingerprintVerifyDialog.show();

                //获取系统服务对象
                mfingerprintManager = (FingerprintManager)getSystemService(Context.FINGERPRINT_SERVICE);

                /**************************设备功能检测*****************************/
                //检测是否有指纹识别的硬件
                if (!mfingerprintManager.isHardwareDetected()){
                    Toast.makeText(RegisterActivity.this,"您手机没有指纹识别设备",Toast.LENGTH_SHORT).show();
                    return ;
                }
                //检测设备是否处于安全状态中
                KeyguardManager keyguardManager =(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
                if (!keyguardManager.isKeyguardSecure()) {
                    //如果不是处于安全状态中，跳转打开安全保护（锁屏等）
                    return ;
                }
                //检测系统中是否注册的指纹
                if (!mfingerprintManager.hasEnrolledFingerprints()){
                    //没有录入指纹，跳转到指纹录入
                    Toast.makeText(RegisterActivity.this,"没有录入指纹！",Toast.LENGTH_SHORT).show();
                    return ;
                }

                /**************************开始指纹识别*****************************/
                mfingerprintManager.authenticate(null,cancellationSignal,0,myAuthCallback,null);
            }
        });

        //注册按钮
        btn_register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

//                String username = "shesl-meow";
//                String password = "shesl-meow";
//                BigInteger biologic = BigInteger.valueOf(0x1);
//                BigInteger fake_IMEI = BigInteger.valueOf(0x7fff);
//                Toast.makeText(RegisterActivity.this, "123456", Toast.LENGTH_SHORT).show();
//                RegisterHandler registerHandler = new RegisterHandler(username, password, biologic, fake_IMEI);
//                if(registerHandler.checkStatus()) {
//                    Toast.makeText(RegisterActivity.this, "error", Toast.LENGTH_SHORT).show();
//                }

                //获取输入在相应控件中的字符串
                getEditString();
                //判断输入框内容
                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();

                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();

                }else if(TextUtils.isEmpty(pswAgain)) {
                    Toast.makeText(RegisterActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
                } else if(!psw.equals(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "输入两次的密码不一样", Toast.LENGTH_SHORT).show();
                } else if(psw.length() < 8){
                    Toast.makeText(RegisterActivity.this, "密码强度小于8位，请重新输入", Toast.LENGTH_SHORT).show();
                } else if(psw.matches("^[A-Za-z]+$") || psw.matches("^[0-9]*$")){
                    Toast.makeText(RegisterActivity.this, "密码应设置为数字和字母的组合", Toast.LENGTH_SHORT).show();
                } else{
                    UserModel userModel = new UserModel(ConvertUtil.zeroRPad(userName, 64)) {
                        {
                            password = ConvertUtil.zeroRPad(psw, 64);
                            salt = ConvertUtil.zeroRPad(
                                    (new BigInteger(256, new Random())).toString(16), 64
                            );
                            biologic = BigInteger.valueOf(1);
                            imei = BigInteger.valueOf(0x7fff);
                        }
                    };
                    // 所有的注册工作会在这个对象创建的时候完成
                    //RegisterHandler registerHandler = new RegisterHandler(userName, psw, 1, getIMEI(RegisterActivity.this));
                    RegisterHandler registerHandler = new RegisterHandler(
                            userModel,
                            (AbstractHandler caller) -> {
                                Log.i("ActivityDebug", "success");
                            },
                            (AbstractHandler caller) -> {
                                Log.i("ActivityDebug", "failed");
                            }
                    );
                    // 创建完对象之后，通过调调用函数判断是否创建成功（也就意味这注册成功）
                    if (registerHandler.checkStatus()){
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        //注册成功后把账号传递到LoginActivity.java中
                        // 返回值到loginActivity显示
                        Intent data = new Intent();
                        data.putExtra("userName", userName);
                        setResult(RESULT_OK, data);
                        //RESULT_OK为Activity系统常量，状态码为-1，
                        // 表示此页面下的内容操作成功将data返回到上一页面，如果是用back返回过去的则不存在用setResult传递data值
                        RegisterActivity.this.finish();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    }

                }
            }
        });
    }
    /**
     * 获取控件中的字符串
     */
    private void getEditString(){
        userName=et_user_name.getText().toString().trim();
        psw=et_psw.getText().toString().trim();
        pswAgain=et_psw_again.getText().toString().trim();
    }
    /**
     * 从SharedPreferences中读取输入的用户名，判断SharedPreferences中是否有此用户名
     */
    private boolean isExistUserName(String userName){
        boolean has_userName=false;
        //mode_private SharedPreferences sp = getSharedPreferences( );
        // "loginInfo", MODE_PRIVATE
        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        //获取密码
        String spPsw=sp.getString(userName, "");//传入用户名获取密码
        //如果密码不为空则确实保存过这个用户名
        if(!TextUtils.isEmpty(spPsw)) {
            has_userName=true;
        }
        return has_userName;
    }
//    /**
//     * 保存账号和密码到SharedPreferences中SharedPreferences
//     */
//    private void saveRegisterInfo(String userName,String psw){
//        String md5Psw = MD5Util.md5(psw);//把密码用MD5加密
//        //loginInfo表示文件名, mode_private SharedPreferences sp = getSharedPreferences( );
//        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
//
//        //获取编辑器， SharedPreferences.Editor  editor -> sp.edit();
//        SharedPreferences.Editor editor=sp.edit();
//        //以用户名为key，密码为value保存在SharedPreferences中
//        //key,value,如键值对，editor.putString(用户名，密码）;
//        editor.putString(userName, md5Psw);
//        //提交修改 editor.commit();
//        editor.apply();
//    }

    //初始化cancellationSignal类
    private CancellationSignal cancellationSignal = new CancellationSignal();
    //初始化MyAuthCallback
    private FingerprintManager.AuthenticationCallback myAuthCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Toast.makeText(RegisterActivity.this,"识别成功！",Toast.LENGTH_SHORT).show();
            fingerprintVerifyDialog.dismiss();
            try {
                Field field = result.getClass().getDeclaredField("mFingerprint");
                field.setAccessible(true);
                Object fingerPrint = field.get(result);

                if (fingerPrint != null) {
                    Class<?> clazz = Class.forName("android.hardware.fingerprint.Fingerprint");
                    Method getName = clazz.getDeclaredMethod("getName");
                    Method getFingerId = clazz.getDeclaredMethod("getFingerId");
                    Method getGroupId = clazz.getDeclaredMethod("getGroupId");
                    Method getDeviceId = clazz.getDeclaredMethod("getDeviceId");

                    CharSequence name = (CharSequence) getName.invoke(fingerPrint);
                    int fingerID = (int) getFingerId.invoke(fingerPrint);
                    int groupID = (int) getGroupId.invoke(fingerPrint);
                    long deviceID = (long) getDeviceId.invoke(fingerPrint);

                    Toast.makeText(RegisterActivity.this,name,Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "name" + name);
                    Log.d(TAG, "fingerID" + fingerID);
                    Log.d(TAG, "groupID" + groupID);
                    Log.d(TAG, "deviceID" + deviceID);
                }
            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Toast.makeText(RegisterActivity.this,"识别失败，请重试！",Toast.LENGTH_SHORT).show();
        }
    };

    /**
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Method method = manager.getClass().getMethod("getImei", int.class);
                String imei1 = (String) method.invoke(manager, 0);
                String imei2 = (String) method.invoke(manager, 1);
                if(TextUtils.isEmpty(imei2)){
                    return imei1;
                }
                if(!TextUtils.isEmpty(imei1)){
                    //因为手机卡插在不同位置，获取到的imei1和imei2值会交换，所以取它们的最小值,保证拿到的imei都是同一个
                    String imei = "";
                    if(imei1.compareTo(imei2) <= 0){
                        imei = imei1;
                    }else{
                        imei = imei2;
                    }
                    return imei;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return manager.getDeviceId();
            }
            return "";
        }

}
