package com.github.mredjem.kafka.connect.mocks;

import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import java.util.Map;
import java.util.function.Consumer;

public class MockConfigurable implements Configurable<MockConfigurable> {

  private final Consumer<Object> onComponentRegister;

  public MockConfigurable(Consumer<Object> onComponentRegister) {
    this.onComponentRegister = onComponentRegister;
  }

  @Override
  public Configuration getConfiguration() {
    return null;
  }

  @Override
  public MockConfigurable property(String name, Object value) {
    return this;
  }

  @Override
  public MockConfigurable register(Class<?> componentClass) {
    return this;
  }

  @Override
  public MockConfigurable register(Class<?> componentClass, int priority) {
    return this;
  }

  @Override
  public MockConfigurable register(Class<?> componentClass, Class<?>... contracts) {
    return this;
  }

  @Override
  public MockConfigurable register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
    return this;
  }

  @Override
  public MockConfigurable register(Object component) {
    this.onComponentRegister.accept(component);

    return this;
  }

  @Override
  public MockConfigurable register(Object component, int priority) {
    return this;
  }

  @Override
  public MockConfigurable register(Object component, Class<?>... contracts) {
    return this;
  }

  @Override
  public MockConfigurable register(Object component, Map<Class<?>, Integer> contracts) {
    return this;
  }
}
