package com.kh.wellness.recommendation.course.model.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationCandidateRow;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationPlaceResponse;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationRequest;
import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationResponse;
import com.kh.wellness.recommendation.course.model.dao.CourseRecommendationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRecommendationService {

    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final int DEFAULT_PLACE_COUNT = 5;
    private static final int MIN_PLACE_COUNT = 3;
    private static final int MAX_PLACE_COUNT = 10;
    private static final int BEAM_WIDTH = 160;
    private static final int MAX_EXPANSIONS_PER_STATE = 40;

    private final CourseRecommendationMapper courseRecommendationMapper;

    public CourseRecommendationResponse recommend(
            Long memberNo,
            CourseRecommendationRequest request) {
        validateMember(memberNo);
        int placeCount = normalizePlaceCount(request.getPlaceCount());
        Set<Long> preferredPlaceNos = normalizeNumbers(
                request.getPreferredPlaceNos(), "선호 장소 번호", 2);
        Set<Long> requestedTagNos = normalizeNumbers(request.getTagNos(), "태그 번호", 2);
        Set<String> excludedSignatures = normalizeSignatures(
                request.getExcludeCourseSignatures());

        List<Candidate> candidates = aggregateCandidates(
                courseRecommendationMapper.findAllCandidates()).stream()
                .filter(candidate -> candidate.group != null)
                .toList();
        validatePreferredPlaces(preferredPlaceNos, candidates);

        State selected = search(
                request.getStartX(),
                request.getStartY(),
                placeCount,
                preferredPlaceNos,
                requestedTagNos,
                excludedSignatures,
                candidates);
        if (selected == null) {
            throw new NotFoundException(
                    "선택한 조건과 타입별 추천 거리 규칙을 만족하는 다른 추천 코스가 없습니다.");
        }
        return toResponse(selected, requestedTagNos);
    }

    private State search(
            double startX,
            double startY,
            int placeCount,
            Set<Long> preferredPlaceNos,
            Set<Long> requestedTagNos,
            Set<String> excludedSignatures,
            List<Candidate> candidates) {
        List<State> states = List.of(State.start(startX, startY));

        for (int position = 0; position < placeCount; position++) {
            List<State> expanded = new ArrayList<>();
            for (State state : states) {
                List<CandidateDistance> reachable = candidates.stream()
                        .filter(candidate -> !state.usedPlaceNos.contains(candidate.placeNo))
                        .map(candidate -> new CandidateDistance(
                                candidate,
                                distanceMeters(state.lastX, state.lastY,
                                        candidate.xAxis, candidate.yAxis)))
                        .filter(item -> item.distance <= maxLegDistanceMeters(item.candidate))
                        .sorted(Comparator
                                .comparingInt((CandidateDistance item) ->
                                        preferredPlaceNos.contains(item.candidate.placeNo) ? 0 : 1)
                                .thenComparingDouble(CandidateDistance::distance)
                                .thenComparing(item -> item.candidate.placeNo))
                        .limit(MAX_EXPANSIONS_PER_STATE)
                        .toList();

                for (CandidateDistance item : reachable) {
                    State next = append(
                            state,
                            item,
                            position,
                            placeCount,
                            preferredPlaceNos,
                            requestedTagNos);
                    if (next != null) expanded.add(next);
                }
            }
            if (expanded.isEmpty()) return null;

            states = expanded.stream()
                    .sorted(stateComparator())
                    .limit(BEAM_WIDTH)
                    .toList();
        }

        return states.stream()
                .filter(state -> state.usedPlaceNos.containsAll(preferredPlaceNos))
                .filter(state -> state.hasMiddleFood)
                .filter(state -> !excludedSignatures.contains(signature(state.places)))
                .sorted(stateComparator())
                .findFirst()
                .orElse(null);
    }

    private State append(
            State state,
            CandidateDistance item,
            int position,
            int placeCount,
            Set<Long> preferredPlaceNos,
            Set<Long> requestedTagNos) {
        Candidate candidate = item.candidate;
        int remainingAfter = placeCount - position - 1;
        Set<Long> missingPreferred = new HashSet<>(preferredPlaceNos);
        missingPreferred.removeAll(state.usedPlaceNos);
        boolean isMissingPreferred = missingPreferred.contains(candidate.placeNo);
        if (!isMissingPreferred && missingPreferred.size() > remainingAfter) return null;

        Group firstNonFood = state.firstNonFood;
        Group secondNonFood = state.secondNonFood;
        if (candidate.group != Group.FOOD) {
            if (firstNonFood == null) {
                firstNonFood = candidate.group;
            } else if (candidate.group != firstNonFood) {
                if (secondNonFood == null) {
                    secondNonFood = candidate.group;
                } else if (candidate.group != secondNonFood) {
                    // 관광→체험→관광처럼 이미 지나간 범주로 돌아가는 코스는 만들지 않는다.
                    return null;
                }
            } else if (secondNonFood != null) {
                return null;
            }
        }

        boolean hasMiddleFood = state.hasMiddleFood
                || (candidate.group == Group.FOOD && position > 0 && position < placeCount - 1);
        if (!hasMiddleFood && remainingAfter == 0) return null;

        Set<Long> nextUsed = new LinkedHashSet<>(state.usedPlaceNos);
        nextUsed.add(candidate.placeNo);
        long stillMissingPreferred = preferredPlaceNos.stream()
                .filter(placeNo -> !nextUsed.contains(placeNo))
                .count();
        if (stillMissingPreferred > remainingAfter) return null;

        int matchedTags = (int) candidate.tagNos.stream()
                .filter(requestedTagNos::contains)
                .count();
        double score = state.score
                + (isMissingPreferred ? 10_000 : 0)
                + matchedTags * 300
                + Math.log1p(Math.max(0L, candidate.viewCount)) * 2
                + (candidate.group == Group.FOOD && position > 0
                        && position < placeCount - 1 ? 120 : 0)
                - item.distance / 45.0;

        List<Candidate> nextPlaces = new ArrayList<>(state.places);
        nextPlaces.add(candidate);
        List<Long> nextLegDistances = new ArrayList<>(state.legDistances);
        nextLegDistances.add(Math.round(item.distance));
        return new State(
                nextPlaces,
                nextLegDistances,
                nextUsed,
                candidate.xAxis,
                candidate.yAxis,
                firstNonFood,
                secondNonFood,
                hasMiddleFood,
                score,
                state.totalDistance + item.distance);
    }

    private Comparator<State> stateComparator() {
        return Comparator.comparingDouble(State::score).reversed()
                .thenComparingDouble(State::totalDistance)
                .thenComparing(state -> signature(state.places));
    }

    private List<Candidate> aggregateCandidates(List<CourseRecommendationCandidateRow> rows) {
        Map<Long, Candidate> candidates = new LinkedHashMap<>();
        for (CourseRecommendationCandidateRow row : rows) {
            if (row == null || row.getPlaceNo() == null
                    || row.getXAxis() == null || row.getYAxis() == null
                    || !Double.isFinite(row.getXAxis()) || !Double.isFinite(row.getYAxis())) {
                continue;
            }
            Candidate candidate = candidates.computeIfAbsent(
                    row.getPlaceNo(), ignored -> Candidate.from(row));
            if (row.getTagNo() != null) candidate.tagNos.add(row.getTagNo());
            if (row.getTagContent() != null && !row.getTagContent().isBlank()) {
                candidate.tags.add(row.getTagContent());
            }
        }
        return new ArrayList<>(candidates.values());
    }

    private CourseRecommendationResponse toResponse(State state, Set<Long> requestedTagNos) {
        List<CourseRecommendationPlaceResponse> places = new ArrayList<>();
        for (int index = 0; index < state.places.size(); index++) {
            Candidate candidate = state.places.get(index);
            places.add(CourseRecommendationPlaceResponse.builder()
                    .placeNo(candidate.placeNo)
                    .placeName(candidate.placeName)
                    .placeDescription(candidate.placeDescription)
                    .addr(candidate.addr)
                    .addrDetail(candidate.addrDetail)
                    .xAxis(candidate.xAxis)
                    .yAxis(candidate.yAxis)
                    .typeNo(candidate.typeNo)
                    .type(candidate.type)
                    .typeDetailNo(candidate.typeDetailNo)
                    .typeDetail(candidate.typeDetail)
                    .imageUrl(candidate.imageUrl)
                    .distanceFromPreviousMeters(state.legDistances.get(index))
                    .tagNos(List.copyOf(candidate.tagNos))
                    .tags(List.copyOf(candidate.tags))
                    .build());
        }
        int matchedTagCount = (int) state.places.stream()
                .flatMap(candidate -> candidate.tagNos.stream())
                .filter(requestedTagNos::contains)
                .distinct()
                .count();
        return CourseRecommendationResponse.builder()
                .courseSignature(signature(state.places))
                .placeCount(places.size())
                .totalDistanceMeters(Math.round(state.totalDistance))
                .matchedTagCount(matchedTagCount)
                .places(places)
                .build();
    }

    private void validateMember(Long memberNo) {
        if (memberNo == null || memberNo <= 0) {
            throw new BadRequestException("회원 정보를 확인해주세요.");
        }
    }

    private int normalizePlaceCount(Integer placeCount) {
        int result = placeCount == null ? DEFAULT_PLACE_COUNT : placeCount;
        if (result < MIN_PLACE_COUNT || result > MAX_PLACE_COUNT) {
            throw new BadRequestException("추천 장소 개수는 3개 이상 10개 이하여야 합니다.");
        }
        return result;
    }

    private Set<Long> normalizeNumbers(List<Long> values, String fieldName, int maxSize) {
        if (values == null) return Set.of();
        if (values.size() > maxSize) {
            throw new BadRequestException(fieldName + "는 최대 " + maxSize + "개까지 선택할 수 있습니다.");
        }
        Set<Long> result = new LinkedHashSet<>();
        for (Long value : values) {
            if (value == null || value <= 0) {
                throw new BadRequestException(fieldName + "는 1 이상의 값이어야 합니다.");
            }
            result.add(value);
        }
        return result;
    }

    private Set<String> normalizeSignatures(List<String> signatures) {
        if (signatures == null) return Set.of();
        return signatures.stream()
                .filter(signature -> signature != null && !signature.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private void validatePreferredPlaces(Set<Long> preferredPlaceNos, List<Candidate> candidates) {
        Set<Long> candidateNos = candidates.stream()
                .map(candidate -> candidate.placeNo)
                .collect(Collectors.toSet());
        if (!candidateNos.containsAll(preferredPlaceNos)) {
            throw new BadRequestException("선택한 장소 중 추천에 사용할 수 없는 장소가 있습니다.");
        }
    }

    private String signature(List<Candidate> places) {
        return places.stream()
                .map(candidate -> String.valueOf(candidate.placeNo))
                .collect(Collectors.joining("-"));
    }

    private double distanceMeters(double x1, double y1, double x2, double y2) {
        double latitudeDistance = Math.toRadians(y2 - y1);
        double longitudeDistance = Math.toRadians(x2 - x1);
        double firstLatitude = Math.toRadians(y1);
        double secondLatitude = Math.toRadians(y2);
        double haversine = Math.pow(Math.sin(latitudeDistance / 2), 2)
                + Math.cos(firstLatitude) * Math.cos(secondLatitude)
                * Math.pow(Math.sin(longitudeDistance / 2), 2);
        double centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        return EARTH_RADIUS_METERS * centralAngle;
    }

    private double maxLegDistanceMeters(Candidate destination) {
        if (destination == null || destination.typeNo == null) return 3_000;
        return switch (destination.typeNo.intValue()) {
            case 1 -> 10_000; // 주요관광지
            case 2 -> 2_000;  // 의료기관
            case 3 -> 8_000;  // 관광지
            case 4 -> 2_000;  // 체육시설
            case 5 -> 10_000; // 종교시설
            case 6 -> 3_000;  // 음식점
            case 7 -> 8_000;  // 체험
            case 8 -> 5_000;  // 문화시설
            case 9 -> 8_000;  // 자연/생태
            case 10 -> 2_000; // 복지시설
            case 11 -> 3_000; // 공공시설
            default -> 3_000;
        };
    }

    private enum Group {
        TOUR,
        ACTIVITY,
        FOOD
    }

    private static final class Candidate {
        private Long placeNo;
        private String placeName;
        private String placeDescription;
        private String addr;
        private String addrDetail;
        private double xAxis;
        private double yAxis;
        private Long typeNo;
        private String type;
        private Long typeDetailNo;
        private String typeDetail;
        private long viewCount;
        private String imageUrl;
        private Group group;
        private final Set<Long> tagNos = new LinkedHashSet<>();
        private final Set<String> tags = new LinkedHashSet<>();

        private static Candidate from(CourseRecommendationCandidateRow row) {
            Candidate candidate = new Candidate();
            candidate.placeNo = row.getPlaceNo();
            candidate.placeName = row.getPlaceName();
            candidate.placeDescription = row.getPlaceDescription();
            candidate.addr = row.getAddr();
            candidate.addrDetail = row.getAddrDetail();
            candidate.xAxis = row.getXAxis();
            candidate.yAxis = row.getYAxis();
            candidate.typeNo = row.getTypeNo();
            candidate.type = row.getType();
            candidate.typeDetailNo = row.getTypeDetailNo();
            candidate.typeDetail = row.getTypeDetail();
            candidate.viewCount = row.getViewCount() == null ? 0L : row.getViewCount();
            candidate.imageUrl = row.getImageUrl();
            candidate.group = classify(row.getType(), row.getTypeDetail());
            return candidate;
        }

        private static Group classify(String type, String typeDetail) {
            String typeValue = type == null ? "" : type.toLowerCase(Locale.ROOT);
            String detailValue = typeDetail == null ? "" : typeDetail.toLowerCase(Locale.ROOT);
            if (typeValue.contains("음식") || typeValue.contains("카페")
                    || detailValue.contains("카페")) {
                return Group.FOOD;
            }
            if (typeValue.contains("체험") || typeValue.contains("체육")
                    || detailValue.contains("체험") || detailValue.contains("수영")
                    || detailValue.contains("클라이밍") || detailValue.contains("캠핑")) {
                return Group.ACTIVITY;
            }
            if (typeValue.contains("의료") || typeValue.contains("복지")) return null;
            return Group.TOUR;
        }
    }

    private record CandidateDistance(Candidate candidate, double distance) {
    }

    private record State(
            List<Candidate> places,
            List<Long> legDistances,
            Set<Long> usedPlaceNos,
            double lastX,
            double lastY,
            Group firstNonFood,
            Group secondNonFood,
            boolean hasMiddleFood,
            double score,
            double totalDistance) {

        private static State start(double xAxis, double yAxis) {
            return new State(
                    List.of(), List.of(), Set.of(), xAxis, yAxis,
                    null, null, false, 0, 0);
        }
    }
}
