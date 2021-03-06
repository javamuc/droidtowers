/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.controllers;

public class AvatarState {
  public static final int STATIONARY = 1;
  public static final int MOVING = 1 << 1;
  public static final int USING_STAIRS = 1 << 2;
  public static final int USING_ELEVATOR = 1 << 3;
}
