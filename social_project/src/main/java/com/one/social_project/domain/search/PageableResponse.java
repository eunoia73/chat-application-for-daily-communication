package com.one.social_project.domain.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageableResponse {
    private int pageNumber;
    private int pageSize;
    private long offset;
    private boolean last;

    public PageableResponse(int pageNumber, int pageSize, long offset, boolean last) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.offset = offset;
        this.last = last;
    }
}
