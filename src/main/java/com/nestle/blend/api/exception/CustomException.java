package com.nestle.blend.api.exception;

public class CustomException extends Exception {

    private int code = 0;
    private String field;

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
    }
    public CustomException(int code, String message, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public int getCode(){
        return this.code;
    }
    public String getMessage(){
        return super.getMessage();
    }
    public String getField(){
        return this.field;
    }

}
