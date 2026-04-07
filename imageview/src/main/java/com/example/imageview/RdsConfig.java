package com.example.imageview;

public class RdsConfig {
    // RDS MySQL配置
    public static final String DB_HOST = "your-rds-host.mysql.rds.aliyuncs.com";
    public static final int DB_PORT = 3306;
    public static final String DB_NAME = "todo_app";
    public static final String DB_USER = "your_username";
    public static final String DB_PASSWORD = "your_password";
    
    // 连接字符串
    public static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
}
