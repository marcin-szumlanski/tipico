package com.tipico.controller;

import com.tipico.dto.OfferDto;
import com.tipico.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @Operation(summary = "Get eligible offers for a customer")
    @ApiResponse(
            responseCode = "200",
            description = "List of eligible offers",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferDto.class)))
    @GetMapping("/eligible-offers")
    public ResponseEntity<List<OfferDto>> getEligibleOffers(
            @Parameter(description = "Customer UUID", example = "9e61690f-28d7-45ee-a200-2aeea6c4969d") @RequestParam
                    UUID customerUuid,
            @Parameter(description = "Country", example = "POLAND") @RequestParam String country,
            @Parameter(description = "Registration Date", example = "2024-01-13T17:09:42.411") @RequestParam
                    LocalDateTime registrationDate,
            @Parameter(description = "Deposit Amount in Euros", example = "100") @RequestParam int depositAmount,
            @Parameter(description = "Is First Deposit", example = "true") @RequestParam boolean isFirstDeposit) {
        var eligibleOffers = campaignService.getEligibleOffers(
                customerUuid, country, registrationDate, depositAmount, isFirstDeposit);

        return ResponseEntity.ok(eligibleOffers);
    }
}
