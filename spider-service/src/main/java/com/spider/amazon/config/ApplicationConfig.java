package com.spider.amazon.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
@Slf4j
public class ApplicationConfig {

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        Provider<LocalDate> localDateProvider = new AbstractProvider<LocalDate>() {
            @Override
            public LocalDate get() {
                return LocalDate.now();
            }
        };

        Converter<String, LocalDate> toStringDate = new AbstractConverter<String, LocalDate>() {
            @Override
            protected LocalDate convert(String source) {
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                try{
                    LocalDate localDate = LocalDate.parse(source, format);
                    return localDate;
                }catch (Exception e){
                    e.printStackTrace();
                    log.info(e.getLocalizedMessage());
                    return null;
                }
            }
        };

        // map string to LocalDate
        modelMapper.createTypeMap(String.class, LocalDate.class);
        modelMapper.addConverter(toStringDate);
        modelMapper.getTypeMap(String.class, LocalDate.class).setProvider(localDateProvider);

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());

        return modelMapper;
    }
}

