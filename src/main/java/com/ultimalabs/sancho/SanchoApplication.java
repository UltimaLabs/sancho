package com.ultimalabs.sancho;

import com.ultimalabs.sancho.common.config.YamlPropertySourceFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@PropertySource(value = "classpath:/sancho.yml", factory = YamlPropertySourceFactory.class)
public class SanchoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SanchoApplication.class, args);
    }

}
