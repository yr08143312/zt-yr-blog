package xyz.yangrui.ztyrblog.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("zt-yr-blog-API文档")
                .version("1.0")
                .build();
    }

    /*@Bean
    public Docket systemApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("IPP - 系统管理")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.crecgec.web.controller.system"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket baseInfoApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("IPP - 基础信息")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.crecgec.web.controller.baseinfo"))
                .paths(PathSelectors.any())
                .build();
    }*/

    @Bean
    public Docket baseInfoApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("访客页")
                .select()
                .apis(RequestHandlerSelectors.basePackage("xyz.yangrui.ztyrblog.controller.visitor"))
                .paths(PathSelectors.any())
                .build();
    }


}


