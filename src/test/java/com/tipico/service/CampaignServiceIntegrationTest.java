package com.tipico.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tipico.model.Campaign;
import com.tipico.model.Offer;
import com.tipico.model.condition.CountryCondition;
import com.tipico.model.condition.IsFirstDepositCondition;
import com.tipico.model.condition.MinimumDepositAmountCondition;
import com.tipico.model.condition.RegistrationDateCondition;
import com.tipico.repository.CampaignRepository;
import com.tipico.repository.ConditionRepository;
import com.tipico.repository.OfferRepository;
import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CampaignServiceIntegrationTest {
    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ConditionRepository conditionRepository;

    @Autowired
    private CampaignService testSubject;

    private static final LocalDateTime PAST_DATE =
            LocalDate.of(2020, Month.JANUARY, 1).atStartOfDay();
    private static final LocalDateTime CURRENT_DATE =
            LocalDate.of(2021, Month.JANUARY, 1).atStartOfDay();
    private static final LocalDateTime FUTURE_DATE =
            LocalDate.of(2022, Month.JANUARY, 1).atStartOfDay();

    @AfterEach
    void tearDown() {
        offerRepository.deleteAll();
        conditionRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public CampaignService campaignService(OfferRepository offerRepository) {
            var clock = Clock.fixed(CURRENT_DATE.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

            return new CampaignService(offerRepository, clock);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "9e61690f-28d7-45ee-a200-2aeea6c4969d, POLAND, 2024-01-13T17:09:42.411, 100, true,  1", // All conditions met
        "c0dec0de-c0de-c0de-c0de-badbadbadbad, POLAND, 2024-01-13T17:09:42.411, 100, true,  0", // User has no offers
        "9e61690f-28d7-45ee-a200-2aeea6c4969d, SWEDEN, 2024-01-13T17:09:42.411, 100, true,  0", // Wrong country
        "9e61690f-28d7-45ee-a200-2aeea6c4969d, POLAND, 2019-01-13T17:09:42.411, 100, true,  0", // Registered too early
        "9e61690f-28d7-45ee-a200-2aeea6c4969d, POLAND, 2024-01-13T17:09:42.411, 50,  true,  0", // Below minimum deposit
        "9e61690f-28d7-45ee-a200-2aeea6c4969d, POLAND, 2024-01-13T17:09:42.411, 100, false, 0" //  Not first deposit
    })
    void testGetEligibleOffers(
            UUID customerUuid,
            String country,
            LocalDateTime registrationDate,
            int depositAmount,
            boolean isFirstDeposit,
            int expectedOfferCount) {

        var campaign = new Campaign();
        campaign.setUuid(UUID.randomUUID());
        campaign.setAmount(123);
        campaign.setName("Some Campaign");
        campaign.setStartDate(PAST_DATE);
        campaign.setEndDate(FUTURE_DATE);
        campaign = campaignRepository.save(campaign);

        var offer = new Offer();
        offer.setUuid(UUID.randomUUID());
        offer.setCustomerUuid(UUID.fromString("9e61690f-28d7-45ee-a200-2aeea6c4969d"));
        offer.setExpirationDate(FUTURE_DATE);
        offer.setCampaign(campaign);
        offerRepository.save(offer);

        var countryCondition = new CountryCondition("POLAND");
        countryCondition.setCampaign(campaign);

        var registrationDateCondition = new RegistrationDateCondition(CURRENT_DATE);
        registrationDateCondition.setCampaign(campaign);

        var minimumDepositAmountCondition = new MinimumDepositAmountCondition(100);
        minimumDepositAmountCondition.setCampaign(campaign);

        var isFirstDepositCondition = new IsFirstDepositCondition(true);
        isFirstDepositCondition.setCampaign(campaign);
        conditionRepository.saveAll(List.of(
                countryCondition, registrationDateCondition, minimumDepositAmountCondition, isFirstDepositCondition));

        var eligibleOffers =
                testSubject.getEligibleOffers(customerUuid, country, registrationDate, depositAmount, isFirstDeposit);

        assertEquals(expectedOfferCount, eligibleOffers.size());
    }

    private static Stream<Arguments> unmetOfferExpirationOrCampaignStartEndDates() {
        return Stream.of(
                // Offer expired
                Arguments.of(PAST_DATE, PAST_DATE, FUTURE_DATE),
                // Campaign didn't start yet
                Arguments.of(FUTURE_DATE, FUTURE_DATE, FUTURE_DATE),
                // Campaign is already over
                Arguments.of(FUTURE_DATE, PAST_DATE, PAST_DATE));
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName(
            "Given an offer is found with no conditions but at current time offer expired or campaign is not applicable return empty list")
    void unmetOfferExpirationOrCampaignStartEndDates(
            LocalDateTime offerExpirationDate, LocalDateTime campaignStartDate, LocalDateTime campaignEndDate) {

        var campaign = new Campaign();
        campaign.setUuid(UUID.randomUUID());
        campaign.setAmount(123);
        campaign.setName("Some Campaign");
        campaign.setStartDate(campaignStartDate);
        campaign.setEndDate(campaignEndDate);
        campaign = campaignRepository.save(campaign);

        var offer = new Offer();
        offer.setUuid(UUID.randomUUID());
        offer.setCustomerUuid(UUID.fromString("9e61690f-28d7-45ee-a200-2aeea6c4969d"));
        offer.setExpirationDate(offerExpirationDate);
        offer.setCampaign(campaign);
        offerRepository.save(offer);

        var eligibleOffers = testSubject.getEligibleOffers(offer.getCustomerUuid(), "POLAND", CURRENT_DATE, 100, false);

        assertEquals(0, eligibleOffers.size());
    }
}
