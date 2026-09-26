package com.lidigu.matchingservice.service;

import com.lidigu.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor

public class RideEventConsumer {
    private final MatchingService matchingService;

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent event){
        try {
            matchingService.matchDriverForRide(event);
        }catch (Exception e){
            log.error("Error processing ride request: {} - {}", event.rideId(), e.getMessage());


        }
    }
}
