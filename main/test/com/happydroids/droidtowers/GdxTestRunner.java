/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.happydroids.droidtowers.gamestate.server.TowerGameService;
import com.happydroids.droidtowers.tween.TweenSystem;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.HashMap;
import java.util.Map;

public class GdxTestRunner extends NonGLTestRunner implements ApplicationListener {
  private final Map<FrameworkMethod, RunNotifier> invokeInRender = new HashMap<FrameworkMethod, RunNotifier>();

  public GdxTestRunner(Class<?> klass) throws InitializationError {
    super(klass);

    LwjglApplicationConfiguration conf = new LwjglApplicationConfiguration();
    conf.width = 800;
    conf.height = 600;
    conf.title = "Gdx Test Runner";
    conf.useGL20 = true;
    new LwjglApplication(this, conf);
  }

  @Override
  protected void beforeTestRun() {
    TweenSystem.manager();
    TowerGameService.setInstance(new TowerGameService());
  }

  public void create() {
  }

  public void resume() {
  }

  public void render() {
    synchronized (invokeInRender) {
      for (Map.Entry<FrameworkMethod, RunNotifier> each : invokeInRender.entrySet()) {
        super.runChild(each.getKey(), each.getValue());
      }
      invokeInRender.clear();
    }
  }

  public void resize(int width, int height) {
  }

  public void pause() {
  }

  public void dispose() {
  }

  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    synchronized (invokeInRender) {
      //push for invoking in render phase, where gl context is available
      invokeInRender.put(method, notifier);
    }
    //wait until that test was invoked
    waitUntilInvokedInRenderMethod();
  }

  private void waitUntilInvokedInRenderMethod() {
    try {
      while (true) {
        Thread.sleep(1);
        synchronized (invokeInRender) {
          if (invokeInRender.isEmpty()) break;
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
