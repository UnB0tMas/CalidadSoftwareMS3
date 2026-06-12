package com.upsjb.ms3.repository;

import com.upsjb.ms3.domain.entity.EventoEmpleadoMs2Consumido;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoEmpleadoMs2ConsumidoRepository
        extends JpaRepository<EventoEmpleadoMs2Consumido, Long> {

    boolean existsByEventId(UUID eventId);
}