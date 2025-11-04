package com.thehive.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "semantic_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemanticTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "wikidata_id", length = 50)
    private String wikidataId;

    // Relationships
    @ManyToMany(mappedBy = "tags")
    private Set<Offer> offers = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Request> requests = new HashSet<>();
}

