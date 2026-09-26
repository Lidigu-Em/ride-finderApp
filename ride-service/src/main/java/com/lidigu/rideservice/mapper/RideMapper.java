package com.lidigu.rideservice.mapper;

import com.lidigu.rideservice.dto.RideResponse;
import com.lidigu.rideservice.model.Ride;
import org.springframework.stereotype.Service;

@Service
public class RideMapper {

    public RideResponse mapToResponse(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getRiderId(),
                ride.getDriverId(),
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                ride.getPickUpAddress(),
                ride.getDropLatitude(),
                ride.getDropLongitude(),
                ride.getDropAddress(),
                ride.getStatus(),
                ride.getEstimatedFare(),
                ride.getActualFare(),
                ride.getCreatedAt(),
                ride.getUpdatedAt(),
                ride.getStartedAt(),
                ride.getCompletedAt()
        );
    }
}
