package com.hechang.codeagent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * SpringMVC的 JSON 配置类
 */
@JsonComponent
public class JSONConfig {

    /**
     * 添加 Long类型转String类型配置，解决前端的Long类型数据精度丢失问题
     * @param builder：创建一个ObjectMapper对象
     * @return ObjectMapper对象
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(
            Jackson2ObjectMapperBuilder builder
    ) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        // Long类型转String类型
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        //registerModule: 注册一个Module，从而将Module注册到ObjectMapper中
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
