/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.unhappyrobot;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.happydroids.droidtowers.TowerGame;
import com.happydroids.server.HappyDroidService;
import com.happydroids.droidtowers.platform.DesktopBrowserUtil;
import com.happydroids.platform.HappyDroidsDesktopUncaughtExceptionHandler;
import com.happydroids.utils.OSValidator;

public class DesktopGame {
  public static void main(String[] args) {
    HappyDroidService.setDeviceOSName(OSValidator.getOSType());
    HappyDroidService.setDeviceOSVersion(System.getProperty("os.version"));

    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "TowerSim";
    config.resizable = false;
    config.width = 800;
    config.height = 480;
    config.useGL20 = true;
//    config.vSyncEnabled = false;

    TowerGame towerGame = new TowerGame();
    towerGame.setUncaughtExceptionHandler(new HappyDroidsDesktopUncaughtExceptionHandler());
    towerGame.setPlatformBrowserUtil(new DesktopBrowserUtil());

    new LwjglApplication(new LwjglApplicationShim(towerGame), config);
  }

}
