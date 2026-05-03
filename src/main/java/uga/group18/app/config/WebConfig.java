package uga.group18.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uga.group18.app.components.AuthInterceptor;

/**
 * Only /profile and /create-review require login.
 * Everything else (home, album view, song view, etc.) is public.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Autowired
    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // Only protect these specific paths
                .addPathPatterns("/profile", "/profile.html", "/create-review.html", "/logout");
    }
}