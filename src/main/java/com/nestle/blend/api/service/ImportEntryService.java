package com.nestle.blend.api.service;

import com.nestle.blend.api.dao.ImportEntryDao;
import com.nestle.blend.api.dto.ImportEntryDto;
import com.nestle.blend.api.dto.PaginationRespDto;
import com.nestle.blend.api.entity.ImportEntryEntity;
import com.nestle.blend.api.repository.ImportEntryRepository;
import com.nestle.blend.api.utils.PaginationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ImportEntryService {

    @Autowired
    private ImportEntryDao importEntryDao;
    @Autowired
    private ImportEntryRepository importEntryRepository;

    public PaginationRespDto<ImportEntryDto> findAll(UUID categoryId, String emailStatus, int page, int limit) throws Exception {
        PaginationRespDto<ImportEntryDto> result = new PaginationRespDto<>();
        int rowTotal = this.importEntryDao.countAll(categoryId, emailStatus);
        if (rowTotal > 0) {
            List<ImportEntryDto> items = this.importEntryDao.getAllData(categoryId, emailStatus, page, limit);
            result.setItems(items);
        }
        result.setLimit(limit);
        result.setRowTotal(rowTotal);
        result.setCurrentPage(page);
        result.setTotalPage(PaginationUtils.genTotalPage(rowTotal, limit));

        return result;
    }

    public ImportEntryDto findByEntryId(UUID entryId) throws Exception {
        ImportEntryDto result = null;
        ImportEntryEntity entry = this.importEntryRepository.findFirstById(entryId);
        if (entry != null) {
            result = new ImportEntryDto();
            BeanUtils.copyProperties(entry, result);
        }

        return result;
    }
}
