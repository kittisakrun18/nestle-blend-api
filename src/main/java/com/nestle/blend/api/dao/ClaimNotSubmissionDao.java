package com.nestle.blend.api.dao;

import com.nestle.blend.api.dto.ClaimNotSubmissionDto;
import com.nestle.blend.api.utils.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Repository
public class ClaimNotSubmissionDao {
    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int countAll(String search, UUID categoryId) throws Exception {
        this.log.info("## Param {} {}", search, categoryId);
        int result = 0;
        List<Object> params = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select count(t.id) as cnt_all ");
            sql.append(this.query(params, search, categoryId));

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

    public List<ClaimNotSubmissionDto> getAllData(String search, UUID categoryId, int currentPage, int limit) throws Exception {
        this.log.info("## Param {} {} {} {}", search, categoryId, currentPage, limit);
        List<ClaimNotSubmissionDto> entities = new ArrayList<>();
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
                    "   t.email::text as email," +
                    "   t.zone, " +
                    "   t.reward," +
                    "   t.purchased_at," +
                    "   t.email_sent_at," +
                    "   t.expires_at ");
            sql.append(this.query(params, search, categoryId));
            sql.append(" order by t.updated_at desc ");
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
            ClaimNotSubmissionDto entity;
            for (Map<String, Object> map : maps) {
                entity = new ClaimNotSubmissionDto();
                Date purchasedAt = (Date) map.get("purchased_at");
                LocalDate localDtPurchasedAt = purchasedAt != null ? purchasedAt.toLocalDate() : null;
                Timestamp emailSentAt = (Timestamp) map.get("email_sent_at");
                LocalDateTime localDtEmailSentAt = emailSentAt != null ? emailSentAt.toLocalDateTime() : null;
                Timestamp expireAt = (Timestamp) map.get("expires_at");
                LocalDateTime localDtExpireAt = expireAt != null ? expireAt.toLocalDateTime() : null;
                String emailSentAtStr = "";
                if (localDtEmailSentAt != null) {
                    emailSentAtStr = DateUtils.localeDateTimeToThaiStr(localDtEmailSentAt) + " " + DateUtils.localeDateTimeToTimeStr(localDtEmailSentAt);
                }
                String expireAtStr = "";
                if (localDtExpireAt != null) {
                    expireAtStr = DateUtils.localeDateTimeToThaiStr(localDtExpireAt) + " " + DateUtils.localeDateTimeToTimeStr(localDtExpireAt);
                }

                entity.setId((UUID) map.get("id"));
                entity.setCategoryId((UUID) map.get("category_id"));
                entity.setCategoryName((String) map.get("category_name"));
                entity.setSeqNo((String) map.get("seq_no"));
                entity.setFullName((String) map.get("full_name"));
                entity.setEmail((String) map.get("email"));
                entity.setZone((String) map.get("zone"));
                entity.setReward((String) map.get("reward"));
                entity.setZone((String) map.get("zone"));
                entity.setPurchasedAt(localDtPurchasedAt);
                entity.setEmailSentAt(localDtEmailSentAt);
                entity.setExpireAt(localDtExpireAt);
                entity.setPurchasedAtStr(DateUtils.localeDateToThaiStr(localDtPurchasedAt));
                entity.setEmailSentAtStr(emailSentAtStr);
                entity.setExpireAtStr(expireAtStr);

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

    public String query(List<Object> params, String search, UUID categoryId) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append(" from import_entry t " +
                "   inner join category c on c.id = t.category_id " +
                "   where t.used_at is null ");
        if (search != null) {
            sql.append(" and concat(t.full_name, t.phone, t.email, t.zone, t.reward, c.name) like ? ");
            params.add("%" + search + "%");
        }
        if (categoryId != null) {
            sql.append(" and t.category_id = ? ");
            params.add(categoryId);
        }

        return sql.toString();
    }
}
