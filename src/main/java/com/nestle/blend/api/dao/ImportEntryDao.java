package com.nestle.blend.api.dao;

import com.nestle.blend.api.dto.ImportEntryDto;
import com.nestle.blend.api.dto.ImportEntryFailDto;
import com.nestle.blend.api.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ImportEntryDao {
    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int countAll(UUID categoryId, String emailStatus) throws Exception {
        this.log.info("## Param {} {}", categoryId, emailStatus);
        int result = 0;
        List<Object> params = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select count(t.id) as cnt_all ");
            sql.append(this.query(params, categoryId, emailStatus));

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

    public List<ImportEntryDto> getAllData(UUID categoryId, String emailStatus, int currentPage, int limit) throws Exception {
        this.log.info("## Param {} {} {} {}", categoryId, emailStatus, currentPage, limit);
        List<ImportEntryDto> entities = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        try {
            int offset = (currentPage - 1) * limit;

            StringBuilder sql = new StringBuilder();
            sql.append(" select " +
                    "   t.id," +
                    "   t.category_id," +
                    "   c.name as category_name," +
                    "   t.seq_no," +
                    "   t.full_name," +
                    "   t.email::text," +
                    "   t.zone," +
                    "   t.reward," +
                    "   t.purchased_at," +
                    "   t.email_status, " +
                    "   t.email_error, " +
                    "   t.email_sent_at ");
            sql.append(this.query(params, categoryId, emailStatus));
            sql.append(" order by t.created_at desc ");
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
            ImportEntryDto entity;
            for (Map<String, Object> map : maps) {
                entity = new ImportEntryDto();
                UUID id = (UUID) map.get("id");
                UUID catId = (UUID) map.get("category_id");
                Timestamp sentAt = (Timestamp) map.get("email_sent_at");
                LocalDateTime emailSentAt = sentAt != null ? sentAt.toLocalDateTime() : null;
                java.sql.Date purchasedAt = (java.sql.Date) map.get("purchased_at");
                entity.setId(id);
                entity.setCategoryId(catId);
                entity.setCategoryName((String) map.get("category_name"));
                entity.setSeqNo((Integer) map.get("seq_no"));
                entity.setFullName((String) map.get("full_name"));
                entity.setEmail((String) map.get("email"));
                entity.setZone((String) map.get("zone"));
                entity.setReward((String) map.get("reward"));
                entity.setPurchasedAt(purchasedAt != null ? purchasedAt.toLocalDate() : null);
                entity.setEmailStatus((String) map.get("email_status"));
                entity.setEmailError((String) map.get("email_error"));
                entity.setEmailSentAt(emailSentAt);
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

    public String query(List<Object> params, UUID categoryId, String emailStatus) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append(" from import_entry t " +
                "   inner join category c on c.id = t.category_id " +
                "   where 1=1 ");
        if (categoryId != null) {
            sql.append(" and t.category_id = ? ");
            params.add(categoryId);
        }
        if (StringUtils.checkNotEmpty(emailStatus)) {
            sql.append(" and t.email_status = ? ");
            params.add(emailStatus);
        }

        return sql.toString();
    }
}
