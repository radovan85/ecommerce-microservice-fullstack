package com.radovan.spring.interceptors;

import com.radovan.spring.services.PrometheusService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UnifiedMetricsInterceptor implements HandlerInterceptor {

    @Autowired
    private PrometheusService prometheus;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTimeNs = System.nanoTime();
        request.setAttribute("startTimeNs", startTimeNs);

        // 📈 Immediately increment request counter
        prometheus.increaseRequestCount();

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startAttr = request.getAttribute("startTimeNs");
        if (startAttr instanceof Long startTimeNs) {
            double durationSec = (System.nanoTime() - startTimeNs) / 1e9;

            // ⏱ Record response time
            prometheus.recordResponseTime(durationSec);
        }

        // 📊 Classify HTTP status
        prometheus.updateHttpStatusCount(response.getStatus());

        // 🧠 Bonus: tag by controller and method
        if (handler instanceof HandlerMethod method) {
            String controllerName = method.getBeanType().getSimpleName();
            String methodName = method.getMethod().getName();
            prometheus.updateControllerMethodTags(controllerName, methodName);
        }
    }
}
