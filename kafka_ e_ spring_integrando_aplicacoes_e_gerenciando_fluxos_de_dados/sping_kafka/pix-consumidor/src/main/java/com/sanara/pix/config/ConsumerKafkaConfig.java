package com.sanara.pix.config;

import com.sanara.pix.dto.PixDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * classe de configuração do consumidor kafka
 * BOOTSTRAP_SERVERS_CONFIG(bootstrapAddress) -- configuração do kafka que vamos acessar
 * KEY_DESERIALIZER_CLASS_CONFIG(StringDeserializer.class) -- deserializador da classe
 * VALUE_DESERIALIZER_CLASS_CONFIG(JsonSerializer.class) -- classe objeto que vamos deserializar p/ o retorno
 * JsonDeserializer.TRUSTED_PACKAGES("*") -- conf. p/ verificar se a aplicacao esta dentro de um pacote confiavel, nesse caso o * confia em tudo
 * mas vc pode colocar somente p/ deserializar dentro de seus pacotes dto por ex: sanara.pix.dto
 */
@Configuration
public class ConsumerKafkaConfig {

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

    /**
     * consumidor kafka p/ deserializar p/ objetos java
     *
     * @return
     */
    @Bean
    public ConsumerFactory<? super String, ? super String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "*");
        //confighuração pra melhorar o desempenho da aplicação
        //ele vai no kafka pega 100 msgs e dps retorna pra processar
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

        //indica a partir de quando vai consumir as msgs
        //earliest -- pega desde a mais antiga latest-- pega desde a mais recente
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

        //deixa criar tópicos caso ele ainda não exista true deixa false não cria o topico automaticamente
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,false);

        //qdo chega uma msg no consumidor ele faz o commit automaticamente isso faz com q o kafka não distribua
        //a msg dps dela ter sido enviada, se vc quiser fazer o controle desse commit pelo código vc habilita como false
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);


        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

}
