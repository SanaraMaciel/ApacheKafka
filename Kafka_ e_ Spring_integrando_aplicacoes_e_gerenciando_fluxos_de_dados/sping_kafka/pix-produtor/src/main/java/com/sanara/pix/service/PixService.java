package com.sanara.pix.service;

import com.sanara.pix.dto.PixDTO;
import com.sanara.pix.model.Pix;
import com.sanara.pix.repository.PixRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PixService {

    @Autowired
    private final PixRepository pixRepository;

    //adiciona a injecao de dependencias do kafka no spring p/ o tipo configurado string e pixDTO
    @Autowired
    private final KafkaTemplate<String, PixDTO>  kafkaTemplate;

    public PixDTO salvarPix(PixDTO pixDTO) {
        pixRepository.save(Pix.toEntity(pixDTO));
        //aqui configura tópico, chave e valor a ser enviado
        kafkaTemplate.send("pix-topic", pixDTO.getIdentifier(), pixDTO);
        return pixDTO;
    }

}
