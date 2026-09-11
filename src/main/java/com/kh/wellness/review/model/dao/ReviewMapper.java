package com.kh.wellness.review.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.review.model.dto.RatingCountDto;
import com.kh.wellness.review.model.dto.ReviewDetailResponse;
import com.kh.wellness.review.model.dto.ReviewImageDto;
import com.kh.wellness.review.model.dto.ReviewItemDto;
import com.kh.wellness.review.model.dto.ReviewSummaryDto;
import com.kh.wellness.review.model.vo.Review;
import com.kh.wellness.review.model.vo.ReviewImg;

@Mapper
public interface ReviewMapper {

	int countActivePlace(Long placeNo);

	int insertReview(Review review);

	int insertReviewImg(ReviewImg reviewImg);

	String selectCreateDate(Long reviewNo);

	Review selectReviewForAuth(Long reviewNo);

	ReviewDetailResponse selectReviewDetail(
			@Param("placeNo") Long placeNo,
			@Param("reviewNo") Long reviewNo);

	int updateReview(
			@Param("reviewNo") Long reviewNo,
			@Param("rating") Integer rating,
			@Param("reviewContent") String reviewContent);

	int softDeleteReview(Long reviewNo);

	List<ReviewImg> selectReviewImgList(Long reviewNo);

	int softDeleteReviewImages(Long reviewNo);

	long countReviews(Long placeNo);

	ReviewSummaryDto selectReviewSummary(Long placeNo);

	List<RatingCountDto> selectRatingDistribution(Long placeNo);

	List<ReviewItemDto> selectReviews(
			@Param("placeNo") Long placeNo,
			@Param("offset") long offset,
			@Param("size") int size);

	List<ReviewImageDto> selectReviewImagesByReviewNos(@Param("reviewNos") List<Long> reviewNos);

	int hasReview(@Param("memberNo") Long memberNo, @Param("placeNo") Long placeNo);
}
