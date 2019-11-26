package com.ultimalabs.sancho;

import com.ultimalabs.sancho.common.config.SanchoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SanchoApplicationTests {

	@Autowired
	private SanchoConfig config;

	@Test
	void contexLoads() throws Exception {
		assertNotNull(config);
	}
}
