## token

## token延期

1. 更换新的带有过期时间的token，优点是不需要存储，不占用系统资源，缺点是保证不了实时性。
2. 生成永久token，用redis维护token。需要存储，有实时性。

## 2个token

1. access_token
2. reference_token

## token三段式

[参考](https://www.cnblogs.com/aaron911/p/11300062.html)

1. header base64 
2. Payload base64
3. Signature

Base64 不是加密方式。是一种编码格式。

# 加密

不用md5，防止彩虹表

用BCrypt强哈希方法 每次加密的结果都不一样。