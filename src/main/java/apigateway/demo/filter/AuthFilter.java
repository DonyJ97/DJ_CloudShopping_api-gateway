package apigateway.demo.filter;

import apigateway.demo.constant.RedisConstant;
import apigateway.demo.utils.CookieUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @ Author     ：djq.
 * @ Date       ：Created in 9:29 2020/3/7
 * @ Description：权限拦截（区分买家和卖家）
 * @ Modified By：
 * @Version: $
 */
public class AuthFilter extends ZuulFilter{
    RequestContext requestContext = RequestContext.getCurrentContext();
    HttpServletRequest request = requestContext.getRequest();
    //private RedisTemplate redisTemplate;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }


    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER  - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run(){
        /* /order/create 只能买家访问（cookie里有openid）
        *  /order/finish 只能卖家访问（cookie里有token，并且对应redis中的值）
        *  /product/list 都可以访问
        * */
        if("/order/order/create".equals(request.getRequestURI())){
            Cookie cookie = CookieUtil.get(request,"openid");
            if(cookie == null || StringUtils.isEmpty(cookie.getValue())){
                requestContext.setSendZuulResponse(false);
                requestContext.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
            }
        }
        if("/order/order/finish".equals(request.getRequestURI())){
            Cookie cookie = CookieUtil.get(request,"token");
            if(cookie == null || StringUtils.isEmpty(cookie.getValue())
            || StringUtils.isEmpty(redisTemplate.opsForValue().get(String.format(RedisConstant.TOKEN_TEMPLATE,cookie.getValue())))){
                requestContext.setSendZuulResponse(false);
                requestContext.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
            }
        }
        return null;
    }
}
