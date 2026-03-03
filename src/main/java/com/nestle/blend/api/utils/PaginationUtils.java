package com.nestle.blend.api.utils;

public class PaginationUtils {

    public static int genTotalPage(int rowTotal, int limit){
        int totalPage = (int) Math.ceil((double) rowTotal / limit);

        return totalPage;
    }

}
