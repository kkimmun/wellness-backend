package com.kh.wellness.route.model.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.route.model.dao.RouteMapper;
import com.kh.wellness.route.model.dto.CoordinateResponse;
import com.kh.wellness.route.model.dto.OriginSearchResponse;
import com.kh.wellness.route.model.dto.PlaceResponse;
import com.kh.wellness.route.model.dto.RouteResponse;
import com.kh.wellness.route.model.dto.RouteResultResponse;
import com.kh.wellness.route.model.dto.RouteSearchRequest;
import com.kh.wellness.route.model.dto.RouteStepResponse;
import com.kh.wellness.route.model.vo.Place;
import com.kh.wellness.route.model.vo.RouteOption;
import com.kh.wellness.route.model.vo.TransitSortType;
import com.kh.wellness.route.model.vo.TransitType;
import com.kh.wellness.route.model.vo.TransportType;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private static final List<String> BICYCLE_TIME_ROUTE_MODES = List.of(
            "BIKE_ONLY",
            "SHORTEST",
            "ACCESSIBLE"
    );

    private final RouteMapper routeMapper;
    private final KakaoRouteClient kakaoRouteClient;

    public RouteResponse findRoutes(RouteSearchRequest request) {
        TransportType transportType = parseTransportType(request.getTransportType());
        Place origin = resolvePlace(
                request.getStartPlaceNo(),
                request.getStartX(),
                request.getStartY(),
                "출발",
                "현재 위치"
        );
        Place destination = resolvePlace(
                request.getEndPlaceNo(),
                request.getEndX(),
                request.getEndY(),
                "도착",
                "지정 도착지"
        );

        if (sameCoordinates(origin, destination)) {
            throw new BadRequestException("출발지와 도착지가 같습니다.");
        }

        List<Place> waypoints = resolveWaypoints(
                request.getWaypointPlaceNos(),
                transportType
        );

        return switch (transportType) {
            case CAR -> findCarRoutes(request, origin, destination);
            case PUBLIC_TRANSIT -> findPublicTransitRoutes(request, origin, destination);
            case BICYCLE -> findBicycleRoutes(request, origin, destination);
            case WALK -> findWalkingRoutes(request, origin, destination, waypoints);
        };
    }

    public List<OriginSearchResponse> searchOrigins(String query) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("검색어는 필수입니다.");
        }

        return routeMapper.findPlacesByQuery(query.trim())
                .stream()
                .map(place -> OriginSearchResponse.builder()
                        .placeNo(place.getPlaceNo())
                        .placeName(place.getPlaceName())
                        .address(place.getAddress())
                        .xAxis(place.getXAxis())
                        .yAxis(place.getYAxis())
                        .build())
                .toList();
    }

    private RouteResponse findCarRoutes(
            RouteSearchRequest request,
            Place origin,
            Place destination) {
        RouteOption option = parseCarOption(request.getRouteOption());
        String priority = switch (option) {
            case MIN_DISTANCE -> "DISTANCE";
            case MIN_TIME -> "TIME";
            case AVOID_TOLL -> "RECOMMEND";
            default -> throw invalidRouteOption(TransportType.CAR);
        };
        String avoid = option == RouteOption.AVOID_TOLL ? "toll" : null;

        JsonNode response = kakaoRouteClient.findCarRoute(
                origin.getXAxis(),
                origin.getYAxis(),
                destination.getXAxis(),
                destination.getYAxis(),
                priority,
                avoid
        );

        List<RouteResultResponse> routes = mapCarRoutes(response);
        return buildResponse(TransportType.CAR, option.name(), origin, destination, routes);
    }

    private RouteResponse findPublicTransitRoutes(
            RouteSearchRequest request,
            Place origin,
            Place destination) {
        TransitType transitType = parseOptionalTransitType(request.getTransitType());
        TransitSortType sortType = parseTransitSortType(request.getSortType());

        JsonNode response = kakaoRouteClient.findPublicTransitRoutes(
                origin.getXAxis(),
                origin.getYAxis(),
                destination.getXAxis(),
                destination.getYAxis()
        );

        validateTransitStatus(response);
        List<RouteResultResponse> routes = mapAndSortTransitRoutes(response, transitType, sortType);
        if (routes.isEmpty()) {
            throw new NotFoundException("조건에 맞는 대중교통 경로를 찾을 수 없습니다.");
        }

        return buildResponse(
                TransportType.PUBLIC_TRANSIT,
                sortType.name(),
                origin,
                destination,
                routes
        );
    }

    private RouteResponse findBicycleRoutes(
            RouteSearchRequest request,
            Place origin,
            Place destination) {
        RouteOption option = parseBicycleOption(request.getRouteOption());
        RouteResultResponse route;

        if (option == RouteOption.MIN_TIME) {
            route = findFastestBicycleRoute(origin, destination);
        } else {
            String routeMode = option == RouteOption.MIN_DISTANCE ? "SHORTEST" : "ACCESSIBLE";
            JsonNode response = kakaoRouteClient.findBicycleRoute(
                    origin.getXAxis(),
                    origin.getYAxis(),
                    destination.getXAxis(),
                    destination.getYAxis(),
                    routeMode
            );
            route = mapKakaoMapRoute(response, TransportType.BICYCLE.name());
        }

        return buildResponse(
                TransportType.BICYCLE,
                option.name(),
                origin,
                destination,
                List.of(route)
        );
    }

    private RouteResponse findWalkingRoutes(
            RouteSearchRequest request,
            Place origin,
            Place destination,
            List<Place> waypoints) {
        RouteOption option = parseWalkOption(request.getRouteOption());
        String routeMode = switch (option) {
            case SHORTEST -> "SHORTEST";
            case BROAD_FIRST -> "BROAD_FIRST";
            case ACCESSIBLE -> "ACCESSIBLE";
            default -> throw invalidRouteOption(TransportType.WALK);
        };

        JsonNode response = kakaoRouteClient.findWalkingRoute(
                origin.getXAxis(),
                origin.getYAxis(),
                destination.getXAxis(),
                destination.getYAxis(),
                routeMode,
                waypoints
        );
        RouteResultResponse route = mapKakaoMapRoute(response, TransportType.WALK.name());

        return buildResponse(
                TransportType.WALK,
                option.name(),
                origin,
                destination,
                waypoints,
                List.of(route)
        );
    }

    private RouteResultResponse findFastestBicycleRoute(
            Place origin,
            Place destination) {
        List<RouteResultResponse> candidates = new ArrayList<>();

        for (String routeMode : BICYCLE_TIME_ROUTE_MODES) {
            try {
                JsonNode response = kakaoRouteClient.findBicycleRoute(
                        origin.getXAxis(),
                        origin.getYAxis(),
                        destination.getXAxis(),
                        destination.getYAxis(),
                        routeMode
                );
                candidates.add(mapKakaoMapRoute(response, TransportType.BICYCLE.name()));
            } catch (NotFoundException ignored) {
                // 해당 탐색 방식에서 경로가 없으면 나머지 경로를 비교한다.
            }
        }

        return candidates.stream()
                .min(Comparator.comparingInt(RouteResultResponse::getTotalTime))
                .orElseThrow(() -> new NotFoundException("자전거 경로를 찾을 수 없습니다."));
    }

    private List<RouteResultResponse> mapCarRoutes(JsonNode response) {
        List<RouteResultResponse> results = new ArrayList<>();

        for (JsonNode route : response.path("routes")) {
            if (route.path("result_code").asInt(-1) != 0) {
                continue;
            }

            JsonNode summary = route.path("summary");
            List<RouteStepResponse> steps = new ArrayList<>();
            List<CoordinateResponse> path = new ArrayList<>();

            for (JsonNode section : route.path("sections")) {
                for (JsonNode road : section.path("roads")) {
                    addFlatCoordinates(path, road.path("vertexes"));
                }
                for (JsonNode guide : section.path("guides")) {
                    steps.add(RouteStepResponse.builder()
                            .type(String.valueOf(guide.path("type").asInt()))
                            .guidance(guide.path("guidance").asString())
                            .distance(guide.path("distance").asInt())
                            .time(guide.path("duration").asInt())
                            .path(List.of(CoordinateResponse.builder()
                                    .xAxis(guide.path("x").asDouble())
                                    .yAxis(guide.path("y").asDouble())
                                    .build()))
                            .build());
                }
            }

            results.add(RouteResultResponse.builder()
                    .routeType(TransportType.CAR.name())
                    .totalDistance(summary.path("distance").asInt())
                    .totalTime(summary.path("duration").asInt())
                    .toll(summary.path("fare").path("toll").asInt())
                    .steps(steps)
                    .path(path)
                    .build());
        }

        if (results.isEmpty()) {
            throw new NotFoundException("자동차 경로를 찾을 수 없습니다.");
        }

        return results;
    }

    private List<RouteResultResponse> mapAndSortTransitRoutes(
            JsonNode response,
            TransitType selectedType,
            TransitSortType sortType) {
        List<RouteResultResponse> mappedRoutes = new ArrayList<>();

        for (JsonNode route : response.path("routes")) {
            JsonNode properties = route.path("properties");
            String routeType = properties.path("type").asString();
            if (!isTransitType(routeType)) {
                continue;
            }

            List<RouteStepResponse> steps = new ArrayList<>();
            List<CoordinateResponse> path = new ArrayList<>();
            int walkingDistance = 0;

            for (JsonNode step : route.path("steps")) {
                JsonNode stepProperties = step.path("properties");
                String stepType = stepProperties.path("type").asString();
                if ("WALKING".equalsIgnoreCase(stepType)) {
                    walkingDistance += stepProperties.path("distance").asInt();
                }

                List<CoordinateResponse> stepPath = mapPointCoordinates(step.path("path").path("points"));
                path.addAll(stepPath);
                steps.add(RouteStepResponse.builder()
                        .type(stepType)
                        .guidance(stepProperties.path("guidance").asString())
                        .distance(stepProperties.path("distance").asInt())
                        .time(stepProperties.path("time").asInt())
                        .stopNames(mapNames(stepProperties.path("stops"), "name"))
                        .vehicleNames(mapNames(stepProperties.path("vehicles"), "name"))
                        .path(stepPath)
                        .build());
            }

            mappedRoutes.add(RouteResultResponse.builder()
                    .routeType(routeType)
                    .totalDistance(properties.path("totalDistance").asInt())
                    .totalTime(properties.path("totalTime").asInt())
                    .transfers(properties.path("transfers").asInt())
                    .walkingDistance(walkingDistance)
                    .fare(properties.path("fare").path("value").asInt())
                    .steps(steps)
                    .path(path)
                    .build());
        }

        List<RouteResultResponse> sortedRoutes = new ArrayList<>();
        for (TransitType transitType : TransitType.values()) {
            if (selectedType != null && transitType != selectedType) {
                continue;
            }

            mappedRoutes.stream()
                    .filter(route -> transitType.name().equals(route.getRouteType()))
                    .sorted(transitComparator(sortType))
                    .forEach(sortedRoutes::add);
        }

        return sortedRoutes;
    }

    private RouteResultResponse mapKakaoMapRoute(JsonNode response, String routeType) {
        validateKakaoMapRouteStatus(response);
        JsonNode route = response.path("route");
        JsonNode properties = route.path("properties");
        List<RouteStepResponse> steps = new ArrayList<>();
        List<CoordinateResponse> path = new ArrayList<>();

        for (JsonNode leg : route.path("legs")) {
            for (JsonNode step : leg.path("steps")) {
                JsonNode stepProperties = step.path("properties");
                List<CoordinateResponse> stepPath = mapPointCoordinates(step.path("path").path("points"));
                path.addAll(stepPath);
                steps.add(RouteStepResponse.builder()
                        .guidance(stepProperties.path("guidance").asString())
                        .distance(stepProperties.path("distance").asInt())
                        .time(stepProperties.path("time").asInt())
                        .path(stepPath)
                        .build());
            }
        }

        return RouteResultResponse.builder()
                .routeType(routeType)
                .totalDistance(properties.path("totalDistance").asInt())
                .totalTime(properties.path("totalTime").asInt())
                .landingUrl(properties.path("landingUrl").asString())
                .steps(steps)
                .path(path)
                .build();
    }

    private RouteResponse buildResponse(
            TransportType transportType,
            String selectedOption,
            Place origin,
            Place destination,
            List<RouteResultResponse> routes) {
        return buildResponse(
                transportType,
                selectedOption,
                origin,
                destination,
                List.of(),
                routes
        );
    }

    private RouteResponse buildResponse(
            TransportType transportType,
            String selectedOption,
            Place origin,
            Place destination,
            List<Place> waypoints,
            List<RouteResultResponse> routes) {
        return RouteResponse.builder()
                .transportType(transportType)
                .selectedOption(selectedOption)
                .origin(toPlaceResponse(origin))
                .destination(toPlaceResponse(destination))
                .waypoints(waypoints.stream().map(this::toPlaceResponse).toList())
                .routes(routes)
                .build();
    }

    private PlaceResponse toPlaceResponse(Place place) {
        return PlaceResponse.builder()
                .placeNo(place.getPlaceNo())
                .placeName(place.getPlaceName())
                .xAxis(place.getXAxis())
                .yAxis(place.getYAxis())
                .build();
    }

    private List<Place> resolveWaypoints(
            List<Long> waypointPlaceNos,
            TransportType transportType) {
        if (waypointPlaceNos == null || waypointPlaceNos.isEmpty()) {
            return List.of();
        }
        if (transportType != TransportType.WALK) {
            throw new BadRequestException("경유지는 도보 길찾기에서만 사용할 수 있습니다.");
        }
        if (waypointPlaceNos.size() > 3) {
            throw new BadRequestException("경유지는 최대 3개까지 설정할 수 있습니다.");
        }

        List<Place> waypoints = new ArrayList<>();
        for (int index = 0; index < waypointPlaceNos.size(); index++) {
            Long waypointPlaceNo = waypointPlaceNos.get(index);
            if (waypointPlaceNo == null) {
                throw new BadRequestException("경유지는 DB 장소 번호로만 설정할 수 있습니다.");
            }

            String type = "경유지 " + (index + 1);
            waypoints.add(resolvePlace(
                    waypointPlaceNo,
                    null,
                    null,
                    type,
                    type
            ));
        }
        return waypoints;
    }

    private Place resolvePlace(
            Long placeNo,
            Double xAxis,
            Double yAxis,
            String type,
            String directPlaceName) {
        boolean hasPlaceNo = placeNo != null;
        boolean hasAnyCoordinate = xAxis != null || yAxis != null;

        if (hasPlaceNo && hasAnyCoordinate) {
            throw new BadRequestException(
                    type + " 장소 번호와 좌표 중 하나만 입력해야 합니다."
            );
        }

        if (hasPlaceNo) {
            Place place = routeMapper.findPlaceByNo(placeNo);
            if (place == null) {
                throw new NotFoundException(type + " 장소를 찾을 수 없습니다.");
            }
            validateCoordinates(place, type);
            return place;
        }

        if (!hasAnyCoordinate) {
            throw new BadRequestException(type + " 장소 번호 또는 좌표가 필요합니다.");
        }
        if (xAxis == null || yAxis == null) {
            throw new BadRequestException(
                    type + " 장소의 경도와 위도를 모두 입력해야 합니다."
            );
        }
        if (xAxis < -180 || xAxis > 180 || yAxis < -90 || yAxis > 90) {
            throw new BadRequestException(type + " 장소의 좌표 범위가 올바르지 않습니다.");
        }

        return Place.builder()
                .placeName(directPlaceName)
                .xAxis(xAxis)
                .yAxis(yAxis)
                .build();
    }

    private boolean sameCoordinates(Place origin, Place destination) {
        return Double.compare(origin.getXAxis(), destination.getXAxis()) == 0
                && Double.compare(origin.getYAxis(), destination.getYAxis()) == 0;
    }

    private void validateCoordinates(Place place, String type) {
        if (place.getXAxis() == null || place.getYAxis() == null) {
            throw new InternalServerException(type + " 장소의 좌표 정보가 없습니다.");
        }
    }

    private List<CoordinateResponse> mapPointCoordinates(JsonNode points) {
        List<CoordinateResponse> coordinates = new ArrayList<>();
        for (JsonNode point : points) {
            if (point.isArray() && point.size() >= 2) {
                coordinates.add(CoordinateResponse.builder()
                        .xAxis(point.get(0).asDouble())
                        .yAxis(point.get(1).asDouble())
                        .build());
            }
        }
        return coordinates;
    }

    private void addFlatCoordinates(List<CoordinateResponse> coordinates, JsonNode vertexes) {
        for (int index = 0; index + 1 < vertexes.size(); index += 2) {
            coordinates.add(CoordinateResponse.builder()
                    .xAxis(vertexes.get(index).asDouble())
                    .yAxis(vertexes.get(index + 1).asDouble())
                    .build());
        }
    }

    private List<String> mapNames(JsonNode values, String fieldName) {
        List<String> names = new ArrayList<>();
        for (JsonNode value : values) {
            names.add(value.path(fieldName).asString());
        }
        return names;
    }

    private Comparator<RouteResultResponse> transitComparator(TransitSortType sortType) {
        return switch (sortType) {
            case MIN_TIME -> Comparator.comparingInt(RouteResultResponse::getTotalTime);
            case MIN_TRANSFER -> Comparator
                    .comparingInt(RouteResultResponse::getTransfers)
                    .thenComparingInt(RouteResultResponse::getTotalTime);
            case MIN_WALK -> Comparator
                    .comparingInt(RouteResultResponse::getWalkingDistance)
                    .thenComparingInt(RouteResultResponse::getTotalTime);
        };
    }

    private void validateTransitStatus(JsonNode response) {
        String status = response.path("status").asString();
        if ("OK".equals(status)) {
            return;
        }
        if ("EQUAL_POINTS".equals(status) || "INVALID_REQUEST".equals(status)) {
            throw new BadRequestException("대중교통 길찾기 요청이 올바르지 않습니다.");
        }
        if ("STARTNODES_NULL".equals(status)
                || "ENDNODES_NULL".equals(status)
                || "NO_RESULTS".equals(status)) {
            throw new NotFoundException("대중교통 경로를 찾을 수 없습니다.");
        }
        throw new InternalServerException("대중교통 길찾기 응답을 처리할 수 없습니다.");
    }

    private void validateKakaoMapRouteStatus(JsonNode response) {
        String status = response.path("status").asString();
        if ("OK".equals(status)) {
            return;
        }
        if ("SAME_POINT".equals(status)) {
            throw new BadRequestException("출발지와 도착지가 같습니다.");
        }
        if ("START_LINK_NOT_FOUND".equals(status)
                || "END_LINK_NOT_FOUND".equals(status)
                || "TOO_FAR_AWAY".equals(status)
                || "ROUTE_RESULT_NOT_FOUND".equals(status)) {
            throw new NotFoundException("경로를 찾을 수 없습니다.");
        }
        throw new InternalServerException("길찾기 응답을 처리할 수 없습니다.");
    }

    private TransportType parseTransportType(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("이동수단은 필수입니다.");
        }
        try {
            return TransportType.valueOf(normalize(value));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("지원하지 않는 이동수단입니다.");
        }
    }

    private RouteOption parseCarOption(String value) {
        String normalized = normalizeRequiredOption(value, TransportType.CAR);
        return switch (normalized) {
            case "DISTANCE", "MIN_DISTANCE" -> RouteOption.MIN_DISTANCE;
            case "TIME", "MIN_TIME" -> RouteOption.MIN_TIME;
            case "AVOID_TOLL", "MIN_TOLL" -> RouteOption.AVOID_TOLL;
            default -> throw invalidRouteOption(TransportType.CAR);
        };
    }

    private RouteOption parseBicycleOption(String value) {
        String normalized = normalizeRequiredOption(value, TransportType.BICYCLE);
        return switch (normalized) {
            case "DISTANCE", "SHORTEST", "MIN_DISTANCE" -> RouteOption.MIN_DISTANCE;
            case "TIME", "MIN_TIME" -> RouteOption.MIN_TIME;
            case "ACCESSIBLE" -> RouteOption.ACCESSIBLE;
            default -> throw invalidRouteOption(TransportType.BICYCLE);
        };
    }

    private RouteOption parseWalkOption(String value) {
        String normalized = normalizeRequiredOption(value, TransportType.WALK);
        return switch (normalized) {
            case "DISTANCE", "MIN_DISTANCE", "SHORTEST" -> RouteOption.SHORTEST;
            case "BROAD_FIRST" -> RouteOption.BROAD_FIRST;
            case "ACCESSIBLE" -> RouteOption.ACCESSIBLE;
            default -> throw invalidRouteOption(TransportType.WALK);
        };
    }

    private TransitType parseOptionalTransitType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TransitType.valueOf(normalize(value));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("지원하지 않는 대중교통 유형입니다.");
        }
    }

    private TransitSortType parseTransitSortType(String value) {
        if (value == null || value.isBlank()) {
            return TransitSortType.MIN_TIME;
        }
        try {
            return TransitSortType.valueOf(normalize(value));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("지원하지 않는 대중교통 정렬 방식입니다.");
        }
    }

    private String normalizeRequiredOption(String value, TransportType transportType) {
        if (value == null || value.isBlank()) {
            throw invalidRouteOption(transportType);
        }
        return normalize(value);
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private boolean isTransitType(String value) {
        for (TransitType transitType : TransitType.values()) {
            if (transitType.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private BadRequestException invalidRouteOption(TransportType transportType) {
        return new BadRequestException(transportType + "에서 지원하지 않는 경로 옵션입니다.");
    }
}
