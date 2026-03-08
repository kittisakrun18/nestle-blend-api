package com.nestle.blend.api.service;

import com.nestle.blend.api.dao.ClaimSubmissionDao;
import com.nestle.blend.api.dto.ClaimSubmissionDto;
import com.nestle.blend.api.dto.PaginationRespDto;
import com.nestle.blend.api.helper.ExcelStreamHelper;
import com.nestle.blend.api.utils.PaginationUtils;
import com.nestle.blend.api.utils.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ClaimSubmissionService {

    @Value("${nestle-api.base-url}")
    private String appBaseUrl;
    @Value("${app.api.path.prefix}")
    private String prefixPath;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ClaimSubmissionDao claimSubmissionDao;

    private SXSSFWorkbook workbook;
    private Sheet sheet;
    private ExcelStreamHelper helper;

    public PaginationRespDto<ClaimSubmissionDto> findAll(String search, UUID categoryId, LocalDate startDate, LocalDate endDate, int page, int limit) throws Exception {
        PaginationRespDto<ClaimSubmissionDto> result = new PaginationRespDto<>();
        int rowTotal = this.claimSubmissionDao.countAll(search, categoryId, startDate, endDate);
        if (rowTotal > 0) {
            List<ClaimSubmissionDto> items = this.claimSubmissionDao.getAllData(search, categoryId, startDate, endDate, page, limit);
            result.setItems(items);
        }
        result.setLimit(limit);
        result.setRowTotal(rowTotal);
        result.setCurrentPage(page);
        result.setTotalPage(PaginationUtils.genTotalPage(rowTotal, limit));

        return result;
    }

    public byte[] export(String search, UUID categoryId, LocalDate startDate, LocalDate endDate) throws Exception {
        this.workbook = new SXSSFWorkbook(100);
        this.helper = new ExcelStreamHelper();
        sheet = workbook.createSheet("ผู้ร่วมสนุก");

        writeHeaderLine();
        writeDataLines(search, categoryId, startDate, endDate);

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
        this.helper.createCell(this.workbook, row, index++, "วัน-เวลาที่ทำรายการ", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "ร้านค้า", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "โซน", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "เลขที่นั่ง", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "ชื่อ - นามสกุล", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "หมายเลขโทรศัพท์", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "อีเมล", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "อายุต่ำกว่า 20ปี", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "วันที่ซื้อสินค้า", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "รางวัลที่ได้รับ", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "สำเนาบัตรประชาชน", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "สำเนาใบเสร็จ", style, false, null);
        this.helper.createCell(this.workbook, row, index++, "สำเนาบัตรผู้ปกครอง", style, false, null);
    }

    private void writeDataLines(String search, UUID categoryId, LocalDate startDate, LocalDate endDate) throws Exception {
        AtomicInteger rowCount = new AtomicInteger(1);

        XSSFCellStyle style = this.helper.generateCellStyleContent(this.workbook);
        style.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle linkStyle = this.helper.generateCellStyleContent(this.workbook);
        Font linkFont = workbook.createFont();
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.getIndex());
        linkStyle.setFont(linkFont);
//        linkStyle.cloneStyleFrom(style);

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select " +
                "   to_char(t.submitted_at, 'DD/MM/YYYY') as submitted_at, " +
                "   c.name as category_name, " +
                "   e.zone," +
                "   e.seq_no," +
                "   t.full_name," +
                "   t.phone," +
                "   e.email::text," +
                "   t.age_u20," +
                "   to_char(e.purchased_at, 'DD/MM/YYYY') as purchased_at," +
                "   t.id_card_file_path," +
                "   t.receipt_file_path, " +
                "   t.parent_file_path, " +
                "   e.reward ");
        sql.append(this.claimSubmissionDao.query(params, search, categoryId, startDate, endDate));
        sql.append(" order by t.submitted_at desc ");

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
            String mainUrl = appBaseUrl + prefixPath + "/resource/download?file=";
            while (rs.next()) {
                Row row = sheet.createRow(rowCount.getAndIncrement());
                int columnCount = 0;

                String idCardFilePath = rs.getString("id_card_file_path");
                String receiptFilePath = rs.getString("receipt_file_path");
                String parentFilePath = rs.getString("parent_file_path");
                if(StringUtils.checkNotEmpty(idCardFilePath)){
                    idCardFilePath = mainUrl + StringUtils.base64Encode(idCardFilePath);
                }
                if(StringUtils.checkNotEmpty(receiptFilePath)){
                    receiptFilePath = mainUrl + StringUtils.base64Encode(receiptFilePath);
                }
                if(StringUtils.checkNotEmpty(parentFilePath)){
                    parentFilePath = mainUrl + StringUtils.base64Encode(parentFilePath);
                }

                this.helper.createCell(this.workbook, row, columnCount++, no.getAndIncrement(), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("submitted_at"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("category_name"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("zone"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("seq_no"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("full_name"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("phone"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("email"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("age_u20"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("purchased_at"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, rs.getString("reward"), style, false, null);
                this.helper.createCell(this.workbook, row, columnCount++, "Link", linkStyle, true, idCardFilePath);
                this.helper.createCell(this.workbook, row, columnCount++, "Link", linkStyle, true, receiptFilePath);
                this.helper.createCell(this.workbook, row, columnCount++, StringUtils.checkNotEmpty(parentFilePath) ? "Link" : "", linkStyle, true, parentFilePath);
            }
            return null;
        });

        int columnIndex = 0;
        sheet.setColumnWidth(columnIndex++, 10 * 256); // #
        sheet.setColumnWidth(columnIndex++, 25 * 256); // วัน-เวลาที่ทำรายการ
        sheet.setColumnWidth(columnIndex++, 25 * 256); // ร้านค้า
        sheet.setColumnWidth(columnIndex++, 30 * 256); // โซน
        sheet.setColumnWidth(columnIndex++, 20 * 256); // เลขที่นั่ง
        sheet.setColumnWidth(columnIndex++, 40 * 256); // ชื่อ - นามสกุล
        sheet.setColumnWidth(columnIndex++, 25 * 256); // หมายเลขโทรศัพท์
        sheet.setColumnWidth(columnIndex++, 40 * 256); // อีเมล
        sheet.setColumnWidth(columnIndex++, 20 * 256); // อายุต่ำกว่า 20ปี
        sheet.setColumnWidth(columnIndex++, 20 * 256); // วันที่ซื้อสินค้า
        sheet.setColumnWidth(columnIndex++, 40 * 256); // รางวัลที่ได้รับ
        sheet.setColumnWidth(columnIndex++, 25 * 256); // สำเนาบัตร
        sheet.setColumnWidth(columnIndex++, 25 * 256); // สำเนาใบเสร็จ
        sheet.setColumnWidth(columnIndex++, 25 * 256); // สำเนาบัตรผู้ปกครอง
    }
}
