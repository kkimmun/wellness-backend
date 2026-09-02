package com.kh.wellness.course.model.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.course.model.dao.CourseMapper;
import com.kh.wellness.course.model.dto.CourseListResponse;
import com.kh.wellness.course.model.dto.CourseListRow;
import com.kh.wellness.course.model.dto.CourseResponse;
import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.course.model.dto.WaypointDto;
import com.kh.wellness.course.model.dto.WaypointsRequest;
import com.kh.wellness.course.model.enums.CourseTag;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.route.model.dto.CoordinateResponse;
import com.kh.wellness.route.model.dto.PlaceCandidate;
import com.kh.wellness.route.model.dto.RouteResponse;
import com.kh.wellness.route.model.dto.RouteResultResponse;
import com.kh.wellness.route.model.dto.RouteSearchRequest;
import com.kh.wellness.route.model.service.RouteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CourseService {

	private final RouteService routeService;
	
    private static final int PAGE_SIZE = 5;

    private final CourseMapper courseMapper;
    

    public PageResponse<CourseListResponse> getCourses(int page) {
        if (page < 1) {
            throw new BadRequestException("올바르지 않은 조회 조건입니다.");
        }

        long totalElements = courseMapper.countActiveCourses();
        if (totalElements == 0) {
            return PageResponse.empty(page, PAGE_SIZE);
        }

        long offset = (long) (page - 1) * PAGE_SIZE;
        List<CourseListRow> rows = courseMapper.selectActiveCourses(offset, PAGE_SIZE);
        List<CourseListResponse> courses = rows.stream()
                .map(CourseListResponse::from)
                .toList();

        return new PageResponse<>(courses, totalElements, page, PAGE_SIZE);
    }
    
    
    
    public CourseResponse getCourse(Long courseNo) {
    	validateCourseNo(courseNo);
    	CourseResponse course = courseMapper.selectByCourseNo(courseNo);
    	existsCourse(course);
    	course.setPlaces(getWaypoint(courseNo));
    	return course;
    }
    
    private List<WaypointDto> getWaypoint(Long courseNo) {
    	return courseMapper.selectWaypointBycourseNo(courseNo);
    }
    
    private void validateCourseNo(Long courseNo) {
        if (courseNo == null || courseNo <= 0) {
            throw new BadRequestException("올바르지 않은 고정 코스 번호입니다.");
        }
    }
    
    private void existsCourse(CourseResponse course) {
    	if(course == null) {
    		throw new BadRequestException("존재하지 않는 코스 번호입니다.");
    	}
    }



	public List<PlaceCandidate> getWaypoints(WaypointsRequest request) {
		List<PlaceDto>places = courseMapper.selectByTags(request.getTags());
	
		RouteSearchRequest routeRequest = new RouteSearchRequest();
		routeRequest.setStartX(request.getStartX());
		routeRequest.setStartY(request.getStartY());
		routeRequest.setEndPlaceNo(request.getEndPlaceNo());
		routeRequest.setTransportType("WALK");
		routeRequest.setRouteOption("BROAD_FIRST");
		RouteResponse res = routeService.findRoutes(routeRequest);
		List<RouteResultResponse> routes = res.getRoutes();
		if(routes.isEmpty()) {
			throw new InternalServerException("잠시 후에 다시 시도해주세요.");
		}
		List<CoordinateResponse> path = routes.get(0).getPath();
		
		List<PlaceCandidate> candidates = new ArrayList<>();

		for (PlaceDto place : places) {
		    double distance = getMinDistanceFromPath(place, path);
		    if (distance > 2000) {
		        continue;
		    }

		    double distanceScore = calculateDistanceScore(distance);
		    double tagScore = calculateTagScore(place, request.getTags());

		    double totalScore =
		            tagScore * 0.7
		            + distanceScore * 0.3;
		    
		    candidates.add(
		            PlaceCandidate.builder()
		                    .place(place)
		                    .placeName(place.getPlaceName())
		                    .imageUrl(place.getImageUrl())
		                    .tags(place.getTags())
		                    .distance(distance)
		                    .distanceScore(distanceScore)
		                    .tagScore(Math.round(tagScore * 100.0) / 100.0)
		                    .totalScore(Math.round(totalScore * 100.0) / 100.0)
		                    .build()
		    );
		}
		
		candidates.sort(
			    Comparator.comparingDouble(PlaceCandidate::getTotalScore)
			              .reversed());
		return candidates;

	}
	
	private double getMinDistanceFromPath(
	        PlaceDto place,
	        List<CoordinateResponse> path
	) {

	    double minDistance = Double.MAX_VALUE;

	    for (CoordinateResponse coordinate : path) {

	        double distance = calculateDistance(
	                place.getYAxis(),
	                place.getXAxis(),
	                coordinate.getYAxis(),
	                coordinate.getXAxis()
	        );

	        minDistance = Math.min(minDistance, distance);
	    }

	    return minDistance;
	}

	private double calculateDistance(
	        double lat1,
	        double lon1,
	        double lat2,
	        double lon2
	) {

	    final double EARTH_RADIUS = 6371000;

	    double latDistance = Math.toRadians(lat2 - lat1);
	    double lonDistance = Math.toRadians(lon2 - lon1);

	    double a =
	            Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(lat1))
	            * Math.cos(Math.toRadians(lat2))
	            * Math.sin(lonDistance / 2)
	            * Math.sin(lonDistance / 2);

	    double c = 2 * Math.atan2(
	            Math.sqrt(a),
	            Math.sqrt(1 - a)
	    );

	    return EARTH_RADIUS * c;
	}
	
	private double calculateDistanceScore(double distance) {

	    if (distance <= 500) {
	        return 1.0;
	    } else if (distance <= 1000) {
	        return 0.7;
	    } else if (distance <= 1500) {
	        return 0.5;
	    } else if (distance <= 2000) {
	        return 0.2;
	    }

	    return 0;
	}
	
	private double calculateTagScore(
	        PlaceDto place,
	        List<CourseTag> requestTags
	) {

	    long matchedCount = requestTags.stream()
	            .filter(place.getTags()::contains)
	            .count();

	    return (double) matchedCount / requestTags.size();
	}
	

}
