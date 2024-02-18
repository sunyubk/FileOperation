package com.sy.fileoperation.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @ClassName RestApiResponse
 * @Description
 * @Author sunyu
 * @Date 2024/2/16 11:22
 * @Version 1.0
 **/
@Data
public class RestApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public RestApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> RestApiResponse<T> success() {
        return new RestApiResponse<>(200, "success", null);
    }

    public static <T> RestApiResponse<T> success(T data) {
        return new RestApiResponse<>(200, "success", data);
    }

    public static <T> RestApiResponse<T> success(String message) {
        return new RestApiResponse<>(200, message, null);
    }

    public static <T> RestApiResponse<T> success(String message, T data) {
        return new RestApiResponse<>(200, message, data);
    }

    public static<T> RestApiResponse<T> success(Integer code, String message, T data) {
        return new RestApiResponse<>(code, message, data);
    }

    public static <T> RestApiResponse<T> error(Integer code, String message) {
        return new RestApiResponse<>(code, message, null);
    }

    public static <T> RestApiResponse<T> error(Integer code, String message, T data) {
        return new RestApiResponse<>(code, message, data);
    }
}
