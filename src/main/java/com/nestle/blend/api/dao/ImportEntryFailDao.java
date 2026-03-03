package com.nestle.blend.api.dao;

import com.nestle.blend.api.dto.ImportEntryFailDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public class ImportEntryFailDao {
    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int countAll(LocalDate startDate, LocalDate endDate) throws Exception {
        this.log.info("## Param {} {}", startDate, endDate);
        int result = 0;
        List<Object> params = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select count(t.id) as cnt_all ");
            sql.append(this.query(params, startDate, endDate));

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

    public List<ImportEntryFailDto> getAllData(LocalDate startDate, LocalDate endDate, int currentPage, int limit) throws Exception {
        this.log.info("## Param {} {} {} {}", startDate, endDate, currentPage, limit);
        List<ImportEntryFailDto> entities = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        try {
            int offset = (currentPage - 1) * limit;

            StringBuilder sql = new StringBuilder();
            sql.append(" select " +
                    "   t.id," +
                    "   t.job_id," +
                    "   t.insert_date," +
                    "   t.category_name," +
                    "   t.sheet_name," +
                    "   t.excel_row_no," +
                    "   t.seq_no_raw," +
                    "   t.full_name," +
                    "   t.email," +
                    "   t.zone," +
                    "   t.reward," +
                    "   t.purchased_at_raw," +
                    "   t.reason ");
            sql.append(this.query(params, startDate, endDate));
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
            ImportEntryFailDto entity;
            for (Map<String, Object> map : maps) {
                entity = new ImportEntryFailDto();
                UUID id = (UUID) map.get("id");
                java.sql.Date date = (java.sql.Date) map.get("insert_date");
                entity.setId(id);
                entity.setJobId((String) map.get("job_id"));
                entity.setInsertDate(date != null ? date.toLocalDate() : null);
                entity.setCategoryName((String) map.get("category_name"));
                entity.setSheetName((String) map.get("sheet_name"));
                entity.setExcelRowNo((Integer) map.get("excel_row_no"));
                entity.setSeqNoRaw((String) map.get("seq_no_raw"));
                entity.setFullName((String) map.get("full_name"));
                entity.setEmail((String) map.get("email"));
                entity.setZone((String) map.get("zone"));
                entity.setReward((String) map.get("reward"));
                entity.setPurchasedAtRaw((String) map.get("purchase_at_raw"));
                entity.setReason((String) map.get("reason"));
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

    public String query(List<Object> params, LocalDate startDate, LocalDate endDate) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append(" from import_entry_fail t " +
                "   where 1=1 ");

        if (startDate != null && endDate != null) {
            sql.append(" and t.insert_date between ? and ? ");
            params.add(startDate);
            params.add(endDate);
        } else if (startDate != null) {
            sql.append(" and t.insert_date >= ? ");
            params.add(startDate);
        } else if (endDate != null) {
            sql.append(" and t.insert_date <= ? ");
            params.add(endDate);
        }

        return sql.toString();
    }
}
