package com.lidigu.matchingservice.service;

import com.lidigu.matchingservice.client.LocationServiceClient;
import com.lidigu.matchingservice.dto.NearByDriverResponse;
import com.lidigu.matchingservice.event.RideMatchedEvent;
import com.lidigu.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    public void matchDriverForRide(RideRequestedEvent event){
        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearByDrivers(
                event.pickupLatitude(),
                event.pickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM
        );
        if (nearByDrivers.isEmpty()){
            log.warn("No drivers found near ride.");
            return;
        }
        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);

        if (bestDriver.isEmpty()){
            log.warn("could not find suitable driver for ride.");
            return;
        }

        NearByDriverResponse assignedDriver = bestDriver.get();

        RideMatchedEvent matchedEvent = new RideMatchedEvent(
                event.rideId(),
                event.riderId(),
                assignedDriver.driverId(),
                assignedDriver.latitude(),
                assignedDriver.longitude(),
                assignedDriver.distanceInKm()
        );
        kafkaTemplate.send(RIDE_MATCHED_TOPIC, event.rideId(), matchedEvent);
        log.info("RideMatchedEvent published");

    }

    //use greedy algorithm
    private Optional<NearByDriverResponse> findBestDriver(List<NearByDriverResponse> nearByDrivers) {

        double distanceWeight = 0.7;
        double ratingWeight = 0.3;

        return nearByDrivers.stream()
                .max(Comparator.comparingDouble(driver -> {
                    double distanceScore = 1.0/(driver.distanceInKm() + 0.1);

                    double simulatedRating = 4.0 + Math.random();

                    return (distanceScore * distanceWeight) + (simulatedRating * ratingWeight);
                }));
    }
}
