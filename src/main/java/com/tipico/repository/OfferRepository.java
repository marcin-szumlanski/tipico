package com.tipico.repository;

import com.tipico.model.Offer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer>
            findByCustomerUuidAndExpirationDateGreaterThanEqualAndCampaignStartDateLessThanEqualAndCampaignEndDateGreaterThanEqual(
                    UUID customerUuid, LocalDateTime date1, LocalDateTime date2, LocalDateTime date3);

    default List<Offer> findUnexpiredOffersByCustomerUuid(UUID customerUuid, LocalDateTime date) {
        return findByCustomerUuidAndExpirationDateGreaterThanEqualAndCampaignStartDateLessThanEqualAndCampaignEndDateGreaterThanEqual(
                customerUuid, date, date, date);
    }
}
