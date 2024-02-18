package com.sy.fileoperation;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
public class FileOperationApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileOperationApplication.class, args);
    }

}
