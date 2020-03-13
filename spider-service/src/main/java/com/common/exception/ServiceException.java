package com.common.exception;

public class ServiceException extends RuntimeException {


    private String code;

    public ServiceException(String msg){
        super(msg);
    }
    public ServiceException(String code, String msg){
        super(code + ":" + msg);
        this.code = code;
    }

    public ServiceException(String code, Throwable e){
        super(code , e);
        this.code = code;
    }
    public ServiceException(String code, String msg, Throwable e){
        super(code + ":" + msg, e);
        this.code = code;
    }

    public String getCode(){
        return code;
    }

}
