package com.lidigu.matchingservice.event;

public record RideRequestedEvent(
        String riderId,
        String rideId,
        double pickupLatitude,
        double pickupLongitude,
        String pickUpAddress,
        double dropLatitude,
        double dropLongitude,
        String dropAddress
) {
}
