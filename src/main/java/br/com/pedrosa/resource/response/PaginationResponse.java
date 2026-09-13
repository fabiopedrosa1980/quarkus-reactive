package br.com.pedrosa.resource.response;

import java.util.List;

public record PaginationResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int pageIndex,
        int pageSize
) {
    public PaginationResponse(List<T> content, long totalElements, int pageSize, int pageIndex) {
        this(
                content,
                totalElements,
                (int) Math.ceil((double) totalElements / pageSize),
                pageIndex,
                pageSize
        );
    }
}