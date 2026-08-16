package com.pilaslot;

import com.pilaslot.support.PostgreSqlTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Import(PostgreSqlTestContainerConfiguration.class)
class PilaSlotApplicationTests {

	@Autowired
	private Clock clock;

	@Value("${app.timezone}")
	private String timezone;

	@Test
	void contextLoads() {
		assertThat(clock.getZone()).isEqualTo(ZoneId.of(timezone));
	}

}
