package com.example.assistant;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BedrockRuntimeHintsTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	void test() throws Exception {

		var test = new BedrockRuntimeHints();
		for (var clzz : test.find(getClass().getPackageName())) {
			log.info("{}", clzz);
		}

	}

}