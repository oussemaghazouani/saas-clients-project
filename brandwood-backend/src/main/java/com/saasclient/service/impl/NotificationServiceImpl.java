package com.saasclient.service.impl;

import com.saasclient.dto.NotificationResponse;
import com.saasclient.entity.Notification;
import com.saasclient.mapper.NotificationMapper;
import com.saasclient.repository.NotificationRepository;
import com.saasclient.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    /** Transaction indépendante : une notification en échec ne casse pas l'opération métier. */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifier(Long userId, String titre, String message, String lien) {
        try {
            repository.save(Notification.builder()
                .userId(userId).titre(titre).message(message).lien(lien).lu(false).build());
        } catch (Exception e) {
            log.warn("Notification non créée pour l'utilisateur {} : {}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> lister(Long userId) {
        return repository.findTop30ByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long compterNonLues(Long userId) {
        return repository.countByUserIdAndLuFalse(userId);
    }

    @Override
    @Transactional
    public void marquerLu(Long userId, Long id) {
        repository.findById(id)
            .filter(n -> n.getUserId().equals(userId))
            .ifPresent(n -> { n.setLu(true); repository.save(n); });
    }

    @Override
    @Transactional
    public void marquerToutLu(Long userId) {
        List<Notification> nonLues = repository.findByUserIdAndLuFalse(userId);
        nonLues.forEach(n -> n.setLu(true));
        repository.saveAll(nonLues);
    }
}
