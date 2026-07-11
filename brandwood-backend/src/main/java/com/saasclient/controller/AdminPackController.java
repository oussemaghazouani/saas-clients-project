package com.saasclient.controller;

import com.saasclient.dto.PackRequest;
import com.saasclient.dto.PackResponse;
import com.saasclient.service.PackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/packs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminPackController {

    private final PackService packService;

    @GetMapping
    public Page<PackResponse> lister(@RequestParam(required = false) Boolean actif,
                                     @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return packService.lister(actif, pageable);
    }

    @GetMapping("/{id}")
    public PackResponse obtenir(@PathVariable Long id) {
        return packService.obtenir(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PackResponse creer(@Valid @RequestBody PackRequest req) {
        return packService.creer(req);
    }

    @PutMapping("/{id}")
    public PackResponse modifier(@PathVariable Long id, @Valid @RequestBody PackRequest req) {
        return packService.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        packService.supprimer(id);
    }
}
