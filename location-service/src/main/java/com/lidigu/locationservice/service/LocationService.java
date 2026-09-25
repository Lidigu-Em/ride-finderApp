package com.lidigu.locationservice.service;

import com.lidigu.locationservice.dto.DriverLocationRequest;
import com.lidigu.locationservice.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String DRIVERS_GEO_KEY = "drivers:locations";

    public void  updateDriverLocation(DriverLocationRequest driverLocationRequest){
        log.info("updating location for driver: {}", driverLocationRequest.driverId());

        Point driverPoint = new Point(
               driverLocationRequest.longitude(),
               driverLocationRequest.latitude()
        );

        redisTemplate.opsForGeo().add(
                DRIVERS_GEO_KEY,
                driverPoint,
                driverLocationRequest.driverId()

        );
        log.info("location updated for driver: {}", driverLocationRequest.driverId());
    }

    public List<NearByDriverResponse> findNearByDrivers(
            double latitude,
            double longitude,
            double radiusInKm) {
        log.info("finding drivers near lat: {}  lang: {} within {} km", latitude, longitude, radiusInKm);

        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS)
        );
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(
                DRIVERS_GEO_KEY,
                searchArea,
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates()
                        .includeDistance()
                        .sortAscending()
                        .limit(10)
        );
        List<NearByDriverResponse> nearByDrivers = new ArrayList<>();
        if (results != null){
            results.getContent().forEach(result -> {
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                nearByDrivers.add(new NearByDriverResponse(
                        location.getName(),
                        location.getPoint().getY(),
                        location.getPoint().getX(),
                        result.getDistance().getValue()
                ));
            });
        }
        log.info("found {} drivers nearby ", nearByDrivers.size());
        return nearByDrivers;
    }
    public void removeDriver(String driverId) {
        log.info("removing driver: {}", driverId);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverId);
    }
}
