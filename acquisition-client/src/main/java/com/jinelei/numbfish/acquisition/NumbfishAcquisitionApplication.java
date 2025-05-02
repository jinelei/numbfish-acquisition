package com.jinelei.numbfish.acquisition;

import com.jinelei.numbfish.authorization.configuration.annotation.EnableFeignAuthorization;
import com.jinelei.numbfish.common.helper.SpringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SuppressWarnings("unused")
@EnableFeignAuthorization
@EnableFeignClients(basePackages = "com.jinelei.numbfish")
@Import(SpringHelper.class)
@SpringBootApplication(scanBasePackageClasses = {NumbfishAcquisitionApplication.class})
public class NumbfishAcquisitionApplication {
    private static final Logger log = LoggerFactory.getLogger(NumbfishAcquisitionApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NumbfishAcquisitionApplication.class, args);
    }

}
