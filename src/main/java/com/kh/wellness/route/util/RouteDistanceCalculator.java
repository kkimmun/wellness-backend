package com.kh.wellness.route.util;

import java.util.List;

import com.kh.wellness.route.model.dto.CoordinateResponse;

public final class RouteDistanceCalculator {
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private RouteDistanceCalculator() {
    }

    public static boolean isValidCoordinate(Double x, Double y) {
        return x != null && y != null && Double.isFinite(x) && Double.isFinite(y)
                && Math.abs(x) <= 180 && Math.abs(y) <= 90;
    }

    public static boolean isValidPath(List<CoordinateResponse> path) {
        return path != null && path.size() >= 2 && path.stream().allMatch(RouteDistanceCalculator::isValidPoint);
    }

    // 경로의 꼭짓점뿐 아니라 각 유한 선분에 투영한 지점까지 비교한다.
    public static double minDistanceMeters(Double x, Double y, List<CoordinateResponse> path) {
        if (!isValidCoordinate(x, y) || path == null || path.isEmpty()) return Double.POSITIVE_INFINITY;
        double minimum = Double.POSITIVE_INFINITY;
        CoordinateResponse previous = null;
        for (CoordinateResponse point : path) {
            if (!isValidPoint(point)) {
                previous = null; // 좌표가 끊긴 부분을 가상의 선분으로 연결하지 않는다.
                continue;
            }
            minimum = Math.min(minimum, angularDistance(x, y, point.getXAxis(), point.getYAxis()) * EARTH_RADIUS_METERS);
            if (previous != null) {
                minimum = Math.min(minimum, distanceToSegment(x, y, previous, point));
            }
            previous = point;
        }
        return minimum;
    }

    private static boolean isValidPoint(CoordinateResponse point) {
        return point != null && isValidCoordinate(point.getXAxis(), point.getYAxis());
    }

    // 구면의 cross-track/along-track 거리. 참고: https://www.movable-type.co.uk/scripts/latlong.html
    private static double distanceToSegment(double x, double y, CoordinateResponse start, CoordinateResponse end) {
        double segmentAngle = angularDistance(start.getXAxis(), start.getYAxis(), end.getXAxis(), end.getYAxis());
        double startAngle = angularDistance(start.getXAxis(), start.getYAxis(), x, y);
        double endAngle = angularDistance(end.getXAxis(), end.getYAxis(), x, y);
        double endpointDistance = Math.min(startAngle, endAngle) * EARTH_RADIUS_METERS;
        if (segmentAngle < 1e-12 || Math.PI - segmentAngle < 1e-12) return endpointDistance;

        double bearingDifference = bearing(start.getXAxis(), start.getYAxis(), x, y)
                - bearing(start.getXAxis(), start.getYAxis(), end.getXAxis(), end.getYAxis());
        double alongTrack = Math.atan2(Math.sin(startAngle) * Math.cos(bearingDifference), Math.cos(startAngle));
        if (alongTrack < 0 || alongTrack > segmentAngle) return endpointDistance;
        double crossTrack = Math.asin(clamp(Math.sin(startAngle) * Math.sin(bearingDifference), -1, 1));
        return Math.min(endpointDistance, Math.abs(crossTrack) * EARTH_RADIUS_METERS);
    }

    private static double angularDistance(double x1, double y1, double x2, double y2) {
        double latitude = Math.toRadians(y2 - y1);
        double longitude = Math.toRadians(x2 - x1);
        double a = Math.pow(Math.sin(latitude / 2), 2)
                + Math.cos(Math.toRadians(y1)) * Math.cos(Math.toRadians(y2)) * Math.pow(Math.sin(longitude / 2), 2);
        return 2 * Math.asin(Math.sqrt(clamp(a, 0, 1)));
    }

    private static double bearing(double x1, double y1, double x2, double y2) {
        double lat1 = Math.toRadians(y1);
        double lat2 = Math.toRadians(y2);
        double longitude = Math.toRadians(x2 - x1);
        return Math.atan2(Math.sin(longitude) * Math.cos(lat2),
                Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(longitude));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
