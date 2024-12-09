package com.example.mcs.kafka.infra.kafka.worker;

import com.example.lib.commons.bean.Payment;
import com.example.lib.commons.bean.PaymentStatus;
import com.example.lib.commons.config.kafka.bean.TopicConfig;
import com.example.lib.commons.worker.ExampleWorker;
import org.springframework.stereotype.Component;

@Component
public class PaymentWorker implements ExampleWorker<Payment, PaymentStatus> {

    @Override
    public String getTopic() {
        return "payment";
    }

    @Override
    public TopicConfig getTopicConfig() {
        return TopicConfig.builder()
                .resultStorageTopic("payment-status")
                .numberOfRetriesOnFailure(3)
                .retryInterval(1000)
                .maxConcurrency(10)
                .build();
    }

    @Override
    public PaymentStatus processMessage(Payment message) {
        return PaymentStatus.builder()
                .status("SUCCESS")
                .build();
    }
}
