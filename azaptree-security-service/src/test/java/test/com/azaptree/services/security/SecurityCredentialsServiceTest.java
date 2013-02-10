package test.com.azaptree.services.security;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.security.CredentialNames;
import com.azaptree.services.security.IncompatibleCredentialTypeException;
import com.azaptree.services.security.SecurityCredentialsService;
import com.azaptree.services.security.config.spring.SecurityCredentialsServiceConfig;

@ContextConfiguration(classes = { SecurityCredentialsServiceConfig.class })
public class SecurityCredentialsServiceTest extends AbstractTestNGSpringContextTests {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SecurityCredentialsService service;

	@Test
	public void test_convertCredentialToBytes() {
		final byte[] credentialBytes = service.convertCredentialToBytes(CredentialNames.PASSWORD.credentialName, "password");
		Assert.assertNotNull(credentialBytes);
		Assert.assertTrue(credentialBytes.length > 0);
	}

	@Test(expectedExceptions = { IncompatibleCredentialTypeException.class })
	public void test_convertCredentialToBytes_invalidName() {
		service.convertCredentialToBytes("INVALID NAME sdfsdfsfs", "password");
	}

	@Test
	public void test_getSupportedCredentials() {
		final Map<String, Class<?>> supportedCredentials = service.getSupportedCredentials();
		Assert.assertNotNull(supportedCredentials);
		Assert.assertFalse(supportedCredentials.isEmpty());

		for (Map.Entry<String, Class<?>> entry : supportedCredentials.entrySet()) {
			log.info("{} -> {}", entry.getKey(), entry.getValue().getName());
		}

		Assert.assertEquals(supportedCredentials.get(CredentialNames.PASSWORD.credentialName), String.class);
	}

	@Test
	public void test_isCredentialSupported() {
		Assert.assertTrue(service.isCredentialSupported(CredentialNames.PASSWORD.credentialName, "password"));
		Assert.assertFalse(service.isCredentialSupported(CredentialNames.PASSWORD.credentialName, 234));
		Assert.assertFalse(service.isCredentialSupported("asdfsdfsdfsd", 234));
		Assert.assertFalse(service.isCredentialSupported("sdgdfgf", "password"));
	}

}
