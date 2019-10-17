package de.mclonips.clubadministration.capella.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class ApplicationConfig {

    //All Configurations for Swagger

    /**
     * This method creates an {@link Docket} to use with Swagger.
     * <p>
     * To see raw swagger in a non human readable JSON go to [server]:[port]/${contextPath}/v2/api-docs .
     * To see the swagger-ui go to [server]:[port]/${contextPath}/swagger-ui.html .
     */
    @Bean
    public Docket api() {
        //noinspection Guava
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build();
    }
}
