package com.example.assistant;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class BedrockRuntimeHintsTest {

	@Test
	void find() throws Exception {
		var log = LoggerFactory.getLogger(getClass());
		var test = new BedrockRuntimeHints();
		for (var clzz : test.find(getClass().getPackageName())) {
			log.info("{}", clzz);
		}

	}

}