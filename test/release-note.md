## v1.0.14

### Bug fixes
- api generator plus
    - 🎉🎉🎉修复配置模块token成功后上传接口接口失败并提示: "默认 token 为空"
    - 🎉🎉🎉修复@RequestMapping等注释中path属性包含静态常量时路径解析为空
### New features
- api generator plus
    - 🎉 Copy as 选择端口时增加选择IP地址

## v1.0.13
v1.0.13_IC171.469473

### Bug fixes
- api generator plus
    - 🎉🎉🎉修修复读取字段时死循环
### New features
- api generator plus
    - 🎉 将注解标签 @res_body,@res_body_type,@res_body_is_json_schema 增加至 Live Templates

## v1.0.12
v1.0.12_IC171.469473

### Bug fixes
- api generator plus
    - 🎉🎉🎉修复copy as fetch、copy as axios字段过滤功能失效问题
    - 🎉🎉🎉修复copy as fetch、copy as axios未处理包含@RequestAttribute、@RequestHeader注释的字段的问题


## v1.0.11
v1.0.11_IC171.469473

### Bug fixes
- api generator plus
    - 🎉🎉🎉未指定接口名称时使用接口路径代替
    - 🎉🎉🎉修复Server Url或Token错误时空指针异常[(#7)](https://github.com/buyili/api-generator-plus/issues/7)
### New features
- copy as curl
    - 🎉🎉 新增Copy as axios功能

## v1.0.10
v1.0.10_IC171.469473

### Bug fixes
- api generator plus
    - 🎉🎉🎉修复类型转换异常 <a href="https://github.com/buyili/api-generator-plus/issues/6">(#6)</a>
- copy as curl
    - 🎉🎉🎉修复打开设置报错，保存后配置数据丢失等问题
### New features
- api generator plus
    - ✨✨ 新增自定义Date、LocalDateTime、LocalDate、LocalTime返回值


## v1.0.9
v1.0.9_IC171.469473

### Bug fixes
- api generator plus
    - 🎉🎉🎉修复字段类型为long而不是Long时报错问题
    - 🎉🎉🎉修复嵌套泛型导致idea卡死的问题 <a href="https://github.com/buyili/api-generator-plus/issues/4">(#4)</a>
    - 🎉🎉🎉修复返回类型为ResponseEntity&lt;xx&gt;时，idea卡死的问题


## v1.0.8
v1.0.8_IC171.469473

### Bug fixes
- api generator plus
    - 🎉 Fixed throw exception on 2020.2+ issue [(#1)](https://github.com/buyili/api-generator-plus/issues/1)
### New features
- api generator plus
    - ✨ Specify the tag for the interface
    - 🎉 Custom interface state


## v1.0.7
v1.0.7_IC171.469473

### Bug fixes
- api generator plus
    - 🎉🎉🎉Fixed Parameter description is not uploaded if it contains Spaces

### New features

- api generator plus
    - 🎉🎉🎉Ignore update response data for YApi api
    - 🎉🎉🎉Add annotation @res_body @res_body_is_json_schema @res_body_type



## v1.0.6
### New features
- 🎉add copy RESTful uri



## v1.0.5
### Bug fixes
- copy as curl
    - Fixed curl and fetch syntax errors when content-type was multipart/form-data
### Bug 修复
- copy as curl
    - 修复Content-Type为 multipart/form-data时curl、fetch语法错误
