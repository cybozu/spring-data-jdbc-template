package com.cybozu.spring.data.jdbc.template.repository;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.cybozu.spring.data.jdbc.template.entity.EntityCallback;
import com.cybozu.spring.data.jdbc.template.repository.Animal.Status;

@Getter
@Setter
@ToString
@Table(name = AnimalWithCallback.TABLE_NAME)
public class AnimalWithCallback implements EntityCallback {
    public static final String TABLE_NAME = Animal.TABLE_NAME;

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Column(name = "scientific_name")
    private String scientificName;

    private Status status;
}
