package com.kh.wellness.course.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.admin.place.model.vo.Place;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.course.model.dao.CourseMapper;
import com.kh.wellness.course.model.dto.CourseListResponse;
import com.kh.wellness.course.model.dto.CourseListRow;
import com.kh.wellness.course.model.dto.CourseResponse;
import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.course.model.dto.WaypointDto;
import com.kh.wellness.course.model.dto.WaypointsRequest;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.place.model.service.PlaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CourseService {

    private static final int PAGE_SIZE = 5;

    private final CourseMapper courseMapper;
    private final PlaceService placeService;

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



	public List<PlaceDto> getWaypoints(@Valid WaypointsRequest request) {
		
		//태그 1개 이상 포함되는 관광지 모두 조회
		//List<PlaceDto>places resultMap 써서 placeDto에 list담기
		List<PlaceDto>places = courseMapper.selectByTags(request.getTags()); //1단계 성공
		
		//거리별 필터링
		//출발지/도착지 경로받아서, 그 경로로부터 2km이상이면 제외
		Place endPlace = placeService.selectByPlace(request.getEndPlaceNo( ));
		
		//거리별 점수  -> placeDto에 점수필드를 준다면? 어떨까.
		// 500m 이하 1
		// 1km 이하 0.7
		// 1.5km 이하 0.5
		// 2km 이하 0.2
		// x0.3해서 필드에 +
		
		//태그별 점수
		//태그 최대 5개 받기
		
		
		//list size 계산해서 분모값으로 넣는다. ex 3
		//태그가 포함되는 개수에 따라 점수를 채워준다.
		
		// RecommnededPlaceDto에 담아서 점수 계산을한다.
		// 왜 나누었는가? PlaceDto는 장소 자체의 속성을 저장하는 곳이기 때문에 의미상
		// 새로운 Dto를 만들어서 관리하는게 맞다고 판단했다.
		
		// 1/4 => 0.25
		// 2/4 => 0.5
		// 3/4 => 0.75
		// 4/4 => 1
		
		// 1/3 => 0.33
		// 2/3 => 0.67
		// 3/3 => 1
		// x0.7해서 필드에 +
		
		//숫자를 높은 순서대로 나열하고
		//점수 순서대로 10개 앞단으로 보낸다.
		return null;
	}

}
