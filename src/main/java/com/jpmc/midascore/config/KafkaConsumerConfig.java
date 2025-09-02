//package com.jpmc.midascore.config;
//
//import com.jpmc.midascore.foundation.Transaction; // <- use your actual package
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.support.serializer.JsonDeserializer;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@EnableKafka
//@Configuration
//public class KafkaConsumerConfig {
//
//    @Bean
//    public ConsumerFactory<String, Transaction> transactionConsumerFactory() {
//        JsonDeserializer<Transaction> valueDeserializer = new JsonDeserializer<>(Transaction.class);
//        valueDeserializer.addTrustedPackages("*"); // allow deserialization of your Transaction package
//
//        Map<String, Object> props = new HashMap<>();
//        // NOTE: For tests, bootstrap servers are auto-wired via spring.embedded.kafka.brokers (no need to set here)
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");  // make sure we read from beginning in tests
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
//
//        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, Transaction> transactionKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, Transaction> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(transactionConsumerFactory());
//        return factory;
//    }
//}
