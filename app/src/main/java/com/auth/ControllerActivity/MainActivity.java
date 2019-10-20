package com.auth.ControllerActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.uuzuche.lib_zxing.activity.CaptureActivity;
//import com.auth.CryptoUtils.MD5Util;
import com.auth.NetworkUtils.MobileAuthHandler;
import com.auth.NetworkUtils.PcAuthHandler;
import com.auth.NetworkUtils.DynamicAuthHandler;
import com.yzq.zxinglibrary.android.CaptureActivity;

public class MainActivity extends AppCompatActivity {

    /**
     * 扫描跳转Activity RequestCode
     */
    public static final int REQUEST_CODE_SCAN = 111;

    private String userName,psw,spPsw;//获取的用户名，密码，加密密码
    private EditText et_user_name,et_psw;//编辑框

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    /*
     *  获取界面控件
     */
    private void init() {
        //从main_title_bar中获取的id
        //从activity_login.xml中获取的
        TextView tv_register = (TextView) findViewById(R.id.tv_register);
        TextView tv_find_psw = (TextView) findViewById(R.id.tv_find_psw);
        Button btn_login = (Button) findViewById(R.id.btn_login);
        et_user_name= (EditText) findViewById(R.id.et_username);
        et_psw= (EditText) findViewById(R.id.et_newpsw);
        Button btn_qrcode=(Button)findViewById(R.id.btn_qrcode);

        //扫描二维码控件的点击事件
        btn_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                /*ZxingConfig是配置类  可以设置是否显示底部布局，闪光灯，相册，是否播放提示音  震动等动能
                 * 也可以不传这个参数
                 * 不传的话  默认都为默认不震动  其他都为true
                 * */

                //ZxingConfig config = new ZxingConfig();
                //config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                //config.setPlayBeep(true);//是否播放提示音
                //config.setShake(true);//是否震动
                //config.setShowAlbum(true);//是否显示相册
                //config.setShowFlashLight(true);//是否显示闪光灯
                //intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                startActivityForResult(intent, REQUEST_CODE_SCAN);

            }
        });

        // 立即注册控件的点击事件
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //为了跳转到注册界面，并实现注册功能
                Intent intent=new Intent(MainActivity.this,RegisterActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // 找回密码控件的点击事件
        tv_find_psw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,LostFindActivity.class));
            }
        });

        //登录按钮的点击事件
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始登录，获取用户名和密码 getText().toString().trim();
                userName = et_user_name.getText().toString().trim();
                psw = et_psw.getText().toString().trim();
                // 用户输入用户密码之后，直接创建这个对象，完成所有的认证工作
                MobileAuthHandler mobileAuthHandler = new MobileAuthHandler(userName, psw);
                // TextUtils.isEmpty
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(MainActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(psw)) {
                    Toast.makeText(MainActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    // 检查是否认证成功
                } else if ( mobileAuthHandler.checkStatus()) {
                    //一致登录成功
                    Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    //保存登录状态，在界面保存登录的用户名 定义个方法 saveLoginStatus boolean 状态 , userName 用户名;
                    saveLoginStatus(true, userName);
                    //登录成功后关闭此页面进入主页
                    Intent data = new Intent();
                    //datad.putExtra( ); name , value ;
                    data.putExtra("isLogin", true);
                    //RESULT_OK为Activity系统常量，状态码为-1
                    // 表示此页面下的内容操作成功将data返回到上一页面，如果是用back返回过去的则不存在用setResult传递data值
                    setResult(RESULT_OK, data);
                    //销毁登录界面
                    MainActivity.this.finish();
                    //跳转到主界面，登录成功的状态传递到 MainActivity 中
                    startActivity(new Intent(MainActivity.this, ItemActivity.class));
                } else if (!mobileAuthHandler.checkStatus()) {
                    Toast.makeText(MainActivity.this, "输入的用户名和密码不一致", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "此用户名不存在", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    /**
//     * 从SharedPreferences中根据用户名读取密码
//     */
//    private String readPassword(String userName){
//        //getSharedPreferences("loginInfo",MODE_PRIVATE);
//        //"loginInfo",mode_private; MODE_PRIVATE表示可以继续写入
//        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
//        //sp.getString() userName, "";
//        return sp.getString(userName , "");
//    }

    /**
     * 保存登录状态和登录用户名到SharedPreferences中
     */
    private void saveLoginStatus(boolean status,String userName){
        //saveLoginStatus(true, userName);
        //loginInfo表示文件名  SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        //获取编辑器
        SharedPreferences.Editor editor=sp.edit();
        //存入boolean类型的登录状态
        editor.putBoolean("isLogin", status);
        //存入登录状态时的用户名
        editor.putString("loginUserName", userName);
        //提交修改
        editor.apply();
    }

    /**
     * 注册成功的数据返回至此
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            //是获取注册界面回传过来的用户名
            // getExtra().getString("***");
            String userName=data.getStringExtra("userName");
            if(!TextUtils.isEmpty(userName)){
                //设置用户名到 et_user_name 控件
                et_user_name.setText(userName);
                //et_user_name控件的setSelection()方法来设置光标位置
                et_user_name.setSelection(userName.length());
            }
        }

        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            QRCodeHelper qrcodehelper = new QRCodeHelper();
            String result = qrcodehelper.QRcode(data);
            int len = result.length();
            if (result.charAt(len-1) == '0') {
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));
            } else if (result.charAt(len-1) == '1') {
                startActivity(new Intent(MainActivity.this,VerifyActivity.class));
                // 在前端扫码得到字符串之后，将字符串传入创建 PcAuthHandler 对象
                PcAuthHandler pcAuthHandler = new PcAuthHandler(result.substring(0,len-1));
                // 创建完成后，判断是够创建成功（成功或失败后可以进行一些用户交互）
                pcAuthHandler.checkStatus();
            } else if (result.charAt(len-1) == '2') {
                startActivity(new Intent(MainActivity.this,VerifyActivity.class));
                // 扫码后把字符串传入
                DynamicAuthHandler dynamicAuthHandler = new DynamicAuthHandler(result.substring(0,64), result.substring(64,128), "1111");
                // 检查是否认证成功
                dynamicAuthHandler.checkStatus();
            } else {
                startActivity(new Intent(MainActivity.this,UpdateActivity.class));
            }

        }
    }

}
