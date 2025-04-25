package com.jinelei.numbfish.acquisition;

import com.jinelei.numbfish.auth.configuration.annotation.EnableFeignAuthorization;
import com.jinelei.numbfish.common.helper.SpringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SuppressWarnings("unused")
@EnableFeignAuthorization
@EnableFeignClients(basePackages = "com.jinelei.numbfish")
@Import(SpringHelper.class)
@SpringBootApplication(scanBasePackageClasses = {NumbfishAcquisitionClientApplication.class})
public class NumbfishAcquisitionClientApplication {
    private static final Logger log = LoggerFactory.getLogger(NumbfishAcquisitionClientApplication.class);

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext run = SpringApplication.run(NumbfishAcquisitionClientApplication.class, args);
        log.info("""
                        
                        ----------------------------------------------------------
                        \t\
                        Application '{}' is running!
                        \t\
                        Local: \t\thttp://localhost:{}
                        \t\
                        External: \thttp://{}:{}
                        \t\
                        Doc: \t\thttp://{}:{}/doc.html
                        ----------------------------------------------------------""",
                SpringHelper.getProperty("spring.application.name"),
                SpringHelper.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                SpringHelper.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                SpringHelper.getProperty("server.port"));
    }

}
