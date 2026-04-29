package com.github.mredjem.kafka.connect.units;

import com.github.mredjem.kafka.connect.extensions.RbacAuthorizerExtension;
import com.github.mredjem.kafka.connect.extensions.filters.AuthenticationFilter;
import com.github.mredjem.kafka.connect.mocks.MockConfigurable;
import com.github.mredjem.kafka.connect.utils.TestUtils;
import org.apache.kafka.connect.health.ConnectClusterState;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Configurable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class RbacAuthorizerExtensionTest {

  private RbacAuthorizerExtension extension;

  @BeforeEach
  void before() {
    this.extension = new RbacAuthorizerExtension();
  }

  @Test
  void testAuthorizer() throws InterruptedException {
    this.extension.configure(TestUtils.load());

    Assertions.assertEquals("1", this.extension.version());

    CountDownLatch latch = new CountDownLatch(1);

    ConnectRestExtensionContext dummyContext = new DummyConnectRestExtensionContext(component -> {
      Assertions.assertInstanceOf(AuthenticationFilter.class, component);

      latch.countDown();
    });

    this.extension.register(dummyContext);

    boolean opened = latch.await(1L, TimeUnit.SECONDS);

    Assertions.assertTrue(opened);
  }

  private static final class DummyConnectRestExtensionContext implements ConnectRestExtensionContext {

    private final Consumer<Object> onComponentRegister;

    public DummyConnectRestExtensionContext(Consumer<Object> onComponentRegister) {
      this.onComponentRegister = onComponentRegister;
    }

    @Override
    public Configurable<? extends Configurable<?>> configurable() {
      return new MockConfigurable(this.onComponentRegister);
    }

    @Override
    public ConnectClusterState clusterState() {
      return null;
    }
  }
}
