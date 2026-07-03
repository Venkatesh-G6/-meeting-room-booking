package com.yourcompany.roombooking.repository;

import com.yourcompany.roombooking.entity.AuditLog;
import com.yourcompany.roombooking.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByActorEmailOrderByCreatedAtDesc(String actorEmail);

    List<AuditLog> findAllByActionOrderByCreatedAtDesc(AuditAction action);

    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
