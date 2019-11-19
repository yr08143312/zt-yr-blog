package xyz.yangrui.ztyrblog.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by FIGO on 2019/3/5
 * @Description：结果数据对象
 */

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class ResultDTO<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int STATUS_CODE_SUCCESS = 1;

    public static final int STATUS_CODE_BUSINESS_ERROR = 0;

    public static final int STATUS_CODE_REQUEST_WRONG = 111;

    public static final int STATUS_CODE_SYSTEM_ERROR = 999;

    public static final int STATUS_CODE_NOT_AUTHORIZED = -1;
    public static final int STATUS_CODE_NO_PERMISSION = -99;

    public static final int STATUS_CODE_NOT_LOGIN = -999;

    public static final Map<Integer,String> codeMsgMap = new HashMap<>();

    static{
        codeMsgMap.put(STATUS_CODE_SUCCESS,"SUCCESS");
        codeMsgMap.put(STATUS_CODE_BUSINESS_ERROR,"Business processing exception");
        codeMsgMap.put(STATUS_CODE_SYSTEM_ERROR,"system error");
        codeMsgMap.put(STATUS_CODE_NOT_AUTHORIZED,"no authorized");
        codeMsgMap.put(STATUS_CODE_NO_PERMISSION,"no no no");
        codeMsgMap.put(STATUS_CODE_NOT_LOGIN,"user not login");
        codeMsgMap.put(STATUS_CODE_REQUEST_WRONG,"request Param error");
    }

    private T data;

    private int code;

    private String message;

    private boolean success;

    private Map<String,Object> addInfo;

    public ResultDTO(){
        this.setCode(STATUS_CODE_SUCCESS);
        this.setMessage(getMsgByCode(this.getCode()));
    }

    public ResultDTO(T data) {
        this();
        this.data = data;
    }

    public ResultDTO(Integer code, T data) {
        this.code = code;
        this.message = getMsgByCode(this.getCode());
        this.data = data;
    }

    public ResultDTO(Integer code, String message, T data) {
        this.setCode(code);
        this.message = message;
        this.data = data;
    }

    public boolean getSuccess(){
        return this.success;
    }

    public ResultDTO setCode(int code){
        this.code = code;
        this.message = getMsgByCode(code);
        this.success = this.code == STATUS_CODE_SUCCESS;
        return this;
    }

    public static ResultDTO execute(Object data) {

        return new ResultDTO(STATUS_CODE_SUCCESS, null, data);
    }

    public static ResultDTO execute(String message, Object data) {
        return new ResultDTO(STATUS_CODE_SUCCESS, message, data);
    }

    public static String getMsgByCode(int code){
        String msg = codeMsgMap.get(code);
        return StringUtils.isNotBlank(msg) ? msg : "error";
    }

    public ResultDTO putAddInfos(String Key,Object o){
        if(StringUtils.isEmpty(Key)){
            return this;
        }
        if(this.addInfo == null){
            this.addInfo = new HashMap<>();
        }
        addInfo.put(Key,o);
        return this;
    }

    public static ResultDTO ok(){
        return new ResultDTO();
    }

    public static ResultDTO fail(){
        ResultDTO result = new ResultDTO();
        result.setCode(STATUS_CODE_BUSINESS_ERROR);
        return result;
    }

    public static ResultDTO fail(String msg){
        ResultDTO result = new ResultDTO();
        result.setCode(STATUS_CODE_BUSINESS_ERROR);
        return StringUtils.isNotEmpty(msg) ? result.setMessage(msg) : result;
    }

    public static ResultDTO fail(int code,String msg){
        ResultDTO result = new ResultDTO();
        result.setCode(code);
        return StringUtils.isNotEmpty(msg) ? result.setMessage(msg) : result;
    }
}
