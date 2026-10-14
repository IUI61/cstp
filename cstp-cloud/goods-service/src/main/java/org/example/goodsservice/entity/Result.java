package org.example.goodsservice.entity;

import lombok.Data;

@Data
public class Result {
    private int code;// 状态码，200成功，其他失败
    private String message;
    private Object data;
    //成功（无数据）
    public static Result success(){
        Result result = new Result();
        result.code = 200;
        result.message = "操作成功";
        return result;
    }
    //成功（有数据）
    public static Result success(Object data){
        Result result = new Result();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }
    //失败
    public static Result error(int code,String message){
        Result result = new Result();
        result.code = code;
        result.message = message;
        return result;
    }
}
