package com.searchmetrics.exchangerates.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
public class FlywayTest {
	
	/* 
	 * Dummy test to get the class to execute. Real validation happens because spring.jpa.hibernate.ddl-auto=validate
	 * will validate that executed flyway scripts and @Entities are in sync. 
	 */
	@Test
	void setup() {
		assertTrue(true);
	}

}
