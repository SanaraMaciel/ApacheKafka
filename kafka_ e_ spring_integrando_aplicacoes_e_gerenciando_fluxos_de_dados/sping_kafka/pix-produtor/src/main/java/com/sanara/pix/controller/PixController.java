package com.sanara.pix.controller;

import com.sanara.pix.dto.PixDTO;
import com.sanara.pix.dto.PixStatus;
import com.sanara.pix.service.PixService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/pix")
@RequiredArgsConstructor
public class PixController {

    private final PixService pixService;

    @PostMapping
    public PixDTO salvarPix(@RequestBody PixDTO pixDTO) {

        pixDTO.setIdentifier(UUID.randomUUID().toString());
        pixDTO.setDataTransferencia(LocalDateTime.now());
        pixDTO.setStatus(PixStatus.EM_PROCESSAMENTO);

        return pixService.salvarPix(pixDTO);
    }
}
