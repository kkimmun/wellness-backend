package com.kh.wellness.plan.model.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.place.model.dto.MapPlaceResponse;
import com.kh.wellness.place.model.dto.PlaceTypeOptionResponse;
import com.kh.wellness.place.model.service.PlaceService;
import com.kh.wellness.plan.model.dto.NearbyPlaceRecommendationResponse;
import com.kh.wellness.plan.model.dto.PlanDetailResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanRecommendationService {

    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final int MAX_PLAN_PLACES = 10;

    private final PlanService planService;
    private final PlaceService placeService;

    public List<NearbyPlaceRecommendationResponse> findNearbyPlaces(
            Long memberNo,
            Double xAxis,
            Double yAxis,
            Long typeNo,
            List<Long> excludePlaceNos) {

        validateMember(memberNo);
        validateCoordinates(xAxis, yAxis);
        validateTypeNo(typeNo);
        Set<Long> excludedPlaceNos = validateExcludedPlaces(excludePlaceNos);

        Map<Long, MapPlaceResponse> activePlaces = activePlacesByPlaceNo();
        Map<Long, PlaceTypeOptionResponse> typesByTypeDetailNo = typesByTypeDetailNo();

        return planService.findNearbyPlaces(memberNo, xAxis, yAxis).stream()
                .filter(place -> place != null && place.getPlaceNo() != null)
                .filter(place -> !excludedPlaceNos.contains(place.getPlaceNo()))
                .filter(place -> activePlaces.containsKey(place.getPlaceNo()))
                .filter(place -> matchesType(place, typeNo, typesByTypeDetailNo))
                .map(place -> toResponse(
                        place,
                        activePlaces.get(place.getPlaceNo()),
                        typesByTypeDetailNo.get(place.getTypeDetailNo()),
                        xAxis,
                        yAxis))
                .sorted(Comparator
                        .comparingLong(NearbyPlaceRecommendationResponse::getDistanceMeters)
                        .thenComparing(NearbyPlaceRecommendationResponse::getPlaceNo))
                .toList();
    }

    private void validateMember(Long memberNo) {
        if (memberNo == null || memberNo <= 0) {
            throw new BadRequestException("회원 정보를 확인해주세요.");
        }
    }

    private void validateCoordinates(Double xAxis, Double yAxis) {
        if (xAxis == null || !Double.isFinite(xAxis) || xAxis < -180 || xAxis > 180) {
            throw new BadRequestException("X 좌표는 -180 이상 180 이하의 값이어야 합니다.");
        }
        if (yAxis == null || !Double.isFinite(yAxis) || yAxis < -90 || yAxis > 90) {
            throw new BadRequestException("Y 좌표는 -90 이상 90 이하의 값이어야 합니다.");
        }
    }

    private void validateTypeNo(Long typeNo) {
        if (typeNo != null && typeNo <= 0) {
            throw new BadRequestException("타입 번호는 1 이상이어야 합니다.");
        }
    }

    private Set<Long> validateExcludedPlaces(List<Long> excludePlaceNos) {
        if (excludePlaceNos == null) {
            return Collections.emptySet();
        }
        if (excludePlaceNos.size() > MAX_PLAN_PLACES) {
            throw new BadRequestException("계획 장소는 최대 10개까지 지정할 수 있습니다.");
        }

        Set<Long> result = new HashSet<>();
        for (Long placeNo : excludePlaceNos) {
            if (placeNo == null || placeNo <= 0) {
                throw new BadRequestException("제외 장소 번호는 1 이상이어야 합니다.");
            }
            result.add(placeNo);
        }
        return result;
    }

    private Map<Long, MapPlaceResponse> activePlacesByPlaceNo() {
        Map<Long, MapPlaceResponse> result = new HashMap<>();
        for (MapPlaceResponse place : placeService.findMapPlaces()) {
            if (place != null && place.getPlaceNo() != null) {
                result.putIfAbsent(place.getPlaceNo(), place);
            }
        }
        return result;
    }

    private Map<Long, PlaceTypeOptionResponse> typesByTypeDetailNo() {
        Map<Long, PlaceTypeOptionResponse> result = new HashMap<>();
        for (PlaceTypeOptionResponse type : placeService.findPlaceTypeOptions()) {
            if (type != null && type.getTypeDetailNo() != null) {
                result.putIfAbsent(type.getTypeDetailNo(), type);
            }
        }
        return result;
    }

    private boolean matchesType(
            PlanDetailResponse place,
            Long requestedTypeNo,
            Map<Long, PlaceTypeOptionResponse> typesByTypeDetailNo) {
        if (requestedTypeNo == null) {
            return true;
        }
        PlaceTypeOptionResponse type = typesByTypeDetailNo.get(place.getTypeDetailNo());
        return type != null && requestedTypeNo.equals(type.getTypeNo());
    }

    private NearbyPlaceRecommendationResponse toResponse(
            PlanDetailResponse nearbyPlace,
            MapPlaceResponse activePlace,
            PlaceTypeOptionResponse type,
            double originX,
            double originY) {

        return NearbyPlaceRecommendationResponse.builder()
                .placeNo(nearbyPlace.getPlaceNo())
                .placeName(firstNonBlank(activePlace.getPlaceName(), nearbyPlace.getPlaceName()))
                .placeDescription(firstNonBlank(
                        activePlace.getPlaceDescription(),
                        nearbyPlace.getPlaceDescription()))
                .addr(firstNonBlank(activePlace.getAddr(), nearbyPlace.getAddr()))
                .addrDetail(firstNonBlank(activePlace.getAddrDetail(), nearbyPlace.getAddrDetail()))
                .xAxis(activePlace.getXAxis())
                .yAxis(activePlace.getYAxis())
                .typeNo(type == null ? null : type.getTypeNo())
                .type(type == null ? activePlace.getType() : type.getType())
                .typeDetailNo(nearbyPlace.getTypeDetailNo())
                .typeDetail(type == null ? activePlace.getTypeDetail() : type.getTypeDetailContent())
                .imageUrl(firstNonBlank(activePlace.getImageUrl(), nearbyPlace.getImgPath()))
                .distanceMeters(calculateDistanceMeters(
                        originX,
                        originY,
                        activePlace.getXAxis(),
                        activePlace.getYAxis()))
                .build();
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred != null && !preferred.isBlank() ? preferred : fallback;
    }

    private long calculateDistanceMeters(double x1, double y1, Double x2, Double y2) {
        if (x2 == null || y2 == null || !Double.isFinite(x2) || !Double.isFinite(y2)) {
            return Long.MAX_VALUE;
        }

        double latitudeDistance = Math.toRadians(y2 - y1);
        double longitudeDistance = Math.toRadians(x2 - x1);
        double firstLatitude = Math.toRadians(y1);
        double secondLatitude = Math.toRadians(y2);
        double haversine = Math.pow(Math.sin(latitudeDistance / 2), 2)
                + Math.cos(firstLatitude)
                * Math.cos(secondLatitude)
                * Math.pow(Math.sin(longitudeDistance / 2), 2);
        double centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        return Math.round(EARTH_RADIUS_METERS * centralAngle);
    }
}
