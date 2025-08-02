// package com.example.projectmanagement.security;

// import org.springframework.context.annotation.Configuration;
// // import org.springframework.web.method.support.HandlerMethodArgumentResolver;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// // import java.util.List;

// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     // private final CurrentUserArgumentResolver currentUserArgumentResolver;

//     // public WebConfig(CurrentUserArgumentResolver resolver) {
//     //     this.currentUserArgumentResolver = resolver;
//     // }

//     // @Override
//     // public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//     //     resolvers.add(currentUserArgumentResolver);
//     // }
//     @Override
//     public void addCorsMappings(CorsRegistry registry) {
//         registry.addMapping("/**")
//             .allowedOrigins("http://localhost:5173")
//             .allowedMethods("*")
//             .allowedHeaders("*");
//     }
// }


// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
// public class WebConfig {
//     @Bean
//     public WebMvcConfigurer corsConfigurer() {
//         return new WebMvcConfigurer() {
//             @Override
//             public void addCorsMappings(CorsRegistry registry) {
//                 registry.addMapping("/api/**")
//                         .allowedOrigins("http://localhost:5173")
//                         .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                         .allowedHeaders("*")
//                         .allowCredentials(true);
//             }
//         };
//     }
// }
