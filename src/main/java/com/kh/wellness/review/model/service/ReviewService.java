package com.kh.wellness.review.model.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.wellness.exception.BadRequestException;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

	private static final int PAGE_SIZE = 10;

	private static final String REVIEW_IMAGE_DIRECTORY = "reviews";

	private final ReviewMapper reviewMapper;
	private final FileService fileService;
	private final S3Service s3Service;

	@Transactional
	public ReviewCreateResponse createReview(Long memberNo, Long placeNo, ReviewCreateRequest request) {
		requireLogin(memberNo);
		requireActivePlace(placeNo);
		
		int reviewCount = reviewMapper.hasReview(memberNo, placeNo);
		
		if(reviewCount > 0) {
			throw new BadRequestException("리뷰가 존재합니다.");
		}

		Review review = Review.builder()
				.memberNo(memberNo)
				.placeNo(placeNo)
				.reviewContent(normalizeContent(request.getReviewContent()))
				.rating(request.getRating())
				.build();

		if (reviewMapper.insertReview(review) != 1 || review.getReviewNo() == null) {
			throw new InternalServerException("리뷰 등록에 실패했습니다.");
		}

		uploadReviewImage(review.getReviewNo(), request.getImage());

		return ReviewCreateResponse.builder()
				.reviewNo(review.getReviewNo())
				.createDate(reviewMapper.selectCreateDate(review.getReviewNo()))
				.build();
	}

	@Transactional(readOnly = true)
	public ReviewDetailResponse getReviewForEdit(Long memberNo, Long placeNo, Long reviewNo) {
		requireLogin(memberNo);

		ReviewDetailResponse review = reviewMapper.selectReviewDetail(placeNo, reviewNo);
		if (review == null) {
			throw new NotFoundException("존재하지 않는 리뷰입니다.");
		}

		review.setImages(reviewMapper.selectReviewImgList(reviewNo).stream()
				.map(this::toReviewImageDto)
				.toList());
		return review;
	}

	@Transactional(readOnly = true)
	public ReviewListResponse getReviews(Long placeNo, int page) {
		requireActivePlace(placeNo);

		if (page < 1) {
			page = 1;
		}

		long totalElements = reviewMapper.countReviews(placeNo);
		int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);
		long offset = (long) (page - 1) * PAGE_SIZE;

		List<ReviewItemDto> content = totalElements == 0
				? List.of()
				: reviewMapper.selectReviews(placeNo, offset, PAGE_SIZE);
		bindReviewImages(content);

		return ReviewListResponse.builder()
				.summary(buildSummary(placeNo))
				.content(content)
				.currentPage(page)
				.size(PAGE_SIZE)
				.totalElements(totalElements)
				.totalPages(totalPages)
				.hasNext(page < totalPages)
				.hasPrevious(page > 1 && totalPages > 0)
				.build();
	}

	@Transactional
	public ReviewUpdateResponse updateReview(Long memberNo, Long placeNo, Long reviewNo, ReviewUpdateRequest request) {
		requireLogin(memberNo);
		Review review = requireOwnedReview(memberNo, placeNo, reviewNo,
				"본인이 작성한 리뷰만 수정할 수 있습니다.");

		if (reviewMapper.updateReview(reviewNo, request.getRating(), normalizeContent(request.getReviewContent())) != 1) {
			throw new InternalServerException("리뷰 수정에 실패했습니다.");
		}

		if (request.getImage() != null) {
			reviewMapper.softDeleteReviewImages(reviewNo);
			uploadReviewImage(reviewNo, request.getImage());
		}

		return ReviewUpdateResponse.builder()
				.reviewNo(review.getReviewNo())
				.updateDate(LocalDate.now().toString())
				.build();
	}

	@Transactional
	public void deleteReview(Long memberNo, Long placeNo, Long reviewNo) {
		requireLogin(memberNo);
		requireOwnedReview(memberNo, placeNo, reviewNo, "본인이 작성한 리뷰만 삭제할 수 있습니다.");

		if (reviewMapper.softDeleteReview(reviewNo) != 1) {
			throw new InternalServerException("리뷰 삭제에 실패했습니다.");
		}
		reviewMapper.softDeleteReviewImages(reviewNo);
	}

	private Review requireOwnedReview(Long memberNo, Long placeNo, Long reviewNo, String forbiddenMessage) {
		Review review = reviewMapper.selectReviewForAuth(reviewNo);
		if (review == null || !placeNo.equals(review.getPlaceNo())) {
			throw new NotFoundException("존재하지 않는 리뷰입니다.");
		}
		if (!memberNo.equals(review.getMemberNo())) {
			throw new ForbiddenException(forbiddenMessage);
		}
		return review;
	}

	private ReviewSummaryDto buildSummary(Long placeNo) {
		ReviewSummaryDto summary = reviewMapper.selectReviewSummary(placeNo);
		if (summary == null) {
			summary = new ReviewSummaryDto();
			summary.setAvgRating(0.0);
			summary.setTotalReviewCount(0);
		}

		Map<Integer, Integer> distribution = new LinkedHashMap<>();
		for (int score = 1; score <= 5; score++) {
			distribution.put(score, 0);
		}
		for (RatingCountDto row : reviewMapper.selectRatingDistribution(placeNo)) {
			if (row.getRating() != null && distribution.containsKey(row.getRating())) {
				distribution.put(row.getRating(), row.getCount());
			}
		}
		summary.setRatingDistribution(distribution);
		return summary;
	}

	private void bindReviewImages(List<ReviewItemDto> content) {
		if (content == null || content.isEmpty()) {
			return;
		}

		List<Long> reviewNos = content.stream().map(ReviewItemDto::getReviewNo).toList();
		Map<Long, List<String>> imagesByReview = new LinkedHashMap<>();
		for (ReviewImageDto image : reviewMapper.selectReviewImagesByReviewNos(reviewNos)) {
			imagesByReview.computeIfAbsent(image.getReviewNo(), key -> new ArrayList<>())
					.add(image.getImageUrl());
		}

		for (ReviewItemDto item : content) {
			item.setImages(imagesByReview.getOrDefault(item.getReviewNo(), new ArrayList<>()));
		}
	}

	private void uploadReviewImage(Long reviewNo, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return;
		}

		// 트랜잭션 롤백은 DB만 되돌리므로, 실패 시 이미 올라간 S3 객체는 직접 삭제
		String uploadedKey = null;
		try {
			FileSaveResult stored = fileService.store(file, REVIEW_IMAGE_DIRECTORY);
			uploadedKey = REVIEW_IMAGE_DIRECTORY + "/" + stored.getSaveName();

			ReviewImg reviewImg = ReviewImg.builder()
					.reviewNo(reviewNo)
					.originalName(file.getOriginalFilename())
					.saveName(stored.getSaveName())
					.imgPath(stored.getImgPath())
					.imgOrder(1)
					.build();

			if (reviewMapper.insertReviewImg(reviewImg) != 1) {
				throw new InternalServerException("리뷰 이미지 등록에 실패했습니다.");
			}
		} catch (RuntimeException e) {
			if (uploadedKey != null) {
				deleteUploadedImage(uploadedKey);
			}
			throw e;
		}
	}

	private void deleteUploadedImage(String key) {
		try {
			s3Service.deleteFile(key);
		} catch (RuntimeException ex) {
			log.warn("리뷰 이미지 등록 롤백 중 S3 객체 삭제 실패: {}", key, ex);
		}
	}

	private void requireLogin(Long memberNo) {
		if (memberNo == null) {
			throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
	}

	private void requireActivePlace(Long placeNo) {
		if (reviewMapper.countActivePlace(placeNo) == 0) {
			throw new NotFoundException("잘못된 접근입니다.");
		}
	}

	private ReviewImageDto toReviewImageDto(ReviewImg reviewImg) {
		ReviewImageDto dto = new ReviewImageDto();
		dto.setImgNo(reviewImg.getImgNo());
		dto.setReviewNo(reviewImg.getReviewNo());
		dto.setImgOrder(reviewImg.getImgOrder());
		dto.setImageUrl(reviewImg.getImgPath() + reviewImg.getSaveName());
		return dto;
	}

	private String normalizeContent(String content) {
		if (content == null || content.isBlank()) {
			return null;
		}
		return content.trim();
	}
}
