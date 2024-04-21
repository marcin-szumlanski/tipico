package com.tipico.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import com.tipico.model.Campaign;
import com.tipico.model.Offer;
import com.tipico.model.condition.*;
import com.tipico.repository.OfferRepository;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignServiceUnitTest {
    @Mock
    private OfferRepository offerRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private CampaignService testSubject;

    private static final UUID CUSTOMER_UUID = UUID.randomUUID();
    private static final UUID OFFER_UUID = UUID.randomUUID();
    private static final String POLAND = "Poland";
    private static final String UTC = "UTC";
    private static final LocalDateTime PAST_DATE =
            LocalDate.of(2020, Month.JANUARY, 1).atStartOfDay();
    private static final LocalDateTime CURRENT_DATE =
            LocalDate.of(2021, Month.JANUARY, 1).atStartOfDay();
    private static final LocalDateTime FUTURE_DATE =
            LocalDate.of(2022, Month.JANUARY, 1).atStartOfDay();
    private static final int DEPOSIT_AMOUNT = 100;
    private static final boolean IS_FIRST_DEPOSIT = true;
    private static final boolean IS_NOT_FIRST_DEPOSIT = false;

    @Test
    @DisplayName("Given no offers were found for user, return empty list")
    void noOffersForUser() {
        mockClock();

        given(offerRepository.findUnexpiredOffersByCustomerUuid(CUSTOMER_UUID, CURRENT_DATE))
                .willReturn(new ArrayList<>());

        var eligibleOffers =
                testSubject.getEligibleOffers(CUSTOMER_UUID, POLAND, CURRENT_DATE, DEPOSIT_AMOUNT, IS_FIRST_DEPOSIT);

        assertEquals(0, eligibleOffers.size());
    }

    @Test
    @DisplayName("Given an offer is found for user and all conditions are met, return it")
    void testGetEligibleOffers_CoveringAllConditions() {
        mockClock();

        var conditions = new ArrayList<>(List.of(
                new CountryCondition(POLAND),
                new MinimumDepositAmountCondition(DEPOSIT_AMOUNT),
                new RegistrationDateCondition(CURRENT_DATE),
                new IsFirstDepositCondition(IS_FIRST_DEPOSIT)));

        Campaign campaign = new Campaign();
        campaign.setStartDate(PAST_DATE);
        campaign.setEndDate(FUTURE_DATE);
        campaign.setConditions(conditions);

        Offer offer = new Offer();
        offer.setUuid(OFFER_UUID);
        offer.setCustomerUuid(CUSTOMER_UUID);
        offer.setExpirationDate(FUTURE_DATE);
        offer.setCampaign(campaign);

        given(offerRepository.findUnexpiredOffersByCustomerUuid(CUSTOMER_UUID, CURRENT_DATE))
                .willReturn(new ArrayList<>(List.of(offer)));

        var eligibleOffers =
                testSubject.getEligibleOffers(CUSTOMER_UUID, POLAND, CURRENT_DATE, DEPOSIT_AMOUNT, IS_FIRST_DEPOSIT);

        assertEquals(1, eligibleOffers.size());
        assertEquals(OFFER_UUID, eligibleOffers.getFirst().uuid());
    }

    private static Stream<List<Condition>> oneUnmetCondition() {
        return Stream.of(
                // Wrong country
                List.of(
                        new CountryCondition("SWEDEN"),
                        new MinimumDepositAmountCondition(DEPOSIT_AMOUNT),
                        new RegistrationDateCondition(CURRENT_DATE),
                        new IsFirstDepositCondition(IS_NOT_FIRST_DEPOSIT)),
                // Below minimum deposit
                List.of(
                        new CountryCondition(POLAND),
                        new MinimumDepositAmountCondition(50),
                        new RegistrationDateCondition(CURRENT_DATE),
                        new IsFirstDepositCondition(IS_NOT_FIRST_DEPOSIT)),
                // Registered too early
                List.of(
                        new CountryCondition(POLAND),
                        new MinimumDepositAmountCondition(DEPOSIT_AMOUNT),
                        new RegistrationDateCondition(PAST_DATE),
                        new IsFirstDepositCondition(IS_NOT_FIRST_DEPOSIT)),
                // Not first deposit
                List.of(
                        new CountryCondition(POLAND),
                        new MinimumDepositAmountCondition(DEPOSIT_AMOUNT),
                        new RegistrationDateCondition(CURRENT_DATE),
                        new IsFirstDepositCondition(IS_FIRST_DEPOSIT)));
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Given an offer is found and one condition is not met, return empty list")
    void oneUnmetCondition(List<Condition> conditions) {
        mockClock();

        Campaign campaign = new Campaign();
        campaign.setStartDate(PAST_DATE);
        campaign.setEndDate(FUTURE_DATE);
        campaign.setConditions(conditions);

        Offer offer = new Offer();
        offer.setUuid(OFFER_UUID);
        offer.setCustomerUuid(CUSTOMER_UUID);
        offer.setExpirationDate(FUTURE_DATE);
        offer.setCampaign(campaign);

        given(offerRepository.findUnexpiredOffersByCustomerUuid(CUSTOMER_UUID, CURRENT_DATE))
                .willReturn(new ArrayList<>(List.of(offer)));

        var eligibleOffers = testSubject.getEligibleOffers(
                CUSTOMER_UUID, POLAND, CURRENT_DATE, DEPOSIT_AMOUNT, IS_NOT_FIRST_DEPOSIT);

        assertEquals(0, eligibleOffers.size());
    }

    private void mockClock() {
        given(clock.instant()).willReturn(CURRENT_DATE.toInstant(ZoneOffset.UTC));
        given(clock.getZone()).willReturn(ZoneId.of(UTC));
    }
}
