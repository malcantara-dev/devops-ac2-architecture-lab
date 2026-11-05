package org.example.grupo_7_praticaatdd.iot.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "eventos_iot")
public class EventoIotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    private String dispositivoId;
    private String laboratorio;
    private String tipo;
    private Long usuarioId;
    private String ocorridoEm;

    protected EventoIotEntity() {
    }

    public EventoIotEntity(String eventId, String dispositivoId, String laboratorio, String tipo,
                           Long usuarioId, String ocorridoEm) {
        this.eventId = eventId;
        this.dispositivoId = dispositivoId;
        this.laboratorio = laboratorio;
        this.tipo = tipo;
        this.usuarioId = usuarioId;
        this.ocorridoEm = ocorridoEm;
    }

    public String getEventId() {
        return eventId;
    }
}
