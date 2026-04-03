package com.zorvyn.finance.entities;

import com.zorvyn.finance.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Column(name = "date_created", nullable = false, updatable = false)
    protected OffsetDateTime dateCreated;

    @Column(name = "date_updated", nullable = false)
    protected OffsetDateTime dateUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    protected EntityStatus status;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.dateCreated = now;
        this.dateUpdated = now;
        if (this.status == null) {
            this.status = EntityStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateUpdated = OffsetDateTime.now(ZoneOffset.UTC);
    }
}