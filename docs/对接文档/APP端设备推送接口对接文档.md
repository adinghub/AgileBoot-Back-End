# APP端设备推送接口对接文档

## 1. 说明

本文档用于给 App 前端对接设备注册与 Push 绑定模块。

当前实现目标：

1. App 把当前设备 Token 注册到后端
2. 后端把用药提醒派发到当前用户的设备列表
3. App 可查看和停用当前账号下的设备
4. 提醒 Push 透传业务载荷与首页任务体系保持一致
5. 报告建议 Push 透传业务载荷与首页任务体系保持一致


## 2. 认证方式

以下所有接口都属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 接口列表

### 3.1 注册或刷新设备

#### 接口地址

`POST /app/push/devices`

#### 请求体示例

```json
{
  "deviceCode": "9db83ce2-7d15-4f85-933d-54c20ea6a201",
  "pushPlatform": "ANDROID",
  "deviceToken": "android_push_token_xxx",
  "deviceModel": "Xiaomi 14",
  "manufacturer": "Xiaomi",
  "osVersion": "Android 15",
  "appVersion": "1.0.0"
}
```

#### 字段说明

1. `deviceCode`：设备唯一编码，建议 App 安装后生成稳定 UUID
2. `pushPlatform`：推送平台，当前支持 `ANDROID`、`IOS`、`HARMONY`
3. `deviceToken`：Push Token
4. `deviceModel`：设备型号，可选
5. `manufacturer`：设备厂商，可选
6. `osVersion`：系统版本，可选
7. `appVersion`：App版本，可选

#### 说明

该接口支持幂等刷新：

1. 同一设备重复注册会覆盖最新 Token
2. 同一设备切换账号会更新归属用户

### 3.2 当前用户设备列表

#### 接口地址

`GET /app/push/devices`

#### 返回示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": [
    {
      "deviceId": 1,
      "deviceCode": "9db83ce2-7d15-4f85-933d-54c20ea6a201",
      "pushPlatform": "ANDROID",
      "deviceModel": "Xiaomi 14",
      "manufacturer": "Xiaomi",
      "osVersion": "Android 15",
      "appVersion": "1.0.0",
      "lastActiveTime": "2026-04-23 12:00:00",
      "status": 1
    }
  ]
}
```

### 3.3 停用设备

#### 接口地址

`DELETE /app/push/devices/{deviceCode}`

#### 说明

停用后该设备不会再接收新的用药提醒 Push。


## 4. 前端对接建议

1. App 登录成功后应立即调用“注册设备”接口
2. Push Token 刷新时应重新调用“注册设备”接口覆盖旧值
3. 用户退出登录时，建议调用“停用设备”接口
4. 若用户关闭系统通知权限，建议同步停用当前设备
5. 通知点击后的透传业务载荷建议参考《APP端推送消息业务载荷对接文档》
