package com.lidigu.rideservice.service;

import com.lidigu.rideservice.event.RideMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideEventConsumer {

    private final RideService rideService;

    @KafkaListener(
            topics = "ride.matched",
            groupId = "ride-service"
    )
    public void consumeRideMatchedEvent(RideMatchedEvent event){
        rideService.updateRideWithDriver(
                event.rideId(),
                event.driverId()
        );
    }
}
