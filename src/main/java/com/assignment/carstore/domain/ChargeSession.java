package com.assignment.carstore.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity for charging session.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
public class ChargeSession {

    private UUID id;

    private String stationId;

    private LocalDateTime startedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime stoppedAt;

    private StatusEnum status;

    public ChargeSession(String stationId, LocalDateTime startedAt) {
        this.id = UUID.randomUUID();
        this.stationId = stationId;
        this.startedAt = startedAt;
        this.status = StatusEnum.IN_PROGRESS;
    }

    @JsonGetter("id")
    public UUID getId() {
        return id;
    }

    @JsonGetter("stationId")
    public String getStationId() {
        return stationId;
    }

    @JsonGetter("startedAt")
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    @JsonGetter("stoppedAt")
    public LocalDateTime getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(LocalDateTime stoppedAt) {
        this.stoppedAt = stoppedAt;
    }

    @JsonGetter("status")
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChargeSession that = (ChargeSession) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(stationId, that.stationId) &&
                Objects.equals(startedAt, that.startedAt) &&
                Objects.equals(stoppedAt, that.stoppedAt) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stationId, startedAt, stoppedAt, status);
    }
}
