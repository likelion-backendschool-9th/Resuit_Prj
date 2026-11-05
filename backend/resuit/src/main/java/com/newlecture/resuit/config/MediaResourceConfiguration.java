package com.newlecture.resuit.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MediaResourceConfiguration implements WebMvcConfigurer {

	private final String mediaRoot;

	public MediaResourceConfiguration(
			@Value("${resuit.media.root:src/main/media}") String mediaRoot) {
		this.mediaRoot = Path.of(mediaRoot).toAbsolutePath().normalize().toString()
				.replace('\\', '/').replaceAll("/+$", "");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/media/image/**")
				.addResourceLocations("file:" + mediaRoot + "/image/");
		registry.addResourceHandler("/media/video/**")
				.addResourceLocations("file:" + mediaRoot + "/video/");
	}
}
