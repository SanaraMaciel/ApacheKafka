package com.sanara.pix.consumer;

import com.sanara.pix.dto.PixDTO;
import com.sanara.pix.dto.PixStatus;
import com.sanara.pix.model.Key;
import com.sanara.pix.model.Pix;
import com.sanara.pix.repository.KeyRepository;
import com.sanara.pix.repository.PixRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.generic.GenericData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ValidarPix {

    @Autowired
    private PixRepository pixRepository;

    @Autowired
    private KeyRepository keyRepository;

    @KafkaListener(topics = "pix-app.public.pix", groupId = "group-1")
    public void process(GenericData.Record data) throws JsonProcessingException {
        System.out.println(data.get("after").toString());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        PixDTO dto = objectMapper.readValue(data.get("after").toString(), PixDTO.class);

        if (dto.getStatus().equals(PixStatus.EM_PROCESSAMENTO)) {
            Pix pix = pixRepository.findByIdentifier(dto.getIdentifier());

            Key origem = keyRepository.findByChave(dto.getChaveOrigem());
            Key destino = keyRepository.findByChave(dto.getChaveDestino());

            if (origem == null || destino == null) {
                pix.setStatus(PixStatus.ERRO);
            } else {
                pix.setStatus(PixStatus.PROCESSADO);
            }
            pixRepository.save(pix);
        }

    }

}
