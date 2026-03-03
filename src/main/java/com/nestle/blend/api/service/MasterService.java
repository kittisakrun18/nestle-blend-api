package com.nestle.blend.api.service;

import com.nestle.blend.api.dto.CommonValueLabelDto;
import com.nestle.blend.api.entity.CategoryEntity;
import com.nestle.blend.api.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MasterService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CommonValueLabelDto> getCategories() {
        List<CommonValueLabelDto> results = new ArrayList<>();
        List<CategoryEntity> entities = this.categoryRepository.findAll();
        if (entities != null && !entities.isEmpty()) {
            CommonValueLabelDto dto = null;
            for (CategoryEntity e : entities) {
                dto = new CommonValueLabelDto();
                dto.setValue(e.getId().toString());
                dto.setLabel(e.getName());
                results.add(dto);
            }
        }

        return results;
    }
}
