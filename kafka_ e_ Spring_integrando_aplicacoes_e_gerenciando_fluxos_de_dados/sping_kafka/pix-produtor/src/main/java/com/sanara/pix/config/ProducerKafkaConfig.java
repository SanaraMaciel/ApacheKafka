package com.sanara.pix.config;

import com.sanara.pix.dto.PixDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * classe para armazenar e informar as configuracoes do kafka
 * BOOTSTRAP_SERVERS_CONFIG (bootstrapAddress) -- informa o endereco do servidor kafka
 * KEY_SERIALIZER_CLASS_CONFIG (StringSerializer.class) -- tipo de serializacao que sera feito
 * VALUE_SERIALIZER_CLASS_CONFIG (JsonSerializer.class) -- objeto que vai retornar um json ex:pixDTO
 */
@Configuration
public class ProducerKafkaConfig {


    @Value(value = "${spring.kafka.bootstrap-servers:localhost:9082}")
    private String bootstrapAddress;

    @Bean
    public ProducerFactory<String, PixDTO> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PixDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
