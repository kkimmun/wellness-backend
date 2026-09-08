package com.kh.wellness.review.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.kh.wellness.exception.ForbiddenException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.file.dto.FileSaveResult;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;
import com.kh.wellness.review.model.dao.ReviewMapper;
import com.kh.wellness.review.model.dto.RatingCountDto;
import com.kh.wellness.review.model.dto.ReviewCreateRequest;
import com.kh.wellness.review.model.dto.ReviewCreateResponse;
import com.kh.wellness.review.model.dto.ReviewDetailResponse;
import com.kh.wellness.review.model.dto.ReviewImageDto;
import com.kh.wellness.review.model.dto.ReviewItemDto;
import com.kh.wellness.review.model.dto.ReviewListResponse;
import com.kh.wellness.review.model.dto.ReviewSummaryDto;
import com.kh.wellness.review.model.dto.ReviewUpdateRequest;
import com.kh.wellness.review.model.dto.ReviewUpdateResponse;
import com.kh.wellness.review.model.vo.Review;
import com.kh.wellness.review.model.vo.ReviewImg;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewMapper reviewMapper;

	@Mock
	private FileService fileService;

	@Mock
	private S3Service s3Service;

	@InjectMocks
	private ReviewService reviewService;

	private ReviewCreateRequest createRequest(Integer rating, String content, MultipartFile image) {
		ReviewCreateRequest request = new ReviewCreateRequest();
		request.setRating(rating);
		request.setReviewContent(content);
		request.setImage(image);
		return request;
	}

	private ReviewUpdateRequest updateRequest(Integer rating, String content, MultipartFile image) {
		ReviewUpdateRequest request = new ReviewUpdateRequest();
		request.setRating(rating);
		request.setReviewContent(content);
		request.setImage(image);
		return request;
	}

	private MockMultipartFile imageFile() {
		return new MockMultipartFile("image", "a.jpg", "image/jpeg", "a".getBytes());
	}

	// insertReview 시 MyBatis 가 생성 키를 채워주는 동작을 흉내낸다.
	// Review VO 에 setter 가 없으므로 필드에 직접 주입한다.
	private Answer<Integer> assignReviewNo(long reviewNo) {
		return invocation -> {
			ReflectionTestUtils.setField(invocation.getArgument(0, Review.class), "reviewNo", reviewNo);
			return 1;
		};
	}

	// ---------- createReview ----------

	@Test
	@DisplayName("리뷰 등록 성공 시 리뷰를 저장하고 이미지 1장을 IMG_ORDER 1 로 저장한 뒤 등록일을 반환한다")
	void createReview_success_withImage() {
		when(reviewMapper.countActivePlace(1L)).thenReturn(1);
		when(reviewMapper.insertReview(any(Review.class))).thenAnswer(assignReviewNo(50L));
		when(fileService.store(any(), eq("reviews")))
				.thenReturn(new FileSaveResult("saved.jpg", "https://bucket/reviews/"));
		when(reviewMapper.insertReviewImg(any(ReviewImg.class))).thenReturn(1);
		when(reviewMapper.selectCreateDate(50L)).thenReturn("2026-09-08 10:00:00");

		ReviewCreateResponse result = reviewService.createReview(12L, 1L, createRequest(4, "좋아요", imageFile()));

		assertThat(result.getReviewNo()).isEqualTo(50L);
		assertThat(result.getCreateDate()).isEqualTo("2026-09-08 10:00:00");

		ArgumentCaptor<ReviewImg> imgCaptor = ArgumentCaptor.forClass(ReviewImg.class);
		verify(reviewMapper).insertReviewImg(imgCaptor.capture());
		assertThat(imgCaptor.getValue().getReviewNo()).isEqualTo(50L);
		assertThat(imgCaptor.getValue().getImgOrder()).isEqualTo(1);
		assertThat(imgCaptor.getValue().getImgPath()).isEqualTo("https://bucket/reviews/");

		ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
		verify(reviewMapper).insertReview(reviewCaptor.capture());
		assertThat(reviewCaptor.getValue().getMemberNo()).isEqualTo(12L);
		assertThat(reviewCaptor.getValue().getPlaceNo()).isEqualTo(1L);
		assertThat(reviewCaptor.getValue().getRating()).isEqualTo(4);
	}

	@Test
	@DisplayName("이미지가 없어도 리뷰를 등록하고 이미지 저장은 시도하지 않는다")
	void createReview_success_withoutImage() {
		when(reviewMapper.countActivePlace(1L)).thenReturn(1);
		when(reviewMapper.insertReview(any(Review.class))).thenAnswer(assignReviewNo(51L));
		when(reviewMapper.selectCreateDate(51L)).thenReturn("2026-09-08 10:00:00");

		ReviewCreateResponse result = reviewService.createReview(12L, 1L, createRequest(5, null, null));

		assertThat(result.getReviewNo()).isEqualTo(51L);
		verify(fileService, never()).store(any(), any());
		verify(reviewMapper, never()).insertReviewImg(any());
	}

	@Test
	@DisplayName("빈 이미지 파트로 등록하면 이미지 저장을 시도하지 않는다")
	void createReview_emptyImagePart() {
		when(reviewMapper.countActivePlace(1L)).thenReturn(1);
		when(reviewMapper.insertReview(any(Review.class))).thenAnswer(assignReviewNo(52L));
		when(reviewMapper.selectCreateDate(52L)).thenReturn("2026-09-08 10:00:00");

		MockMultipartFile empty = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);
		reviewService.createReview(12L, 1L, createRequest(3, "무난", empty));

		verify(fileService, never()).store(any(), any());
		verify(reviewMapper, never()).insertReviewImg(any());
	}

	@Test
	@DisplayName("로그인하지 않으면 UnauthorizedException 을 던지고 장소 확인/저장을 하지 않는다")
	void createReview_notLoggedIn() {
		assertThatThrownBy(() -> reviewService.createReview(null, 1L, createRequest(4, "좋아요", null)))
				.isInstanceOf(UnauthorizedException.class);

		verify(reviewMapper, never()).countActivePlace(anyLong());
		verify(reviewMapper, never()).insertReview(any());
	}

	@Test
	@DisplayName("존재하지 않는 장소면 NotFoundException 을 던지고 리뷰를 저장하지 않는다")
	void createReview_placeNotFound() {
		when(reviewMapper.countActivePlace(99L)).thenReturn(0);

		assertThatThrownBy(() -> reviewService.createReview(12L, 99L, createRequest(4, "좋아요", null)))
				.isInstanceOf(NotFoundException.class);

		verify(reviewMapper, never()).insertReview(any());
	}

	@Test
	@DisplayName("리뷰 INSERT 결과가 1이 아니면 InternalServerException 을 던지고 이미지 업로드를 하지 않는다")
	void createReview_insertFail() {
		when(reviewMapper.countActivePlace(1L)).thenReturn(1);
		when(reviewMapper.insertReview(any(Review.class))).thenReturn(0);

		assertThatThrownBy(() -> reviewService.createReview(12L, 1L, createRequest(4, "좋아요", imageFile())))
				.isInstanceOf(InternalServerException.class);

		verify(fileService, never()).store(any(), any());
	}

	@Test
	@DisplayName("이미지 저장 중 실패하면 이미 업로드된 S3 객체를 삭제한다")
	void createReview_imageFail_cleansUpS3() {
		when(reviewMapper.countActivePlace(1L)).thenReturn(1);
		when(reviewMapper.insertReview(any(Review.class))).thenAnswer(assignReviewNo(50L));
		when(fileService.store(any(), eq("reviews")))
				.thenReturn(new FileSaveResult("s1.jpg", "https://bucket/reviews/"));
		when(reviewMapper.insertReviewImg(any(ReviewImg.class))).thenReturn(0);

		assertThatThrownBy(() -> reviewService.createReview(12L, 1L, createRequest(4, "좋아요", imageFile())))
				.isInstanceOf(InternalServerException.class);

		verify(s3Service).deleteFile("reviews/s1.jpg");
	}

	// ---------- getReviewForEdit ----------

	@Test
	@DisplayName("수정용 단건 조회 성공 시 리뷰 상세와 이미지 목록을 담아 반환한다")
	void getReviewForEdit_success() {
		ReviewDetailResponse detail = new ReviewDetailResponse();
		detail.setReviewNo(50L);
		detail.setPlaceNo(1L);
		detail.setRating(4);
		when(reviewMapper.selectReviewDetail(1L, 50L)).thenReturn(detail);
		when(reviewMapper.selectReviewImgList(50L)).thenReturn(List.of(
				ReviewImg.builder().imgNo(9L).reviewNo(50L).imgPath("https://bucket/reviews/").saveName("s.jpg")
						.imgOrder(1).build()));

		ReviewDetailResponse result = reviewService.getReviewForEdit(12L, 1L, 50L);

		assertThat(result.getReviewNo()).isEqualTo(50L);
		assertThat(result.getImages()).hasSize(1);
		assertThat(result.getImages().getFirst().getImageUrl()).isEqualTo("https://bucket/reviews/s.jpg");
	}

	@Test
	@DisplayName("수정용 단건 조회 시 로그인하지 않으면 UnauthorizedException 을 던진다")
	void getReviewForEdit_notLoggedIn() {
		assertThatThrownBy(() -> reviewService.getReviewForEdit(null, 1L, 50L))
				.isInstanceOf(UnauthorizedException.class);

		verify(reviewMapper, never()).selectReviewDetail(anyLong(), anyLong());
	}

	@Test
	@DisplayName("수정용 단건 조회 시 리뷰가 없으면 NotFoundException 을 던지고 이미지 조회를 하지 않는다")
	void getReviewForEdit_notFound() {
		when(reviewMapper.selectReviewDetail(1L, 99L)).thenReturn(null);

		assertThatThrownBy(() -> reviewService.getReviewForEdit(12L, 1L, 99L))
				.isInstanceOf(NotFoundException.class);

		verify(reviewMapper, never()).selectReviewImgList(anyLong());
	}

	// ---------- getReviews ----------

	private ReviewSummaryDto summary(double avg, long count) {
		ReviewSummaryDto summary = new ReviewSummaryDto();
		summary.setAvgRating(avg);
		summary.setTotalReviewCount(count);
		return summary;
	}

	private RatingCountDto ratingCount(int rating, int count) {
		RatingCountDto dto = new RatingCountDto();
		dto.setRating(rating);
		dto.setCount(count);
		return dto;
	}

	@Test
	@DisplayName("리뷰 목록 조회 시 요약(평점 분포 1~5 채움)과 이미지가 매핑된 목록을 반환한다")
	void getReviews_withContent() {
		ReviewItemDto item = new ReviewItemDto();
		item.setReviewNo(50L);

		when(reviewMapper.countActivePlace(1L)).thenReturn(1);
		when(reviewMapper.countReviews(1L)).thenReturn(2L);
		when(reviewMapper.selectReviews(1L, 0L, 10)).thenReturn(List.of(item));
		when(reviewMapper.selectReviewImagesByReviewNos(List.of(50L))).thenReturn(List.of(
				imageRow(50L, "https://bucket/reviews/s.jpg")));
		when(reviewMapper.selectReviewSummary(1L)).thenReturn(summary(4.5, 2));
		when(reviewMapper.selectRatingDistribution(1L)).thenReturn(List.of(ratingCount(4, 1), ratingCount(5, 1)));

		ReviewListResponse result = reviewService.getReviews(1L, 1);

		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().getFirst().getImages()).containsExactly("https://bucket/reviews/s.jpg");
		assertThat(result.getSummary().getAvgRating()).isEqualTo(4.5);
		assertThat(result.getSummary().getRatingDistribution())
				.containsEntry(1, 0).containsEntry(2, 0).containsEntry(3, 0)
				.containsEntry(4, 1).containsEntry(5, 1);
	}

	@Test
	@DisplayName("리뷰가 없으면 목록 조회 쿼리를 호출하지 않고 빈 목록과 요약만 반환한다")
	void getReviews_empty() {
		when(reviewMapper.countActivePlace(1L)).thenReturn(1);
		when(reviewMapper.countReviews(1L)).thenReturn(0L);
		when(reviewMapper.selectReviewSummary(1L)).thenReturn(summary(0.0, 0));
		when(reviewMapper.selectRatingDistribution(1L)).thenReturn(List.of());

		ReviewListResponse result = reviewService.getReviews(1L, 1);

		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalPages()).isZero();
		assertThat(result.isHasNext()).isFalse();
		assertThat(result.getSummary().getRatingDistribution()).containsEntry(3, 0);
		verify(reviewMapper, never()).selectReviews(anyLong(), anyLong(), anyInt());
	}

	@Test
	@DisplayName("페이지가 여러 개면 hasNext 는 true, hasPrevious 는 false 로 계산한다")
	void getReviews_pagination() {
		ReviewItemDto item = new ReviewItemDto();
		item.setReviewNo(50L);

		when(reviewMapper.countActivePlace(1L)).thenReturn(1);
		when(reviewMapper.countReviews(1L)).thenReturn(25L);
		when(reviewMapper.selectReviews(1L, 0L, 10)).thenReturn(List.of(item));
		when(reviewMapper.selectReviewImagesByReviewNos(List.of(50L))).thenReturn(List.of());
		when(reviewMapper.selectReviewSummary(1L)).thenReturn(summary(4.0, 25));
		when(reviewMapper.selectRatingDistribution(1L)).thenReturn(List.of());

		ReviewListResponse result = reviewService.getReviews(1L, 1);

		assertThat(result.getTotalPages()).isEqualTo(3);
		assertThat(result.isHasNext()).isTrue();
		assertThat(result.isHasPrevious()).isFalse();
	}

	@Test
	@DisplayName("존재하지 않는 장소의 리뷰 목록을 조회하면 NotFoundException 을 던진다")
	void getReviews_placeNotFound() {
		when(reviewMapper.countActivePlace(99L)).thenReturn(0);

		assertThatThrownBy(() -> reviewService.getReviews(99L, 1))
				.isInstanceOf(NotFoundException.class);

		verify(reviewMapper, never()).countReviews(anyLong());
	}

	private ReviewImageDto imageRow(Long reviewNo, String url) {
		ReviewImageDto dto = new ReviewImageDto();
		dto.setReviewNo(reviewNo);
		dto.setImageUrl(url);
		return dto;
	}

	// ---------- updateReview ----------

	private Review ownedReview(Long memberNo, Long placeNo) {
		return Review.builder().reviewNo(50L).memberNo(memberNo).placeNo(placeNo).delYn("N").build();
	}

	@Test
	@DisplayName("이미지 파트 없이 수정하면 리뷰만 수정하고 이미지 교체를 시도하지 않는다")
	void updateReview_withoutImage() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(12L, 1L));
		when(reviewMapper.updateReview(50L, 5, "수정됨")).thenReturn(1);

		ReviewUpdateResponse result = reviewService.updateReview(12L, 1L, 50L, updateRequest(5, "수정됨", null));

		assertThat(result.getReviewNo()).isEqualTo(50L);
		assertThat(result.getUpdateDate()).isEqualTo(LocalDate.now().toString());
		verify(reviewMapper, never()).softDeleteReviewImages(anyLong());
		verify(fileService, never()).store(any(), any());
	}

	@Test
	@DisplayName("이미지 파트를 함께 보내면 기존 이미지를 삭제한 뒤 새 이미지를 저장한다")
	void updateReview_replaceImage() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(12L, 1L));
		when(reviewMapper.updateReview(50L, 4, "교체")).thenReturn(1);
		when(fileService.store(any(), eq("reviews")))
				.thenReturn(new FileSaveResult("new.jpg", "https://bucket/reviews/"));
		when(reviewMapper.insertReviewImg(any(ReviewImg.class))).thenReturn(1);

		reviewService.updateReview(12L, 1L, 50L, updateRequest(4, "교체", imageFile()));

		InOrder order = inOrder(reviewMapper, fileService);
		order.verify(reviewMapper).softDeleteReviewImages(50L);
		order.verify(fileService).store(any(), eq("reviews"));
		order.verify(reviewMapper).insertReviewImg(any(ReviewImg.class));
	}

	@Test
	@DisplayName("빈 이미지 파트를 보내면 기존 이미지를 삭제만 하고 새 이미지를 저장하지 않는다")
	void updateReview_clearImage() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(12L, 1L));
		when(reviewMapper.updateReview(50L, 4, null)).thenReturn(1);

		MockMultipartFile empty = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);
		reviewService.updateReview(12L, 1L, 50L, updateRequest(4, null, empty));

		verify(reviewMapper).softDeleteReviewImages(50L);
		verify(fileService, never()).store(any(), any());
		verify(reviewMapper, never()).insertReviewImg(any());
	}

	@Test
	@DisplayName("본인이 작성하지 않은 리뷰를 수정하면 ForbiddenException 을 던지고 수정하지 않는다")
	void updateReview_notOwner() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(99L, 1L));

		assertThatThrownBy(() -> reviewService.updateReview(12L, 1L, 50L, updateRequest(4, "x", null)))
				.isInstanceOf(ForbiddenException.class);

		verify(reviewMapper, never()).updateReview(anyLong(), any(), any());
	}

	@Test
	@DisplayName("리뷰가 없거나 장소가 일치하지 않으면 NotFoundException 을 던진다")
	void updateReview_reviewNotFoundOrPlaceMismatch() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(12L, 2L));

		assertThatThrownBy(() -> reviewService.updateReview(12L, 1L, 50L, updateRequest(4, "x", null)))
				.isInstanceOf(NotFoundException.class);

		verify(reviewMapper, never()).updateReview(anyLong(), any(), any());
	}

	@Test
	@DisplayName("리뷰 UPDATE 결과가 1이 아니면 InternalServerException 을 던진다")
	void updateReview_updateFail() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(12L, 1L));
		when(reviewMapper.updateReview(50L, 4, "x")).thenReturn(0);

		assertThatThrownBy(() -> reviewService.updateReview(12L, 1L, 50L, updateRequest(4, "x", null)))
				.isInstanceOf(InternalServerException.class);
	}

	// ---------- deleteReview ----------

	@Test
	@DisplayName("리뷰 삭제 성공 시 리뷰와 리뷰 이미지를 함께 논리삭제한다")
	void deleteReview_success() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(12L, 1L));
		when(reviewMapper.softDeleteReview(50L)).thenReturn(1);

		reviewService.deleteReview(12L, 1L, 50L);

		verify(reviewMapper).softDeleteReview(50L);
		verify(reviewMapper).softDeleteReviewImages(50L);
	}

	@Test
	@DisplayName("본인이 작성하지 않은 리뷰를 삭제하면 ForbiddenException 을 던지고 삭제하지 않는다")
	void deleteReview_notOwner() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(99L, 1L));

		assertThatThrownBy(() -> reviewService.deleteReview(12L, 1L, 50L))
				.isInstanceOf(ForbiddenException.class);

		verify(reviewMapper, never()).softDeleteReview(anyLong());
	}

	@Test
	@DisplayName("리뷰 삭제 결과가 1이 아니면 InternalServerException 을 던진다")
	void deleteReview_deleteFail() {
		when(reviewMapper.selectReviewForAuth(50L)).thenReturn(ownedReview(12L, 1L));
		when(reviewMapper.softDeleteReview(50L)).thenReturn(0);

		assertThatThrownBy(() -> reviewService.deleteReview(12L, 1L, 50L))
				.isInstanceOf(InternalServerException.class);

		verify(reviewMapper, never()).softDeleteReviewImages(anyLong());
	}
}
