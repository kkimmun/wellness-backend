package com.kh.wellness.admin.place.model.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.wellness.admin.place.model.dao.AdminPlaceMapper;
import com.kh.wellness.admin.place.model.dto.AdminPlaceCreateRequest;
import com.kh.wellness.admin.place.model.dto.AdminPlaceDetailResponse;
import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.admin.place.model.vo.Place;
import com.kh.wellness.admin.place.model.vo.PlaceImg;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.file.dto.FileSaveResult;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPlaceService {

	private static final int PAGE_SIZE = 10;

	private static final String PLACE_IMAGE_DIRECTORY = "places";

	private final AdminPlaceMapper adminPlaceMapper;
	private final FileService fileService;
	private final S3Service s3Service;

	// 관리자 장소 목록 조회 (page 는 1-base 로 받아 내부 0-base 로 변환)
	public PageResponse<AdminPlaceListResponse> getPlaces(int page) {
		validatePage(page);

		long totalElements = adminPlaceMapper.countPlaces();
		
		if (totalElements == 0) {
	        return PageResponse.empty(page, PAGE_SIZE);
	    }

		long offset = (long) (page - 1) * PAGE_SIZE;
		
		List<AdminPlaceListResponse> content = adminPlaceMapper.selectPlaces(offset, PAGE_SIZE);

		return new PageResponse<>(content, totalElements,page, PAGE_SIZE);
	}
	
	public AdminPlaceDetailResponse getPlace(Long placeNo) {
		AdminPlaceDetailResponse place = adminPlaceMapper.selectPlaceDetail(placeNo);
		if (place == null) {
			throw new NotFoundException("해당 장소가 존재하지 않습니다.");
		}

		place.setPlaceImages(adminPlaceMapper.selectPlaceImages(placeNo));
		return place;
	}

	// 관리자 장소 등록 (PLACE 1건 + PLACE_IMG N건, 한 트랜잭션)
	@Transactional
	public void savePlace(AdminPlaceCreateRequest request) {
		if (adminPlaceMapper.countTypeDetailByNo(request.getTypeDetailNo()) == 0) {
			throw new BadRequestException("존재하지 않는 분류입니다.");
		}

		Place place = Place.builder()
				.typeDetailNo(request.getTypeDetailNo())
				.placeName(request.getPlaceName().trim())
				.placeDescription(normalizeDescription(request.getPlaceDescription()))
				.addr(request.getAddr().trim())
				.xAxis(request.getXAxis())
				.yAxis(request.getYAxis())
				.viewCount(0)
				.build();

		if (adminPlaceMapper.insertPlace(place) != 1 || place.getPlaceNo() == null) {
			throw new InternalServerException("장소 등록에 실패했습니다.");
		}

		savePlaceImages(place.getPlaceNo(), request.getImageFiles());
	}

	private void savePlaceImages(Long placeNo, List<MultipartFile> imageFiles) {
		if (imageFiles == null || imageFiles.isEmpty()) {
			return;
		}

		// 트랜잭션 롤백은 DB만 되돌리므로, 실패 시 이미 올라간 S3 객체는 직접 삭제한다.
		List<String> uploadedKeys = new ArrayList<>();
		try {
			for (int index = 0; index < imageFiles.size(); index++) {
				MultipartFile file = imageFiles.get(index);
				log.info("장소 이미지[{}] filename={}, contentType={}, size={}, empty={}",
						index, file.getOriginalFilename(), file.getContentType(), file.getSize(), file.isEmpty());
				FileSaveResult stored = fileService.store(file, PLACE_IMAGE_DIRECTORY);
				uploadedKeys.add(PLACE_IMAGE_DIRECTORY + "/" + stored.getSaveName());

				PlaceImg placeImg = PlaceImg.builder()
						.placeNo(placeNo)
						.originalName(file.getOriginalFilename())
						.saveName(stored.getSaveName())
						.imgPath(stored.getImgPath())
						.imgOrder(index + 1)
						.build();

				if (adminPlaceMapper.insertPlaceImg(placeImg) != 1) {
					throw new InternalServerException("장소 이미지 등록에 실패했습니다.");
				}
			}
		} catch (RuntimeException e) {
			deleteUploadedImages(uploadedKeys);
			throw e;
		}
	}

	private void deleteUploadedImages(List<String> keys) {
		for (String key : keys) {
			try {
				s3Service.deleteFile(key);
			} catch (RuntimeException ex) {
				log.warn("장소 등록 롤백 중 S3 객체 삭제 실패: {}", key, ex);
			}
		}
	}

	private String normalizeDescription(String description) {
		if (description == null || description.isBlank()) {
			return null;
		}
		return description.trim();
	}

	 private void validatePage(int page) {
        if (page < 1) {
            throw new BadRequestException("올바르지 않은 조회 조건입니다.");
        }
        
	 }
}
