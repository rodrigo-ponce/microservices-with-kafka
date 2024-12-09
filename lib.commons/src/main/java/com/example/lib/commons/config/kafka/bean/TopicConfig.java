package com.example.lib.commons.config.kafka.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicConfig {

    private String resultStorageTopic;
    private int numberOfRetriesOnFailure;
    private long retryInterval;
    private int maxConcurrency;
}
