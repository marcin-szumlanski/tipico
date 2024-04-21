package com.tipico.model;

import com.tipico.model.condition.Condition;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private UUID uuid;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int amount;

    @OneToMany(mappedBy = "campaign")
    private List<Offer> offers;

    @OneToMany(mappedBy = "campaign", fetch = FetchType.EAGER)
    private List<Condition> conditions;
}
