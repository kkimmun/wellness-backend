package com.kh.wellness.route.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.kh.wellness.route.model.dto.CoordinateResponse;

class RouteDistanceCalculatorTest {
    @Test
    void findsRestaurantNearMiddleOfLongSegmentRatherThanOnlyVertices() {
        List<CoordinateResponse> path = List.of(point(-0.02, 0), point(0.02, 0));
        assertThat(RouteDistanceCalculator.minDistanceMeters(0.0, latitude(500), path)).isCloseTo(500, within(0.001));
    }

    @Test
    void clipsProjectionToEndpointsInsteadOfInfiniteLine() {
        List<CoordinateResponse> path = List.of(point(0, 0), point(0.01, 0));
        assertThat(RouteDistanceCalculator.minDistanceMeters(-0.02, 0.0, path)).isGreaterThan(2000);
        assertThat(RouteDistanceCalculator.minDistanceMeters(0.03, 0.0, path)).isGreaterThan(2000);
    }

    @Test
    void followsBendsInsteadOfConnectingStartAndDestinationDirectly() {
        List<CoordinateResponse> path = List.of(point(0, 0), point(0, 0.04), point(0.04, 0.04));
        assertThat(RouteDistanceCalculator.minDistanceMeters(0.0, 0.02, path)).isCloseTo(0, within(0.001));
        assertThat(RouteDistanceCalculator.minDistanceMeters(0.02, 0.02, path)).isGreaterThan(2000);
    }

    @Test
    void handlesSinglePointAndDuplicateVertices() {
        assertThat(RouteDistanceCalculator.minDistanceMeters(0.0, latitude(1000), List.of(point(0, 0))))
                .isCloseTo(1000, within(0.001));
        assertThat(RouteDistanceCalculator.minDistanceMeters(0.0, 0.0, List.of(point(0, 0), point(0, 0))))
                .isZero();
    }

    @Test
    void invalidCoordinatesAreNotTreatedAsNearbyOrBridged() {
        assertThat(RouteDistanceCalculator.minDistanceMeters(null, 0.0, List.of(point(0, 0)))).isInfinite();
        assertThat(RouteDistanceCalculator.minDistanceMeters(Double.NaN, 0.0, List.of(point(0, 0)))).isInfinite();
        assertThat(RouteDistanceCalculator.minDistanceMeters(0.0, 0.0, List.of())).isInfinite();
        List<CoordinateResponse> broken = Arrays.asList(point(-0.02, 0), null, point(0.02, 0));
        assertThat(RouteDistanceCalculator.isValidPath(broken)).isFalse();
        assertThat(RouteDistanceCalculator.minDistanceMeters(0.0, 0.0, broken)).isGreaterThan(2000);
    }

    @Test
    void measuresAcrossDateLineOnShortArc() {
        assertThat(RouteDistanceCalculator.minDistanceMeters(180.0, latitude(500),
                List.of(point(179.98, 0), point(-179.98, 0)))).isCloseTo(500, within(0.001));
    }

    private static double latitude(double meters) { return Math.toDegrees(meters / 6_371_000); }
    private static CoordinateResponse point(double x, double y) {
        return CoordinateResponse.builder().xAxis(x).yAxis(y).build();
    }
}
