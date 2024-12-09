package com.example.lib.commons.config.kafka.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EnableConfigurationProperties
@Component
@ConfigurationProperties(value = "topic-config")
public class KafkaTopicConfig {

    private Map<String, TopicConfig> topicConfig;
}
