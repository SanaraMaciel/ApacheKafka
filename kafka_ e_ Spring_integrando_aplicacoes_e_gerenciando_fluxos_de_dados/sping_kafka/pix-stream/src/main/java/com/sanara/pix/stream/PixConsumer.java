package com.sanara.pix.stream;

import com.sanara.pix.dto.PixDTO;
import com.sanara.pix.serdes.PixSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PixConsumer {


    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {

        KStream<String, PixDTO> messageStream = streamsBuilder
                .stream("pix-topic", Consumed.with(Serdes.String(), PixSerdes.serdes()))
                .peek((key, value) -> System.out.println("Pix recebido: " + value.getChaveOrigem()))
                .filter((key, value) -> value.getValor() > 1000)
                .peek((key, value) -> System.out.println("Pix: " + key + " será verificado para possível frause"));

        messageStream.print(Printed.toSysOut()); //imprime os pixs que chegam no console
        //envia o pix pra outro topico p/ verificar se nao e fraude
        messageStream.to("pix-verificacao-fraude", Produced.with(Serdes.String(), PixSerdes.serdes()));

        //agrega todos os pagamentos por chave sempre q agrega é kTable
        KTable<String, Double> aggregateStream = streamsBuilder
                .stream("pix-topic-2", Consumed.with(Serdes.String(), PixSerdes.serdes()))
                .peek((key, value) -> System.out.println("Pix recebido: " + value.getChaveOrigem()))
                .filter((key, value) -> value.getValor() != null)
                .groupBy((key, value) -> value.getChaveOrigem())
                .aggregate(
                        () -> 0.0, //valor que vai somar a partir de 0.0
                        (key, value, aggregate) -> (aggregate + value.getValor()),
                        Materialized.with(Serdes.String(), Serdes.Double())//indica onde o kafka vai armazenar a agregacao no caso pix-topic-2
                );


        aggregateStream.toStream().print(Printed.toSysOut());

        //aqui envia o valor agregado para um topico desejado
        aggregateStream.toStream().to("pix-topic-agregacao", Produced.with(Serdes.String(), Serdes.Double()));



    }
}
