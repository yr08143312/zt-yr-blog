package xyz.yangrui.ztyrblog.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class WebUtils {

    /**
     * 获得 request
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        return getServletRequestAttributes().getRequest();
    }

    public static ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    public static HttpSession getSession(){
        return getRequest().getSession();
    }

    /**
     * 获得 response
     *
     * @return
     */
    public static HttpServletResponse getResponse() {
        return getServletRequestAttributes().getResponse();
    }
}
