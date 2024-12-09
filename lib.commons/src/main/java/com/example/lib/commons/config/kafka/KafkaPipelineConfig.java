package com.example.lib.commons.config.kafka;



import com.example.lib.commons.bean.ExampleRequest;
import com.example.lib.commons.bean.ExampleResponse;
import com.example.lib.commons.interceptor.ExampleConsumerInterceptor;
import com.example.lib.commons.util.JacksonUtil;
import com.example.lib.commons.worker.ExampleWorker;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.StringUtils;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
public class KafkaPipelineConfig {

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.bootstrap-servers}")
    public String kafkaBootstrapServer;

    private final ConcurrentKafkaListenerContainerFactory<String, ExampleWorker> kafkaListenerContainerFactory;

    private final List<ExampleWorker> workers;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public KafkaPipelineConfig(ConcurrentKafkaListenerContainerFactory<String, ExampleWorker> kafkaListenerContainerFactory, List<ExampleWorker> workers,
                               KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaListenerContainerFactory = kafkaListenerContainerFactory;
        this.workers = workers;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void registerKafkaListeners() {
        workers.forEach(this::registerListenerForWorker);
    }


    private void registerListenerForWorker(ExampleWorker worker) {
        try{
            kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());
            var container = kafkaListenerContainerFactory.createContainer(worker.getTopic());
            container.setupMessageListener((MessageListener<String, ExampleRequest>) data ->
                    kafkaTemplate.executeInTransaction(operations -> {
                        log.info("Received message from topic: {}", worker.getTopic());
                        log.debug("Processing message: {}", data.value());
                        ExampleResponse ExampleResponse = (ExampleResponse) worker.processMessage(data.value());

                        if (StringUtils.hasText(worker.getTopicConfig().getResultStorageTopic())) {
                            log.info("Sending processed message to topic: {}", worker.getTopicConfig().getResultStorageTopic());
                            log.debug("Sending message: {}", ExampleResponse);
                            operations.send(worker.getTopicConfig().getResultStorageTopic(), ExampleResponse);
                        }
                        return ExampleResponse;
                    }));

            if(worker.getTopicConfig().getRetryInterval() > 0L && worker.getTopicConfig().getNumberOfRetriesOnFailure() > 0) {
                FixedBackOff backOff = new FixedBackOff(worker.getTopicConfig().getRetryInterval(), worker.getTopicConfig().getNumberOfRetriesOnFailure());
                CommonErrorHandler errorHandler = new DefaultErrorHandler(
                        new DeadLetterPublishingRecoverer(kafkaTemplate), // Publish to a DLT if retries are exhausted
                        backOff
                );
                log.info("Setting up error handler for topic: {} with max retries: {} and retry interval: {}",
                        worker.getTopic(), worker.getTopicConfig().getNumberOfRetriesOnFailure(), worker.getTopicConfig().getRetryInterval());

                container.setCommonErrorHandler(errorHandler);
            }
            container.setConcurrency(worker.getTopicConfig().getMaxConcurrency());
            container.setBeanName("t-".concat(worker.getTopic()));
            container.start();
        } catch (Exception e) {
            log.error("Error registering listener for worker: {}", worker.getClass().getSimpleName(), e);
        }
    }


    // Configura el ConsumerFactory según tus propiedades de Kafka

    public ConsumerFactory<String, Object> consumerFactory() {
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps());
        consumerFactory.setValueDeserializer(new JsonDeserializer<>(JacksonUtil.mapper));
        return consumerFactory;
    }

    private Map<String, Object> consumerProps() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, ExampleConsumerInterceptor.class.getName());


//        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
//        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
//        props.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
        // ...
        return props;
    }


}

