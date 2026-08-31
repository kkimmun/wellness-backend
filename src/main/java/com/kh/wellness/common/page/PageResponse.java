package com.kh.wellness.common.page;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
	private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
    
    public static <T> PageResponse<T> empty(int page, int size){
    	return new PageResponse<>(List.of(), 0, page, size);
    }

    public PageResponse(List<T> content, long totalElements, int currentPage, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.size = size;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }
    
}
