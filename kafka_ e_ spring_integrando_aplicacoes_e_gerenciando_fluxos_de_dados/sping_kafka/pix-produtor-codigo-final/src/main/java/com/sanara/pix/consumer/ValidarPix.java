package com.sanara.pix.consumer;

import com.sanara.pix.avro.Pix;
import com.sanara.pix.dto.PixStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ValidarPix {

    @KafkaListener(topics = "pix-topic-avro", groupId = "group-1")
    public void process(Pix pix) throws JsonProcessingException {
        System.out.println(pix.getChaveDestino());

        if (pix.getValor() > 0) {
            pix.setStatus(PixStatus.PROCESSADO.toString());
        } else  {
            pix.setStatus(PixStatus.ERRO.toString());
        }

    }

}
