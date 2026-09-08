package com.kh.wellness.admin.place.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kh.wellness.admin.place.model.dto.AdminPlaceCreateRequest;
import com.kh.wellness.admin.place.model.dto.PlaceImageLicenseInput;
import com.kh.wellness.admin.place.model.service.AdminPlaceService;

@ExtendWith(MockitoExtension.class)
class AdminPlaceControllerTest {

	@Mock
	private AdminPlaceService adminPlaceService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(new AdminPlaceController(adminPlaceService))
				.build();
	}

	@Test
	@DisplayName("장소 등록 multipart 요청의 이미지별 라이선스를 같은 인덱스로 바인딩한다")
	void savePlace_bindsImageLicenseByIndex() throws Exception {
		MockMultipartFile image = new MockMultipartFile(
				"imageFiles", "place.jpg", "image/jpeg", "image".getBytes());

		mockMvc.perform(multipart("/api/admin/places")
				.file(image)
				.param("typeDetailNo", "1")
				.param("placeName", "김포아트홀")
				.param("placeDescription", "설명")
				.param("addr", "경기도 김포시")
				.param("xAxis", "126.7")
				.param("yAxis", "37.6")
				.param("imageLicenses[0].enabled", "true")
				.param("imageLicenses[0].sourceName", "김포시")
				.param("imageLicenses[0].sourcePageUrl", "https://example.com/source")
				.param("imageLicenses[0].authorName", "홍길동")
				.param("imageLicenses[0].licenseCode", "CC BY 4.0")
				.param("imageLicenses[0].licenseUrl", "https://creativecommons.org/licenses/by/4.0/")
				.param("imageLicenses[0].attributionText", "사진: 홍길동, CC BY 4.0"))
				.andExpect(status().isCreated());

		ArgumentCaptor<AdminPlaceCreateRequest> captor = ArgumentCaptor.forClass(AdminPlaceCreateRequest.class);
		verify(adminPlaceService).savePlace(captor.capture());
		PlaceImageLicenseInput license = captor.getValue().getImageLicenses().getFirst();
		assertThat(license.isEnabled()).isTrue();
		assertThat(license.getSourceName()).isEqualTo("김포시");
		assertThat(license.getLicenseCode()).isEqualTo("CC BY 4.0");
	}

	@Test
	@DisplayName("이미지 라이선스 전체 교체 요청을 서비스에 전달한다")
	void replacePlaceImageLicenses() throws Exception {
		String requestBody = """
				{
				  "licenses": [{
				    "imgNo": 11,
				    "sourceName": "김포시",
				    "sourcePageUrl": "https://example.com/source",
				    "authorName": null,
				    "licenseCode": "KOGL TYPE1",
				    "licenseUrl": null,
				    "attributionText": "김포시 제공"
				  }]
				}
				""";

		mockMvc.perform(put("/api/admin/places/1/images/licenses")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isOk());

		verify(adminPlaceService).replacePlaceImageLicenses(any(), any());
	}

	@Test
	@DisplayName("장소 이미지 삭제 요청을 서비스에 전달한다")
	void deletePlaceImage() throws Exception {
		mockMvc.perform(delete("/api/admin/places/1/images/11"))
				.andExpect(status().isOk());

		verify(adminPlaceService).deletePlaceImage(1L, 11L);
	}
}
