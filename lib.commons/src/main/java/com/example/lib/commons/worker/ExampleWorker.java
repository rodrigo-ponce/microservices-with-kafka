package com.example.lib.commons.worker;

import com.example.lib.commons.config.kafka.bean.TopicConfig;

public interface ExampleWorker<T,R> {

    String getTopic();
    TopicConfig getTopicConfig();
    R processMessage(T message) ;
}
