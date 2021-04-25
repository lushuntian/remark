package com.sunday.remark.controller.util;

/**
 * 通用的 rest http请求 API 返回参数格式。如：
 * {
 *     "status": "00000",
 *     "message": "成功",
 *     "data": [
 *         {
 *             "consumerId": 4,
 *             "content": "",
 *             "createTime": "2021-04-08 09:46:36",
 *             "header": "避免代码陷阱",
 *             "id": 4,
 *             "itemId": "2",
 *             "orderId": "o4",
 *             "score": 80
 *         }
 *     ]
 * }
 */
public class APIBody {
    private final String status;

    private final String message;

    /**
     * 只有这个不是final，利用这个特性，使用同线程覆盖来节省创建对象的开销
     */
    private Object data;

    private final static APIBody SUCCESS_BODY = new APIBody(RtnCodeEnum.SUCCESS);

    private final static ThreadLocal<APIBody> SUCCESS_LOCAL_BODY = new ThreadLocal<>();

    private APIBody(RtnCodeEnum rtnCodeEnum, String message) {
        this.status = rtnCodeEnum.getCode();
        this.message = message;
    }

    private APIBody(RtnCodeEnum rtnCodeEnum) {
        this(rtnCodeEnum, rtnCodeEnum.getDescription());
    }

    /**
     * 构建无数据成功返回对象
     */
    public static APIBody buildSuccess(){
        //确保可变属性不受影响
        SUCCESS_BODY.data = null;
        return SUCCESS_BODY;
    }

    /**
     * 以指定返回值构建成功返回对象。
     * @param data 数据对象
     */
    public static APIBody buildSuccess(Object data){
        APIBody body = SUCCESS_LOCAL_BODY.get();
        if (body == null){
            //在并发下可能导致重复创建对象，不过这只发生在最开始，没关系
            body = new APIBody(RtnCodeEnum.SUCCESS);
            SUCCESS_LOCAL_BODY.set(body);
        }

        //每个线程覆盖属于自己的属性,互不冲突
        body.data = data;
        return body;
    }

    public static APIBody buildError(RtnCodeEnum rtnCodeEnum, String message){
        //很少请求真的会执行失败，这个对象创建不必优化
        return new APIBody(rtnCodeEnum, message);
    }

    public static APIBody buildError(RtnCodeEnum rtnCodeEnum){
        return buildError(rtnCodeEnum, rtnCodeEnum.getDescription());
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
