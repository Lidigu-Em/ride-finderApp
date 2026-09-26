package com.lidigu.rideservice.service;

import com.lidigu.rideservice.dto.RideRequest;
import com.lidigu.rideservice.dto.RideResponse;
import com.lidigu.rideservice.event.RideRequestedEvent;
import com.lidigu.rideservice.mapper.RideMapper;
import com.lidigu.rideservice.model.Ride;
import com.lidigu.rideservice.model.RideStatus;
import com.lidigu.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;
    private final RideMapper mapper;

    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";
    private static final double EARTH_RADIUS_KM = 6371.0;
    @Value("${ride.pricing.base-fare}")
    private double baseFare;

    @Value("${ride.pricing.price-per-km}")
    private double pricePerKm;



    public RideResponse requestRide(RideRequest rideRequest) {
        log.info("new ride request from rider: {}", rideRequest.riderId());

        Ride ride = new Ride();
        ride.setRiderId(rideRequest.riderId());
        ride.setPickupLatitude(rideRequest.pickupLatitude());
        ride.setPickupLongitude(rideRequest.pickupLongitude());
        ride.setPickUpAddress(rideRequest.pickUpAddress());
        ride.setDropLatitude(rideRequest.dropLatitude());
        ride.setDropLongitude(rideRequest.dropLongitude());
        ride.setDropAddress(rideRequest.dropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(rideRequest));

        Ride savedRide = rideRepository.save(ride);

        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getDriverId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickUpAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );
        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent published to kafka for ride: {}", savedRide.getId());

        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);

        return mapper.mapToResponse(savedRide);

    }

    //use haversine formula to calculate distance and price per longitude and latitude
    private double calculateEstimateFare(RideRequest request) {
        double lat1 = Math.toRadians(request.pickupLatitude());
        double lat2 = Math.toRadians(request.dropLatitude());

        double long1 = Math.toRadians(request.pickupLongitude());
        double long2 = Math.toRadians(request.dropLongitude());

        double deltaLat = lat2 - lat1;
        double deltaLong = long2 - long1;

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1)
                * Math.cos(lat2)
                * Math.pow(Math.sin(deltaLong / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distanceKm = EARTH_RADIUS_KM * c;

        return baseFare + (distanceKm * pricePerKm);
    }

    public void updateRideWithDriver(String rideId, String driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);

    }

    public RideResponse startRide(String rideId) throws RuntimeException {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.ACCEPTED)
            throw new RuntimeException("Ride cannot be started, current status: " + ride.getStatus());
        ride.setStatus(RideStatus.RIDE_STATED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);

        return mapper.mapToResponse(ride);

    }

    public RideResponse completeRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.RIDE_STATED) {
            throw new RuntimeException("Ride cannot be completed, current status: " + ride.getStatus());
        }
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);

        return mapper.mapToResponse(ride);

    }

    public RideResponse cancelRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
        return mapper.mapToResponse(ride);
    }


    public RideResponse getRideById(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        return mapper.mapToResponse(ride);
    }

    public List<RideResponse> getRidesByRider(String riderId) {
        List<Ride> rides = rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId);

        return rides.stream()
                .map(mapper::mapToResponse)
                .toList();
    }
}
