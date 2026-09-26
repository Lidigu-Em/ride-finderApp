package com.lidigu.rideservice.dto;

import com.lidigu.rideservice.model.RideStatus;

import java.time.LocalDateTime;

public record RideResponse(
         String id,
         String riderId,
         String driverId,
         double pickupLatitude,
         double pickupLongitude,
         String pickUpAddress,
         double dropLatitude,
         double dropLongitude,
         String dropAddress,
         RideStatus status,
         double estimatedFare,
         double actualFare,
         LocalDateTime createdAt,
         LocalDateTime updatedAt,
         LocalDateTime startedAt,
         LocalDateTime completedAt
) {
}
