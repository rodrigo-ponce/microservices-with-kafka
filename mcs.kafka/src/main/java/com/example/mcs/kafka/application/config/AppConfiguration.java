package com.example.mcs.kafka.application.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration
@ComponentScan(basePackages = {"com.example.mcs.kafka", "com.example.lib.commons"})
public class AppConfiguration {
}
