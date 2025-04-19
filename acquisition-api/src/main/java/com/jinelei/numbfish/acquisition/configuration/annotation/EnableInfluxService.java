package com.jinelei.numbfish.acquisition.configuration.annotation;

import com.jinelei.numbfish.acquisition.configuration.InfluxConfiguration;
import com.jinelei.numbfish.acquisition.property.AcquisitionProperty;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@SuppressWarnings("unused")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({InfluxConfiguration.class, AcquisitionProperty.class})
public @interface EnableInfluxService {
}
