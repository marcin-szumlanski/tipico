package com.tipico.model.condition;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@DiscriminatorValue("minimumDepositAmount")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public non-sealed class MinimumDepositAmountCondition extends Condition {
    private int data;
}
