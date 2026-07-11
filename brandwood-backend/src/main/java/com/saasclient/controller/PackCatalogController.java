package com.saasclient.controller;

import com.saasclient.dto.PackResponse;
import com.saasclient.service.PackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catalogue des packs actifs, en lecture seule, pour tout utilisateur authentifié. */
@RestController
@RequestMapping("/api/packs")
@RequiredArgsConstructor
public class PackCatalogController {

    private final PackService packService;

    @GetMapping
    public List<PackResponse> catalogue() {
        return packService.listerActifs();
    }
}
