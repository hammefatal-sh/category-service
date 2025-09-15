package com.example.categoryservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class CategoryId {

    @Column(name = "id")
    private Long value;

    protected CategoryId() {
        // JPA용 기본 생성자
    }

    public CategoryId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("CategoryId value must be positive");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "CategoryId{" +
                "value=" + value +
                '}';
    }
}