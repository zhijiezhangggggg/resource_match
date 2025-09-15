# 后台管理系统

## 项目简介

基于SpringBoot的后台管理系统框架，提供完整的基础架构和通用功能。

## 技术栈

- **后端框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0+
- **ORM框架**: MyBatis Plus 3.5.3.1
- **连接池**: Druid 1.2.16
- **工具类**: Hutool 5.8.20
- **JSON处理**: FastJSON 2.0.25
- **安全框架**: Spring Security
- **构建工具**: Maven

## 项目结构

```
src/main/java/com/admin/
├── AdminSystemApplication.java          # 启动类
├── common/                              # 通用类
│   ├── BaseEntity.java                  # 基础实体类
│   ├── BaseService.java                 # 基础Service接口
│   ├── BaseServiceImpl.java             # 基础Service实现
│   ├── BusinessException.java           # 业务异常
│   ├── GlobalExceptionHandler.java      # 全局异常处理器
│   ├── PageParam.java                   # 分页参数
│   ├── PageResult.java                  # 分页结果
│   ├── ResponseResultHandler.java       # 统一响应处理器
│   └── Result.java                      # 统一响应结果
├── config/                              # 配置类
│   ├── CorsConfig.java                  # 跨域配置
│   ├── DruidConfig.java                 # Druid配置
│   ├── MyMetaObjectHandler.java         # MyBatis自动填充
│   ├── MybatisPlusConfig.java           # MyBatis Plus配置
│   └── SecurityConfig.java             # Spring Security配置
└── controller/                          # 控制器
    └── SystemController.java            # 系统控制器
```

## 功能特性

- ✅ 统一响应格式
- ✅ 全局异常处理
- ✅ 分页查询支持
- ✅ 数据库连接池配置
- ✅ 跨域处理
- ✅ 日志配置
- ✅ 基础CRUD操作
- ✅ 自动填充创建/更新时间
- ✅ 逻辑删除支持

## 快速开始

### 1. 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE admin_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p admin_system < src/main/resources/sql/init.sql
```

3. 修改数据库连接配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/admin_system?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: your_username
    password: your_password
```

### 3. 启动项目

```bash
# 编译项目
mvn clean compile

# 启动项目
mvn spring-boot:run
```

### 4. 访问地址

- 应用地址: http://localhost:8080/api
- Druid监控: http://localhost:8080/api/druid
- 健康检查: http://localhost:8080/api/system/health

## API接口

### 系统接口

#### 健康检查
- **请求URL**: `/api/system/health`
- **请求方式**: `GET`
- **响应示例**:
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": "系统运行正常",
    "timestamp": 1640995200000
}
```

#### 系统信息
- **请求URL**: `/api/system/info`
- **请求方式**: `GET`
- **响应示例**:
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": "后台管理系统 v1.0.0",
    "timestamp": 1640995200000
}
```

## 开发指南

### 1. 创建实体类

继承 `BaseEntity` 类：

```java
@Data
@TableName("sys_user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    // ... 其他字段
}
```

### 2. 创建Mapper接口

继承 `BaseMapper` 接口：

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

### 3. 创建Service接口和实现

```java
public interface UserService extends BaseService<User> {
    // 自定义业务方法
}

@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {
    // 实现自定义业务方法
}
```

### 4. 创建Controller

```java
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/list")
    public Result<PageResult<User>> list(PageParam pageParam) {
        Page<User> page = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        Page<User> result = userService.page(page);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), 
                                              pageParam.getCurrent(), pageParam.getSize()));
    }
}
```

## 配置说明

### 数据库配置

项目使用Druid连接池，支持以下配置：

- 连接池大小配置
- SQL监控
- 慢SQL检测
- 连接泄露检测

### 日志配置

- 控制台日志输出
- 文件日志输出
- 日志级别配置
- 日志格式自定义

## 注意事项

1. 所有实体类建议继承 `BaseEntity`
2. 所有Service建议继承 `BaseService` 和 `BaseServiceImpl`
3. 所有Controller返回结果会自动包装为 `Result` 格式
4. 异常会被全局异常处理器统一处理
5. 数据库操作支持逻辑删除和自动填充

## 许可证

MIT License
