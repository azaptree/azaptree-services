package test.com.azaptree.services.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.azaptree.services.eventbus.EventBusService;
import com.azaptree.services.eventbus.impl.EventBusServiceImpl;
import com.google.common.eventbus.Subscribe;

public class EventBusServiceTest {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private int longEventCounter = 0;
	private int stringEventCounter = 0;

	@Subscribe
	public void logEvent(final Long event) {
		log.info("EVENT LONG: {}", event);
		longEventCounter++;
	}

	@Subscribe
	public void logEvent(final String event) {
		log.info("EVENT String: {}", event);
		stringEventCounter++;
	}

	@Test
	public void test() {
		final EventBusService service = new EventBusServiceImpl("EventBusService");
		service.register(this);
		test(service);

		final EventBusServiceImpl service2 = new EventBusServiceImpl();
		service2.setBeanName("EventBusService");
		service2.init();
		service2.register(this);
		test(service2);
	}

	public void test(final EventBusService service) {
		longEventCounter = 0;
		stringEventCounter = 0;

		Assert.assertEquals(service.getEventBusName(), "EventBusService");

		Assert.assertEquals(longEventCounter, 0);

		service.post(10l);
		Assert.assertEquals(longEventCounter, 1);

		service.post(10l);
		Assert.assertEquals(longEventCounter, 2);

		service.post(10);
		Assert.assertEquals(longEventCounter, 2);

		Assert.assertEquals(stringEventCounter, 0);

		service.post("HELLO");
		Assert.assertEquals(stringEventCounter, 1);

		service.post("CIAO");
		Assert.assertEquals(stringEventCounter, 2);

		service.unregister(this);
		service.post("CIAO");
		Assert.assertEquals(stringEventCounter, 2);
		service.post(10l);
		Assert.assertEquals(longEventCounter, 2);

		service.register(this);
		service.post("CIAO");
		Assert.assertEquals(stringEventCounter, 3);
		service.post(10l);
		Assert.assertEquals(longEventCounter, 3);
	}

	@Test
	public void test_defaultConstructor() {
		final EventBusServiceImpl service2 = new EventBusServiceImpl();
		service2.setBeanName("EventBusService");
		service2.init();
		service2.register(this);
		test(service2);
	}

}
