package br.com.pedrosa.resource.response;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int pageIndex,
        int pageSize
) {
    public PagedResponse(List<T> content, long totalElements, int pageSize, int pageIndex) {
        this(
                content,
                totalElements,
                (int) Math.ceil((double) totalElements / pageSize),
                pageIndex,
                pageSize
        );
    }
}