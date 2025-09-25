package com.wooni.elk.product.repository;

import com.wooni.elk.product.entity.OutboxEvent;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.processed = false ORDER BY o.id ASC")
    List<OutboxEvent> findUnprocessed(Pageable pageable);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.processed = true WHERE o.id IN :ids")
    void markProcessed(@Param("ids") List<Long> ids);
}
