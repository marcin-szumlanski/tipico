package com.tipico.model.condition;

import com.tipico.model.Campaign;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Proxy;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Proxy(lazy = false)
@Getter
@Setter
public abstract sealed class Condition
        permits CountryCondition, RegistrationDateCondition, IsFirstDepositCondition, MinimumDepositAmountCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
}
