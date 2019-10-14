package com.trng.pwdProtection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UpdateActivity extends AppCompatActivity {

    //用户名，密码，再次输入的密码的控件
    private EditText et_username,et_newpwd,et_newpwd_again;
    //用户名，密码，再次输入的密码的控件的获取值
    private String userName,newpwd,newpwdAgain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面布局 ,更改密码界面
        setContentView(R.layout.activity_updatepwd);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    private void init() {

        //从activity_updatepwd.xml 页面中获取对应的UI控件
        Button btn_updatepwd = (Button) findViewById(R.id.btn_updatepwd);
        et_username= (EditText) findViewById(R.id.et_username);
        et_newpwd= (EditText) findViewById(R.id.et_newpwd);
        et_newpwd_again= (EditText) findViewById(R.id.et_newpwd_again);
        //更改密码按钮
        btn_updatepwd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //获取输入在相应控件中的字符串
                getEditString();
                //判断输入框内容


                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(UpdateActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();

                }else if(TextUtils.isEmpty(newpwd)){
                    Toast.makeText(UpdateActivity.this, "请输入新密码", Toast.LENGTH_SHORT).show();

                }else if(TextUtils.isEmpty(newpwdAgain)) {
                    Toast.makeText(UpdateActivity.this, "请再次输入新密码", Toast.LENGTH_SHORT).show();
                } else if(!newpwd.equals(newpwdAgain)){
                    Toast.makeText(UpdateActivity.this, "输入两次的密码不一样", Toast.LENGTH_SHORT).show();

                    /**
                     *从SharedPreferences中读取输入的用户名，判断SharedPreferences中是否有此用户名
                     */
                }else if(isExistUserName(userName)==false){
                    Toast.makeText(UpdateActivity.this, "此账户名不存在", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(UpdateActivity.this, "更改成功", Toast.LENGTH_SHORT).show();
                    //把账号、密码和账号标识保存到sp里面
                    /**
                     * 保存账号和密码到SharedPreferences中
                     */
                    saveUpdateInfo(userName, newpwd);
                    //更改成功后把账号传递到LoginActivity.java中
                    // 返回值到loginActivity显示
                    Intent data = new Intent();
                    data.putExtra("userName", userName);
                    setResult(RESULT_OK, data);
                    //RESULT_OK为Activity系统常量，状态码为-1，
                    // 表示此页面下的内容操作成功将data返回到上一页面，如果是用back返回过去的则不存在用setResult传递data值
                    UpdateActivity.this.finish();
                }
            }
        });
    }
    /**
     * 获取控件中的字符串
     */
    private void getEditString(){
        userName=et_username.getText().toString().trim();
        newpwd=et_newpwd.getText().toString().trim();
        newpwdAgain=et_newpwd_again.getText().toString().trim();
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
    /**
     * 保存账号和密码到SharedPreferences中SharedPreferences
     */
    private void saveUpdateInfo(String userName,String psw){
        String md5Psw = MD5Utils.md5(psw);//把密码用MD5加密
        //loginInfo表示文件名, mode_private SharedPreferences sp = getSharedPreferences( );
        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);

        //获取编辑器， SharedPreferences.Editor  editor -> sp.edit();
        SharedPreferences.Editor editor=sp.edit();
        //以用户名为key，密码为value保存在SharedPreferences中
        //key,value,如键值对，editor.putString(用户名，密码）;
        editor.putString(userName, md5Psw);
        //提交修改 editor.commit();
        editor.apply();
    }
}