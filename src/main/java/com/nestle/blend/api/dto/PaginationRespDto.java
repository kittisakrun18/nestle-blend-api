package com.nestle.blend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationRespDto<T> implements Serializable {

    private int limit;
    private int rowTotal;
    private int currentPage;
    private int totalPage;
    private List<T> items;

}
