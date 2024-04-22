package com.tipico.model.condition;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@DiscriminatorValue("registrationDate")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public non-sealed class RegistrationDateCondition extends Condition {
    private LocalDateTime data;
}
