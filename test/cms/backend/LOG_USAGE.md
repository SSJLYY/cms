# Log4j2 日志使用说明

## 日志配置

项目使用 SLF4J + Log4j2 作为日志框架，配置文件位于：
`src/main/resources/log4j2-spring.xml`

## 日志文件说明

日志文件存放在 `logs/` 目录下，按天自动切割：

### 日志文件类型

1. **application.log** - 所有级别的日志
   - 文件名格式：`application-yyyy-MM-dd.log`
   - 示例：`application-2025-01-15.log`

2. **application-info.log** - INFO级别日志
   - 文件名格式：`application-info-yyyy-MM-dd.log`
   - 示例：`application-info-2025-01-15.log`

3. **application-warn.log** - WARN级别日志
   - 文件名格式：`application-warn-yyyy-MM-dd.log`
   - 示例：`application-warn-2025-01-15.log`

4. **application-error.log** - ERROR级别日志
   - 文件名格式：`application-error-yyyy-MM-dd.log`
   - 示例：`application-error-2025-01-15.log`

### 日志切割规则

- **按时间切割**：每天凌晨0点自动切割，生成新的日志文件
- **按大小切割**：单个文件超过100MB时也会切割
- **保留时间**：自动保留最近30天的日志，超过30天的日志会被自动删除

## 代码中使用日志

### 1. 引入日志

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class YourService {
    // 使用 log 对象记录日志
}
```

或者不使用Lombok：

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class YourService {
    private static final Logger log = LoggerFactory.getLogger(YourService.class);
}
```

### 2. 记录不同级别的日志

```java
// DEBUG - 调试信息
log.debug("调试信息：变量值 = {}", value);

// INFO - 一般信息
log.info("用户登录成功：username = {}", username);

// WARN - 警告信息
log.warn("配置项缺失，使用默认值：key = {}", key);

// ERROR - 错误信息
log.error("数据库操作失败", exception);
log.error("处理请求失败：id = {}, error = {}", id, error);
```

### 3. 日志最佳实践

#### ✅ 推荐做法

```java
// 使用占位符，避免字符串拼接
log.info("用户 {} 执行了 {} 操作", username, action);

// 记录异常堆栈
try {
    // 业务代码
} catch (Exception e) {
    log.error("操作失败：{}", e.getMessage(), e);
}

// 关键业务节点记录日志
log.info("开始处理订单：orderId = {}", orderId);
// 处理逻辑
log.info("订单处理完成：orderId = {}, result = {}", orderId, result);
```

#### ❌ 不推荐做法

```java
// 不要使用字符串拼接
log.info("用户 " + username + " 执行了 " + action + " 操作");

// 不要在循环中大量打印日志
for (int i = 0; i < 10000; i++) {
    log.info("处理第 {} 条数据", i); // 会产生大量日志
}

// 不要记录敏感信息
log.info("用户密码：{}", password); // 不要记录密码
log.info("银行卡号：{}", cardNumber); // 不要记录敏感信息
```

## 日志级别说明

- **TRACE** - 最详细的信息，通常只在开发时使用
- **DEBUG** - 调试信息，用于开发和测试
- **INFO** - 一般信息，记录系统运行状态
- **WARN** - 警告信息，可能出现问题但不影响运行
- **ERROR** - 错误信息，系统出现错误需要关注

## 生产环境配置建议

在生产环境中，建议：

1. 将日志级别设置为 INFO 或 WARN
2. 适当增加日志保留天数（如60天或90天）
3. 定期检查日志文件大小，避免磁盘空间不足
4. 使用日志分析工具（如ELK）进行日志收集和分析

## 修改日志配置

如需修改日志配置，编辑 `log4j2-spring.xml` 文件：

```xml
<!-- 修改日志保留天数 -->
<IfLastModified age="60d"/>  <!-- 改为60天 -->

<!-- 修改单个文件大小限制 -->
<SizeBasedTriggeringPolicy size="200MB"/>  <!-- 改为200MB -->

<!-- 修改日志级别 -->
<Root level="WARN">  <!-- 改为WARN级别 -->
```

修改后重启应用即可生效。
