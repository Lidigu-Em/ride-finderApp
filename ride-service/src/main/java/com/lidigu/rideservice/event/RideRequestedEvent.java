package com.lidigu.rideservice.event;

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
