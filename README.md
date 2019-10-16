# 接口文档

## `com.auth.NetworkUtil`

注册：

```java
// 所有的注册工作会在这个对象创建的时候完成
RegisterHandler registerHandler = new RegisterHandler(
  username, password, biologic, imei
);
// 创建完对象之后，通过调调用函数判断是否创建成功（也就意味这注册成功）
registerHandler.checkStatus();
```

PC 端认证：

```java
// 在前端扫码得到字符串之后，将字符串传入创建 PcAuthHandler 对象
PcAuthHandler pcAuthHandler = new PcAuthHandler(qrMessage);
// 创建完成后，判断是够创建成功（成功或失败后可以进行一些用户交互）
pcAuthHandler.checkStatus()
```

手机端认证：

```java
// 用户输入用户密码之后，直接创建这个对象，完成所有的认证工作
MobileAuthHandler mobileAuthHandler = new MobileAuthHandler(
  username, password
);
// 检查是否认证成功
mobileAuthHandler.checkStatus();
```

动态验证码认证：

```java
// 扫码后把字符串传入
DynamicAuthHandler dynamicAuthHandler = new DynamicAuthHandler(
  username, qrMessage, imei
);
// 检查是否认证成功
dynamicAuthHandler.checkStatus();
```

