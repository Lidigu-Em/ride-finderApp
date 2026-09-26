package com.lidigu.rideservice.event;

public record RideRequestedEvent(

        String riderId,
        String driverId,
        double pickupLatitude,
        double pickupLongitude,
        String pickUpAddress,
        double dropLatitude,
        double dropLongitude,
        String dropAddress
) {
}
