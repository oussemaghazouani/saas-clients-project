package com.saasclient.service.impl;

import com.saasclient.dto.PackRequest;
import com.saasclient.dto.PackResponse;
import com.saasclient.entity.Pack;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.PackMapper;
import com.saasclient.repository.PackRepository;
import com.saasclient.service.PackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackServiceImpl implements PackService {

    private final PackRepository packRepository;
    private final PackMapper packMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PackResponse> lister(Boolean actif, Pageable pageable) {
        Page<Pack> page = (actif == null)
            ? packRepository.findAll(pageable)
            : packRepository.findByActif(actif, pageable);
        return page.map(packMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackResponse> listerActifs() {
        return packRepository.findByActifTrue().stream()
            .map(packMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PackResponse obtenir(Long id) {
        return packMapper.toResponse(getPack(id));
    }

    @Override
    @Transactional
    public PackResponse creer(PackRequest req) {
        if (packRepository.existsByNomIgnoreCase(req.getNom())) {
            throw new BusinessRuleException("Un pack porte déjà ce nom.", HttpStatus.CONFLICT);
        }
        return packMapper.toResponse(packRepository.save(packMapper.toEntity(req)));
    }

    @Override
    @Transactional
    public PackResponse modifier(Long id, PackRequest req) {
        Pack pack = getPack(id);
        if (!pack.getNom().equalsIgnoreCase(req.getNom())
                && packRepository.existsByNomIgnoreCase(req.getNom())) {
            throw new BusinessRuleException("Un pack porte déjà ce nom.", HttpStatus.CONFLICT);
        }
        packMapper.updateEntity(pack, req);
        return packMapper.toResponse(packRepository.save(pack));
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        packRepository.delete(getPack(id));
    }

    private Pack getPack(Long id) {
        return packRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pack introuvable."));
    }
}
