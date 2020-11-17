package com.springbean.test.config;

import com.springbean.test.dto.Student;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouwb
 * create 2020-11-17 22:16
 */
@Configuration
public class StudentConfig {
    @Bean
    public Student student(){
        return new Student();
    }
}
