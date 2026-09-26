package com.lidigu.matchingservice.event;

public record RideMatchedEvent(
     String  rideId,
     String riderId,
     String driverId,
     double driverLatitude,
     double driverLongitude,
     double distanceToPickupKm

) {
}
