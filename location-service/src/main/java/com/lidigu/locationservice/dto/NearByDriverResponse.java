package com.lidigu.locationservice.dto;

public record NearByDriverResponse(
        String driverId,
        double latitude,
        double longitude,
        double distanceInKm
) {
}
