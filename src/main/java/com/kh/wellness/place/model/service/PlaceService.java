package com.kh.wellness.place.model.service;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.place.model.dao.PlaceMapper;
import com.kh.wellness.place.model.dto.PlaceDetailDto;
import com.kh.wellness.place.model.dto.PlaceDetailResponse;
import com.kh.wellness.place.model.dto.PlaceImageDto;
import com.kh.wellness.place.model.dto.PlaceTagDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceService {

    private final PlaceMapper placeMapper;

    // 팀원(main) 코드
    public PlaceDto selectByPlaceNo(Long placeNo) {
        PlaceDto place = placeMapper.selectByPlaceNo(placeNo);
        if(place == null) {
            throw new NotFoundException("존재하지 않는 관광지입니다.");
        }
        return place;
    }

    // 고객님 코드 (상세보기)
    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeNo, Long memberNo) {
        PlaceDetailDto placeDetail = placeMapper.selectPlaceDetail(placeNo, memberNo);

        if (placeDetail == null) {
            throw new NotFoundException("해당 장소를 찾을 수 없습니다.");
        }

        List<PlaceImageDto> placeImages = placeMapper.selectPlaceImages(placeNo);
        List<PlaceTagDto> tags = placeMapper.selectPlaceTags(placeNo);

        placeDetail.setPlaceImages(placeImages);
        placeDetail.setTags(tags);

        return PlaceDetailResponse.builder()
                .placeNo(placeDetail.getPlaceNo())
                .placeName(placeDetail.getPlaceName())
                .placeDescription(placeDetail.getPlaceDescription())
                .addr(placeDetail.getAddr())
                .addrDetail(placeDetail.getAddrDetail())
                .phoneNumber(placeDetail.getPhoneNumber())
                .viewCount(placeDetail.getViewCount())
                .xAxis(placeDetail.getXAxis())
                .yAxis(placeDetail.getYAxis())
                .typeDetailNo(placeDetail.getTypeDetailNo())
                .typeDetail(placeDetail.getTypeDetail())
                .type(placeDetail.getType())
                .isBookmarked("Y".equals(placeDetail.getBookmarkYn()))
                .avgRating(placeDetail.getAvgRating())
                .reviewCount(placeDetail.getReviewCount())
                .placeImages(placeDetail.getPlaceImages())
                .tags(placeDetail.getTags())
                .build();
    }
}
