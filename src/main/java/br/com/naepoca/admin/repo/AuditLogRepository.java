package br.com.naepoca.admin.repo;

import br.com.naepoca.admin.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { List<AuditLog> findTop100ByOrderByCriadoEmDesc(); }
