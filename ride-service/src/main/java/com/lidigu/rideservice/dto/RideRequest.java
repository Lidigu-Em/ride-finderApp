package com.lidigu.rideservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RideRequest(
        @NotBlank(message = "Rider id required")
     String riderId,
     @NotNull(message = "pickup latitude required")
     double pickupLatitude,
        @NotNull(message = "pickup longitude required")
     double pickupLongitude,
        @NotNull(message = "pickup address required")
     String pickUpAddress,
        @NotNull(message = "drop latitude required")
     double dropLatitude,
        @NotNull(message = "drop longitude required")
     double dropLongitude,
        @NotNull(message = "drop address required")
     String dropAddress
) {
}
