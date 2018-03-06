package com.cybozu.spring.data.jdbc.template.repository;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Table(name = Animal.TABLE_NAME)
public class Animal {
    public static final String TABLE_NAME = "animal";

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Column(name = "scientific_name")
    private String scientificName;

    private Status status;

    @Transient
    private String fieldWithTransientAnnotation;

    private transient String fieldWithTransientModifier;

    public enum Status {
        LC, NT, VU, EN, CR, EW, EX
    }
}
