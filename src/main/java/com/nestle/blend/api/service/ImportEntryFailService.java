package com.nestle.blend.api.service;

import com.nestle.blend.api.dao.ImportEntryFailDao;
import com.nestle.blend.api.dto.ImportEntryFailDto;
import com.nestle.blend.api.dto.PaginationRespDto;
import com.nestle.blend.api.utils.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ImportEntryFailService {

    @Autowired
    private ImportEntryFailDao importEntryFailDao;

    public PaginationRespDto<ImportEntryFailDto> findAll(LocalDate startDate, LocalDate endDate, int page, int limit) throws Exception {
        PaginationRespDto<ImportEntryFailDto> result = new PaginationRespDto<>();
        int rowTotal = this.importEntryFailDao.countAll(startDate, endDate);
        if (rowTotal > 0) {
            List<ImportEntryFailDto> items = this.importEntryFailDao.getAllData(startDate, endDate, page, limit);
            result.setItems(items);
        }
        result.setLimit(limit);
        result.setRowTotal(rowTotal);
        result.setCurrentPage(page);
        result.setTotalPage(PaginationUtils.genTotalPage(rowTotal, limit));

        return result;
    }
}
