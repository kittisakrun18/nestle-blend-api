package com.nestle.blend.api.dao;

import com.nestle.blend.api.dto.ClaimSubmissionDto;
import com.nestle.blend.api.dto.ImportEntryDto;
import com.nestle.blend.api.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ClaimSubmissionDao {
    private Logger log = LogManager.getLogger(this.getClass());

    @Value("${nestle-api.base-url}")
    private String appBaseUrl;
    @Value("${app.api.path.prefix}")
    private String prefixPath;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int countAll(String search, UUID categoryId, LocalDate startDate, LocalDate endDate) throws Exception {
        this.log.info("## Param {} {} {} {}", search, categoryId, startDate, endDate);
        int result = 0;
        List<Object> params = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select count(t.id) as cnt_all ");
            sql.append(this.query(params, search, categoryId, startDate, endDate));

            this.log.info("----- Query -----");
            this.log.info(sql.toString());
            this.log.info(params);
            this.log.info("----- End Query -----");
            List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            for (Map<String, Object> map : maps) {
                Long cntAll = (Long) map.get("cnt_all");
                result = cntAll == null ? 0 : cntAll.intValue();
            }
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        this.log.info("Result {}", result);
        return result;
    }

    public List<ClaimSubmissionDto> getAllData(String search, UUID categoryId, LocalDate startDate, LocalDate endDate, int currentPage, int limit) throws Exception {
        this.log.info("## Param {} {} {} {} {} {}", search, categoryId, startDate, endDate, currentPage, limit);
        List<ClaimSubmissionDto> entities = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        try {
            int offset = (currentPage - 1) * limit;

            StringBuilder sql = new StringBuilder();
            sql.append(" select " +
                    "   t.id," +
                    "   t.import_entry_id," +
                    "   c.name as category_name, " +
                    "   e.seq_no," +
                    "   t.full_name," +
                    "   e.email::text," +
                    "   t.phone," +
                    "   t.age_u20," +
                    "   e.zone," +
                    "   t.id_card_file_path," +
                    "   t.receipt_file_path, " +
                    "   t.submitted_at ");
            sql.append(this.query(params, search, categoryId, startDate, endDate));
            sql.append(" order by t.submitted_at desc ");
            if (limit > 0) {
                sql.append(" limit ? offset ? ");
                params.add(limit);
                params.add(offset);
            }

            this.log.info("----- Query -----");
            this.log.info(sql.toString());
            this.log.info(params);
            this.log.info("----- End Query -----");
            List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            ClaimSubmissionDto entity;
            for (Map<String, Object> map : maps) {
                entity = new ClaimSubmissionDto();
                UUID id = (UUID) map.get("id");
                UUID importEntryId = (UUID) map.get("import_entry_id");
                Timestamp submittedAt = (Timestamp) map.get("submitted_at");
                LocalDateTime localDtSubmittedAt = submittedAt != null ? submittedAt.toLocalDateTime() : null;

                String idCardFilePath = (String) map.get("id_card_file_path");
                String receiptFilePath = (String) map.get("receipt_file_path");
                if(StringUtils.checkNotEmpty(idCardFilePath)){
                    idCardFilePath = appBaseUrl + prefixPath + "/resource?file=" + StringUtils.base64Encode(idCardFilePath);
                }
                if(StringUtils.checkNotEmpty(receiptFilePath)){
                    receiptFilePath = appBaseUrl + prefixPath + "/resource?file=" + StringUtils.base64Encode(receiptFilePath);
                }

                entity.setId(id);
                entity.setImportEntryId(importEntryId);
                entity.setCategoryName((String) map.get("category_name"));
                entity.setSeqNo((Integer) map.get("seq_no"));
                entity.setFullName((String) map.get("full_name"));
                entity.setEmail((String) map.get("email"));
                entity.setPhone((String) map.get("phone"));
                entity.setAgeU20((String) map.get("age_u20"));
                entity.setIdCardFilePath(idCardFilePath);
                entity.setReceiptFilePath(receiptFilePath);
                entity.setZone((String) map.get("zone"));
                entity.setSubmittedAt(localDtSubmittedAt);
                entities.add(entity);
            }
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        this.log.info("Response : {}", entities);
        return entities;
    }

    public String query(List<Object> params, String search, UUID categoryId, LocalDate startDate, LocalDate endDate) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append(" from claim_submission t " +
                "   inner join import_entry e on e.id = t.import_entry_id " +
                "   inner join category c on c.id = e.category_id " +
                "   where 1=1 ");
        if (search != null) {
            sql.append(" and concat(t.full_name, t.phone, e.email, e.zone, e.reward, c.name) like ? ");
            params.add("%" + search + "%");
        }
        if (categoryId != null) {
            sql.append(" and e.category_id = ? ");
            params.add(categoryId);
        }
        if (startDate != null && endDate != null) {
            sql.append(" and t.submitted_at between ? and ? ");
            params.add(startDate);
            params.add(endDate.plusDays(1));
        } else if (startDate != null) {
            sql.append(" and t.submitted_at >= ? ");
            params.add(startDate);
        } else if (endDate != null) {
            sql.append(" and t.submitted_at <= ? ");
            params.add(endDate.plusDays(1));
        }

        return sql.toString();
    }
}
