package com.tipico.dataseed;

import com.tipico.model.Campaign;
import com.tipico.model.Offer;
import com.tipico.model.condition.CountryCondition;
import com.tipico.model.condition.IsFirstDepositCondition;
import com.tipico.model.condition.MinimumDepositAmountCondition;
import com.tipico.model.condition.RegistrationDateCondition;
import com.tipico.repository.CampaignRepository;
import com.tipico.repository.ConditionRepository;
import com.tipico.repository.OfferRepository;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampaignDataSeeder implements CommandLineRunner {

    private final OfferRepository offerRepository;

    private final CampaignRepository campaignRepository;

    private final ConditionRepository conditionRepository;

    @Override
    public void run(String... args) {
        loadCampaignData();
    }

    private void loadCampaignData() {
        var campaign = new Campaign();
        campaign.setUuid(UUID.randomUUID());
        campaign.setAmount(123);
        campaign.setName("Superb Campaign");
        campaign.setStartDate(LocalDate.of(2020, Month.JANUARY, 1).atStartOfDay());
        campaign.setEndDate(LocalDate.of(2026, Month.DECEMBER, 31).atStartOfDay());
        campaign = campaignRepository.save(campaign);

        var offer = new Offer();
        offer.setUuid(UUID.randomUUID());
        offer.setCustomerUuid(UUID.fromString("9e61690f-28d7-45ee-a200-2aeea6c4969d"));
        offer.setExpirationDate(LocalDate.of(2025, Month.JANUARY, 1).atStartOfDay());
        offer.setCampaign(campaign);
        offerRepository.save(offer);

        var countryCondition = new CountryCondition("POLAND");
        countryCondition.setCampaign(campaign);

        var registrationDateCondition = new RegistrationDateCondition(
                LocalDate.of(2021, Month.JANUARY, 1).atStartOfDay());
        registrationDateCondition.setCampaign(campaign);

        var minimumDepositAmountCondition = new MinimumDepositAmountCondition(100);
        minimumDepositAmountCondition.setCampaign(campaign);

        var isFirstDepositCondition = new IsFirstDepositCondition(true);
        isFirstDepositCondition.setCampaign(campaign);
        conditionRepository.saveAll(List.of(
                countryCondition, registrationDateCondition, minimumDepositAmountCondition, isFirstDepositCondition));
    }
}
