package com.example.user_service.model;

import java.util.Objects;

public class Health {
    private HealthStatus status;

    public Health(){}

    public Health(HealthStatus status) {
        this.status = status;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public void setStatus(HealthStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Health health = (Health) o;
        return status == health.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public String toString() {
        return "Health{" +
                "status=" + status +
                '}';
    }
}
