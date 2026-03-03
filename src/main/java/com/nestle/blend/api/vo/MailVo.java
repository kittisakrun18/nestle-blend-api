package com.nestle.blend.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailVo {

    private boolean html;
    private List<MailRecipient> recipients;
    private String from;
    private String subject;
    private String message;

}