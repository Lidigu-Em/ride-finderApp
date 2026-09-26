package com.lidigu.matchingservice.dto;




public record NearByDriverResponse(
        String driverId,
        double latitude,
        double longitude,
        double distanceInKm
) {
}
