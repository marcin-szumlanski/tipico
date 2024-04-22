package com.tipico.service;

import com.tipico.dto.OfferDto;
import com.tipico.model.*;
import com.tipico.model.condition.*;
import com.tipico.repository.OfferRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final OfferRepository offerRepository;

    private final Clock clock;

    @Transactional
    public List<OfferDto> getEligibleOffers(
            UUID customerUuid,
            String country,
            LocalDateTime registrationDate,
            int depositAmount,
            boolean isFirstDeposit) {
        var now = currentTime();

        List<Offer> offers = offerRepository.findUnexpiredOffersByCustomerUuid(customerUuid, now);

        log.info("Found {} potential offers for customer {}", offers.size(), customerUuid);

        offers.removeIf(offer -> !validateConditions(
                offer.getCampaign().getConditions(), country, registrationDate, depositAmount, isFirstDeposit));

        log.info("Found {} eligible offers for customer {}", offers.size(), customerUuid);

        return offers.stream().map(offer -> new OfferDto(offer.getUuid())).toList();
    }

    private boolean validateConditions(
            List<Condition> conditions,
            String country,
            LocalDateTime registrationDate,
            int depositAmount,
            boolean isFirstDeposit) {
        return conditions.stream()
                .allMatch(condition ->
                        isConditionSatisfied(condition, country, registrationDate, depositAmount, isFirstDeposit));
    }

    private boolean isConditionSatisfied(
            Condition condition,
            String country,
            LocalDateTime registrationDate,
            int depositAmount,
            boolean isFirstDeposit) {
        return switch (condition) {
            case CountryCondition c -> country.equalsIgnoreCase(c.getData());
            case RegistrationDateCondition c -> registrationDate.isEqual(c.getData())
                    || registrationDate.isAfter(c.getData());
            case MinimumDepositAmountCondition c -> depositAmount >= c.getData();
            case IsFirstDepositCondition c -> c.isData() && isFirstDeposit;
        };
    }

    private LocalDateTime currentTime() {
        return LocalDateTime.now(clock);
    }
}
;
