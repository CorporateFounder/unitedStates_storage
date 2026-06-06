package com.example.website.citu;

import com.example.website.citu.config.NumberFormattingAspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
	}

	@Test
	void numberFormattingAspectIsDisabledByDefault() {
		assertTrue(applicationContext.getBeansOfType(NumberFormattingAspect.class).isEmpty());
	}

}
