package com.tipico.model.condition;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@DiscriminatorValue("isFirstDeposit")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public non-sealed class IsFirstDepositCondition extends Condition {
    private boolean data;
}
