package com.sanara.pix.consumidor;

import com.sanara.pix.dto.PixDTO;
import com.sanara.pix.dto.PixStatus;
import com.sanara.pix.model.Key;
import com.sanara.pix.model.Pix;
import com.sanara.pix.repository.KeyRepository;
import com.sanara.pix.repository.PixRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * implementa o consumidor kafka fazendo a validacao
 */
@Service
public class PixValidator {

    @Autowired
    private KeyRepository keyRepository;

    @Autowired
    private PixRepository pixRepository;

    //instancia o ouvinte para o topico pix-topic a cada atualizacao ele faz uma chamada 
    //um mesmo método pode ouvir mais de um topico
    @KafkaListener(topics = "pix-topic", groupId = "grupo")
    public void processaPix(PixDTO pixDTO) {
        System.out.println("Pix  recebido: " + pixDTO.getIdentifier());

        Pix pix = pixRepository.findByIdentifier(pixDTO.getIdentifier());

        Key origem = keyRepository.findByChave(pixDTO.getChaveOrigem());
        Key destino = keyRepository.findByChave(pixDTO.getChaveDestino());

        if (origem == null || destino == null) {
            pix.setStatus(PixStatus.ERRO);
        } else {
            pix.setStatus(PixStatus.PROCESSADO);
        }
        pixRepository.save(pix);
    }

}
