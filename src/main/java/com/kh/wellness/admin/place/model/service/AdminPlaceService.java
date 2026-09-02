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
import com.kh.wellness.admin.place.model.dto.AdminPlaceUpdateRequest;
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

		uploadPlaceImages(place.getPlaceNo(), request.getImageFiles(), 1);
	}

	// 관리자 장소 수정 (PATCH - 보낸 필드만 수정, 이미지는 부분 편집)
	@Transactional
	public void updatePlace(Long placeNo, AdminPlaceUpdateRequest request, List<Long> deleteImgNos,
			List<MultipartFile> imageFiles) {

		if (adminPlaceMapper.countActivePlace(placeNo) == 0) {
			throw new NotFoundException("해당 장소가 존재하지 않습니다.");
		}

		if (request.getTypeDetailNo() != null
				&& adminPlaceMapper.countTypeDetailByNo(request.getTypeDetailNo()) == 0) {
			throw new BadRequestException("존재하지 않는 분류입니다.");
		}

		normalizeUpdateRequest(request);
		if (request.hasFieldToUpdate() && adminPlaceMapper.updatePlace(placeNo, request) != 1) {
			throw new InternalServerException("장소 수정에 실패했습니다.");
		}

		// ① 지정된 기존 이미지 소프트삭제 (다른 장소/이미 삭제된 IMG_NO 는 WHERE 조건에서 무시됨)
		if (deleteImgNos != null && !deleteImgNos.isEmpty()) {
			adminPlaceMapper.softDeletePlaceImages(placeNo, deleteImgNos);
		}

		// ② 신규 이미지는 남아있는 이미지 뒤 순번으로 업로드
		int startOrder = adminPlaceMapper.selectMaxImgOrder(placeNo) + 1;
		uploadPlaceImages(placeNo, imageFiles, startOrder);

		// ③ 활성 이미지 IMG_ORDER 를 1..N 으로 재정렬 (유지 이미지가 앞, 신규가 뒤)
		reorderPlaceImages(placeNo);
	}

	// 관리자 장소 일괄 삭제 (소프트) - 이미 삭제된 대상은 WHERE 조건에서 제외되어 카운트에 안 잡힘
	@Transactional
	public int deletePlaces(List<Long> placeNos) {
		return adminPlaceMapper.softDeletePlaces(placeNos);
	}

	// 관리자 장소 일괄 복구 - 이미 활성인 대상은 WHERE 조건에서 제외되어 카운트에 안 잡힘
	@Transactional
	public int restorePlaces(List<Long> placeNos) {
		return adminPlaceMapper.restorePlaces(placeNos);
	}

	private void normalizeUpdateRequest(AdminPlaceUpdateRequest request) {
		if (request.getPlaceName() != null) {
			request.setPlaceName(request.getPlaceName().trim());
		}
		if (request.getPlaceDescription() != null) {
			request.setPlaceDescription(request.getPlaceDescription().trim());
		}
		if (request.getAddr() != null) {
			request.setAddr(request.getAddr().trim());
		}
	}

	private void reorderPlaceImages(Long placeNo) {
		List<PlaceImg> activeImages = adminPlaceMapper.selectPlaceImgList(placeNo);
		for (int index = 0; index < activeImages.size(); index++) {
			int order = index + 1;
			PlaceImg image = activeImages.get(index);
			if (!Integer.valueOf(order).equals(image.getImgOrder())) {
				adminPlaceMapper.updatePlaceImgOrder(image.getImgNo(), order);
			}
		}
	}

	private void uploadPlaceImages(Long placeNo, List<MultipartFile> imageFiles, int startOrder) {
		if (imageFiles == null || imageFiles.isEmpty()) {
			return;
		}

		// 트랜잭션 롤백은 DB만 되돌리므로, 실패 시 이미 올라간 S3 객체는 직접 삭제한다.
		List<String> uploadedKeys = new ArrayList<>();
		try {
			for (int index = 0; index < imageFiles.size(); index++) {
				MultipartFile file = imageFiles.get(index);
				FileSaveResult stored = fileService.store(file, PLACE_IMAGE_DIRECTORY);
				uploadedKeys.add(PLACE_IMAGE_DIRECTORY + "/" + stored.getSaveName());

				PlaceImg placeImg = PlaceImg.builder()
						.placeNo(placeNo)
						.originalName(file.getOriginalFilename())
						.saveName(stored.getSaveName())
						.imgPath(stored.getImgPath())
						.imgOrder(startOrder + index)
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
