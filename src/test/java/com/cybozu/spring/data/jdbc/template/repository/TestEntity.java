package com.cybozu.spring.data.jdbc.template.repository;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Table(name = "test")
public class TestEntity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    private Status status;

    public enum Status {
        GOOD, BAD
    }
}
