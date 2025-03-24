package com.lv2cd.svlcf.config;

import static com.lv2cd.svlcf.util.Constants.CONFIG_PREFIX;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(CONFIG_PREFIX)
@Getter
@Setter
public class SvlcfConfig {

  private String pdfOutputRootPath;
}
