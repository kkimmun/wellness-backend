package com.kh.wellness.admin.place.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.kh.wellness.admin.place.model.dao.AdminPlaceMapper;
import com.kh.wellness.admin.place.model.dto.AdminPlaceCreateRequest;
import com.kh.wellness.admin.place.model.dto.AdminPlaceDetailResponse;
import com.kh.wellness.admin.place.model.dto.AdminPlaceListResponse;
import com.kh.wellness.admin.place.model.dto.PlaceImageResponse;
import com.kh.wellness.admin.place.model.vo.Place;
import com.kh.wellness.admin.place.model.vo.PlaceImg;
import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.file.dto.FileSaveResult;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;

@ExtendWith(MockitoExtension.class)
class AdminPlaceServiceTest {

	@Mock
	private AdminPlaceMapper adminPlaceMapper;

	@Mock
	private FileService fileService;

	@Mock
	private S3Service s3Service;

	@InjectMocks
	private AdminPlaceService adminPlaceService;

	// ---------- getPlaces ----------

	@Test
	@DisplayName("장소가 존재하면 조회 결과와 페이징 정보를 담아 반환한다")
	void getPlaces_returnsPagedContent() {
		when(adminPlaceMapper.countPlaces()).thenReturn(1L);
		when(adminPlaceMapper.selectPlaces(0L, 10))
				.thenReturn(List.of(new AdminPlaceListResponse()));

		PageResponse<AdminPlaceListResponse> result = adminPlaceService.getPlaces(1);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getCurrentPage()).isEqualTo(1);
		assertThat(result.getSize()).isEqualTo(10);
	}

	@Test
	@DisplayName("조회 건수가 없으면 빈 목록을 반환하고 목록 조회 쿼리를 호출하지 않는다")
	void getPlaces_empty() {
		when(adminPlaceMapper.countPlaces()).thenReturn(0L);

		PageResponse<AdminPlaceListResponse> result = adminPlaceService.getPlaces(1);

		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
		assertThat(result.getTotalPages()).isZero();
		verify(adminPlaceMapper, never()).selectPlaces(anyLong(), anyInt());
	}
 
	@Test
	@DisplayName("page 파라미터에 맞춰 offset 을 계산한다")
	void getPlaces_calculatesOffset() {
		when(adminPlaceMapper.countPlaces()).thenReturn(25L);
		when(adminPlaceMapper.selectPlaces(10L, 10))
				.thenReturn(List.of(new AdminPlaceListResponse()));

		PageResponse<AdminPlaceListResponse> result = adminPlaceService.getPlaces(2);

		assertThat(result.getCurrentPage()).isEqualTo(2);
		assertThat(result.getTotalPages()).isEqualTo(3);
		verify(adminPlaceMapper).selectPlaces(10L, 10);
	}

	// ---------- getPlace ----------

	@Test
	@DisplayName("장소 단건 조회 성공 시 상세 정보와 이미지 목록을 담아 반환한다")
	void getPlace_returnsDetailWithImages() {
		AdminPlaceDetailResponse detail = new AdminPlaceDetailResponse();
		detail.setPlaceName("김포아울렛");
		when(adminPlaceMapper.selectPlaceDetail(1L)).thenReturn(detail);
		when(adminPlaceMapper.selectPlaceImages(1L)).thenReturn(List.of(new PlaceImageResponse()));

		AdminPlaceDetailResponse result = adminPlaceService.getPlace(1L);

		assertThat(result.getPlaceName()).isEqualTo("김포아울렛");
		assertThat(result.getPlaceImages()).hasSize(1);
	}

	@Test
	@DisplayName("장소가 존재하지 않으면 NotFoundException 을 던지고 이미지 조회를 하지 않는다")
	void getPlace_notFound() {
		when(adminPlaceMapper.selectPlaceDetail(99L)).thenReturn(null);

		assertThatThrownBy(() -> adminPlaceService.getPlace(99L))
				.isInstanceOf(NotFoundException.class);

		verify(adminPlaceMapper, never()).selectPlaceImages(any());
	}

	// ---------- savePlace ----------

	private AdminPlaceCreateRequest createRequest(Long typeDetailNo, MultipartFile... files) {
		AdminPlaceCreateRequest request = new AdminPlaceCreateRequest();
		request.setTypeDetailNo(typeDetailNo);
		request.setPlaceName("김포아울렛");
		request.setPlaceDescription("김포아울렛 설명");
		request.setAddr("경기도 김포시");
		request.setXAxis(126.7);
		request.setYAxis(37.6);
		request.setImageFiles(List.of(files));
		return request;
	}

	@Test
	@DisplayName("장소 등록 성공 시 PLACE 저장 후 이미지 순서를 부여해 PLACE_IMG 를 저장한다")
	void savePlace_success() {
		MultipartFile first = new MockMultipartFile("imageFiles", "a.jpg", "image/jpeg", "a".getBytes());
		MultipartFile second = new MockMultipartFile("imageFiles", "b.jpg", "image/jpeg", "b".getBytes());

		when(adminPlaceMapper.countTypeDetailByNo(1L)).thenReturn(1);
		when(adminPlaceMapper.insertPlace(any(Place.class))).thenAnswer(invocation -> {
			invocation.getArgument(0, Place.class).setPlaceNo(10L);
			return 1;
		});
		when(fileService.store(any(), eq("places")))
				.thenReturn(new FileSaveResult("saved.jpg", "https://bucket/places/"));
		when(adminPlaceMapper.insertPlaceImg(any(PlaceImg.class))).thenReturn(1);

		adminPlaceService.savePlace(createRequest(1L, first, second));

		ArgumentCaptor<PlaceImg> captor = ArgumentCaptor.forClass(PlaceImg.class);
		verify(adminPlaceMapper, times(2)).insertPlaceImg(captor.capture());
		assertThat(captor.getAllValues()).extracting(PlaceImg::getPlaceNo).containsOnly(10L);
		assertThat(captor.getAllValues()).extracting(PlaceImg::getImgOrder).containsExactly(1, 2);
	}

	@Test
	@DisplayName("존재하지 않는 분류면 BadRequestException 을 던지고 PLACE 를 저장하지 않는다")
	void savePlace_invalidTypeDetail() {
		MultipartFile file = new MockMultipartFile("imageFiles", "a.jpg", "image/jpeg", "a".getBytes());
		when(adminPlaceMapper.countTypeDetailByNo(99L)).thenReturn(0);

		assertThatThrownBy(() -> adminPlaceService.savePlace(createRequest(99L, file)))
				.isInstanceOf(BadRequestException.class);

		verify(adminPlaceMapper, never()).insertPlace(any());
	}

	@Test
	@DisplayName("PLACE 저장에 실패하면 InternalServerException 을 던지고 이미지 업로드를 시도하지 않는다")
	void savePlace_insertPlaceFail() {
		MultipartFile file = new MockMultipartFile("imageFiles", "a.jpg", "image/jpeg", "a".getBytes());
		when(adminPlaceMapper.countTypeDetailByNo(1L)).thenReturn(1);
		when(adminPlaceMapper.insertPlace(any(Place.class))).thenReturn(0);

		assertThatThrownBy(() -> adminPlaceService.savePlace(createRequest(1L, file)))
				.isInstanceOf(InternalServerException.class);

		verify(fileService, never()).store(any(), any());
	}

	@Test
	@DisplayName("이미지 저장 중 실패하면 이미 업로드된 S3 객체를 모두 삭제한다")
	void savePlace_imageFail_cleansUpUploadedS3Objects() {
		MultipartFile first = new MockMultipartFile("imageFiles", "a.jpg", "image/jpeg", "a".getBytes());
		MultipartFile second = new MockMultipartFile("imageFiles", "b.jpg", "image/jpeg", "b".getBytes());

		when(adminPlaceMapper.countTypeDetailByNo(1L)).thenReturn(1);
		when(adminPlaceMapper.insertPlace(any(Place.class))).thenAnswer(invocation -> {
			invocation.getArgument(0, Place.class).setPlaceNo(10L);
			return 1;
		});
		when(fileService.store(any(), eq("places")))
				.thenReturn(new FileSaveResult("s1.jpg", "https://bucket/places/"))
				.thenReturn(new FileSaveResult("s2.jpg", "https://bucket/places/"));
		when(adminPlaceMapper.insertPlaceImg(any(PlaceImg.class)))
				.thenReturn(1)
				.thenReturn(0);

		assertThatThrownBy(() -> adminPlaceService.savePlace(createRequest(1L, first, second)))
				.isInstanceOf(InternalServerException.class);

		verify(s3Service).deleteFile("places/s1.jpg");
		verify(s3Service).deleteFile("places/s2.jpg");
	}
}
