# APP端体检报告接口对接文档

## 1. 说明

本文档用于给 App 前端对接体检报告上传模块。

当前版本已支持：

1. 上传体检报告
2. 体检报告列表
3. 体检报告详情
4. 删除体检报告


## 2. 认证方式

以下所有接口都属于登录后接口，请在请求头中传：

```http
Authorization: Bearer {token}
```


## 3. 接口列表

### 3.1 体检报告列表

#### 接口地址

`GET /app/reports`

#### 查询参数

1. `pageNum`：页码，默认 `1`
2. `pageSize`：每页数量，默认 `10`
3. `memberId`：家庭成员ID，可选
4. `reportType`：报告类型，可选
5. `parseStatus`：解析状态，可选
6. `keyword`：关键字，可选，支持报告名称、医院名称、原始文件名模糊搜索

#### 返回示例

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "total": 1,
    "rows": [
      {
        "reportId": 1,
        "memberId": 1,
        "memberName": "妈妈",
        "reportName": "2026年春季体检报告",
        "reportType": "体检报告",
        "hospitalName": "杭州第一人民医院",
        "reportDate": "2026-04-21",
        "fileUrl": "/profile/report/20260423123000_report_xxx.pdf",
        "originalFileName": "体检报告.pdf",
        "fileSize": 204800,
        "fileExtension": "pdf",
        "parseStatus": 0,
        "analysisSummary": null,
        "remark": "年度体检",
        "createTime": "2026-04-23 12:30:00"
      }
    ]
  }
}
```

### 3.2 体检报告详情

#### 接口地址

`GET /app/reports/{reportId}`

### 3.3 上传体检报告

#### 接口地址

`POST /app/reports`

#### 请求方式

`multipart/form-data`

#### 表单字段

1. `file`：报告文件，必填，支持 pdf / jpg / jpeg / png 等当前系统允许类型
2. `memberId`：家庭成员ID，必填
3. `reportName`：报告名称，可选；不传则后端默认使用原始文件名
4. `reportType`：报告类型，可选
5. `hospitalName`：医院名称，可选
6. `reportDate`：报告日期，可选，格式 `yyyy-MM-dd`
7. `remark`：备注，可选

#### curl 示例

```bash
curl -X POST "http://localhost:18012/app/reports" \
  -H "Authorization: Bearer {token}" \
  -F "file=@D:/reports/checkup.pdf" \
  -F "memberId=1" \
  -F "reportName=2026年春季体检报告" \
  -F "reportType=体检报告" \
  -F "hospitalName=杭州第一人民医院" \
  -F "reportDate=2026-04-21" \
  -F "remark=年度体检"
```

### 3.4 删除体检报告

#### 接口地址

`DELETE /app/reports/{reportId}`


## 4. 状态说明

### 4.1 解析状态

1. `0`：待解析
2. `1`：解析中
3. `2`：已解析
4. `3`：解析失败


## 5. 前端对接建议

1. 上传成功后建议立即刷新报告列表
2. 列表页可以按成员和解析状态筛选
3. 报告详情页可直接使用 `fileUrl` 做在线预览或下载跳转
4. 当前删除为逻辑删除，前端删除后直接从列表移除即可
