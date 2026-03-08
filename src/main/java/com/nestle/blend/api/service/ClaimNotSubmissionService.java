package com.nestle.blend.api.service;

import com.nestle.blend.api.dao.ClaimNotSubmissionDao;
import com.nestle.blend.api.dto.ClaimNotSubmissionDto;
import com.nestle.blend.api.dto.PaginationRespDto;
import com.nestle.blend.api.helper.ExcelStreamHelper;
import com.nestle.blend.api.utils.DateUtils;
import com.nestle.blend.api.utils.PaginationUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ClaimNotSubmissionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ClaimNotSubmissionDao claimNotSubmissionDao;

    private SXSSFWorkbook workbook;
    private Sheet sheet;
    private ExcelStreamHelper helper;

    public PaginationRespDto<ClaimNotSubmissionDto> findAll(String search, UUID categoryId, int page, int limit) throws Exception {
        PaginationRespDto<ClaimNotSubmissionDto> result = new PaginationRespDto<>();
        int rowTotal = this.claimNotSubmissionDao.countAll(search, categoryId);
        if (rowTotal > 0) {
            List<ClaimNotSubmissionDto> items = this.claimNotSubmissionDao.getAllData(search, categoryId, page, limit);
            result.setItems(items);
        }
        result.setLimit(limit);
        result.setRowTotal(rowTotal);
        result.setCurrentPage(page);
        result.setTotalPage(PaginationUtils.genTotalPage(rowTotal, limit));

        return result;
    }

    public byte[] export(String search, UUID categoryId) throws Exception {
        this.workbook = new SXSSFWorkbook(100);
        this.helper = new ExcelStreamHelper();
        sheet = workbook.createSheet("ผู้ที่ยังไม่ตอบกลับร่วมสนุก");

        writeHeaderLine();
        writeDataLines(search, categoryId);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            workbook.write(bos);
        } finally {
            bos.close();
            workbook.close();
        }

        return bos.toByteArray();
    }

    private void writeHeaderLine() throws Exception {
        int index = 0;
        Row row = sheet.createRow(0);
        XSSFCellStyle style = this.helper.generateCellStyleHeader(workbook, row);

        this.helper.createCell(this.workbook, row, index++, "#", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "ร้านค้า", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "โซน", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "เลขที่นั่ง", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "ชื่อ - นามสกุล", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "อีเมล", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "วันที่ซื้อสินค้า", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "รางวัลที่ได้รับ", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "วันที่ส่งอีเมล", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "วันที่หมดอายุร่วมสนุก", style, false, null);
    }

    private void writeDataLines(String search, UUID categoryId) throws Exception {
        AtomicInteger rowCount = new AtomicInteger(1);

        XSSFCellStyle style = this.helper.generateCellStyleContent(this.workbook);
        style.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle linkStyle = this.helper.generateCellStyleContent(this.workbook);
        Font linkFont = workbook.createFont();
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.getIndex());
        linkStyle.setFont(linkFont);

        List<Object> params = new ArrayList<>();
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
        sql.append(this.claimNotSubmissionDao.query(params, search, categoryId));
        sql.append(" order by t.updated_at desc ");

        this.jdbcTemplate.setFetchSize(1000);

        AtomicInteger no = new AtomicInteger(1);
        jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(1000);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return ps;
        }, (ResultSet rs) -> {
            while (rs.next()) {
                Row row = sheet.createRow(rowCount.getAndIncrement());
                int columnCount = 0;
                LocalDate purchaseAt = rs.getDate("purchased_at").toLocalDate();
                LocalDateTime emailSentAt = rs.getTimestamp("email_sent_at").toLocalDateTime();
                LocalDateTime expireAt = rs.getTimestamp("expires_at").toLocalDateTime();

                String purchaseAtStr = DateUtils.localeDateToThaiStr(purchaseAt);
                String emailSentAtStr = DateUtils.localeDateTimeToThaiStr(emailSentAt) + " " + DateUtils.localeDateTimeToTimeStr(emailSentAt);
                String expireAtStr = DateUtils.localeDateTimeToThaiStr(expireAt) + " " + DateUtils.localeDateTimeToTimeStr(expireAt);

                this.helper.createCell(this.workbook, row, columnCount++, no.getAndIncrement(), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("category_name"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("zone"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("seq_no"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("full_name"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("email"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, purchaseAtStr, style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("reward"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, emailSentAtStr, style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, expireAtStr, style, false, null);
            }
            return null;
        });

        int columnIndex = 0;
        sheet.setColumnWidth(columnIndex++, 10 * 256); // #
        sheet.setColumnWidth(columnIndex++, 30 * 256); // ร้านค้า
        sheet.setColumnWidth(columnIndex++, 30 * 256); // โซน
        sheet.setColumnWidth(columnIndex++, 25 * 256); // เลขที่นั่ง
        sheet.setColumnWidth(columnIndex++, 40 * 256); // ชื่อ - นามสกุล
        sheet.setColumnWidth(columnIndex++, 40 * 256); // อีเมล
        sheet.setColumnWidth(columnIndex++, 25 * 256); // วันที่ซื้อสินค้า
        sheet.setColumnWidth(columnIndex++, 40 * 256); // รางวัลที่ได้รับ
        sheet.setColumnWidth(columnIndex++, 30 * 256); // วันที่ส่งอีเมล
        sheet.setColumnWidth(columnIndex++, 30 * 256); // วันที่หมดอายุร่วมสนุก
    }
}
