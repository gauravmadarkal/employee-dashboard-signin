package com.betsol.employeePortal.Configuration;

import com.betsol.employeePortal.Filter.CustomUrlFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<CustomUrlFilter> filterRegistrationBean() {
        FilterRegistrationBean < CustomUrlFilter > registrationBean = new FilterRegistrationBean();
        CustomUrlFilter customURLFilter = new CustomUrlFilter();

        registrationBean.setFilter(customURLFilter);
        registrationBean.addUrlPatterns("*");
        registrationBean.setOrder(1); //set precedence
        return registrationBean;
    }
}
