package com.sanara.pix.consumidor;

import com.sanara.pix.dto.PixDTO;
import com.sanara.pix.dto.PixStatus;
import com.sanara.pix.exception.KeyNotFoundException;
import com.sanara.pix.model.Key;
import com.sanara.pix.model.Pix;
import com.sanara.pix.repository.KeyRepository;
import com.sanara.pix.repository.PixRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
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
    // a propriedade acknowledgment é utilizada p/ controlar onde o commit da msg vai ser feito
    @KafkaListener(topics = "pix-topic", groupId = "grupo")
    //notação usada para informar ao kafka qie se houver erro ao enviar é pra ele fazer uma nova retentativa
    //para um novo tópico
    //backoff: indica qto tempo depois do erro ele vai começar a fazer a retentativa
    //attempts: indica qtas vezes ele vai tentar reprocessar a msg o padrão é 3
    //autoCreateTopics: se o kafka vai criar os tópicos automaticamente
    //include: indicam as exceções que querem ter retentativas ou não no caso nosso colocamos para nossas exceções
    @RetryableTopic(
            backoff = @Backoff(value = 3000L),
            attempts = "5",
            autoCreateTopics = "true",
            include = KeyNotFoundException.class
    )
    public void processaPix(PixDTO pixDTO, Acknowledgment acknowledgment) {
    //public void processaPix(PixDTO pixDTO) {

        System.out.println("Pix  recebido: " + pixDTO.getIdentifier());

        Pix pix = pixRepository.findByIdentifier(pixDTO.getIdentifier());

        Key origem = keyRepository.findByChave(pixDTO.getChaveOrigem());
        Key destino = keyRepository.findByChave(pixDTO.getChaveDestino());

        if (origem == null || destino == null) {
            pix.setStatus(PixStatus.ERRO);
            throw new KeyNotFoundException();
        } else {
            pix.setStatus(PixStatus.PROCESSADO);
        }
        pixRepository.save(pix);

        //por padrão o kafka já faz o commit no final qdo a configuração está como true
        acknowledgment.acknowledge();
    }

}
