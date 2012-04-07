/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.sparky.platform;

import javax.swing.*;

public class WindowsProtocolHandler implements PlatformProtocolHandler {
  public void initialize(String[] applicationArgs) {
    JOptionPane.showMessageDialog(null, applicationArgs, "URI!!", JOptionPane.INFORMATION_MESSAGE);
  }
}
