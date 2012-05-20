/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.happydroids.droidtowers.Colors;
import com.happydroids.droidtowers.TowerAssetManager;

import static com.happydroids.droidtowers.platform.Display.scale;

public class ActionBar extends Table {
  public ActionBar() {
    setBackground(TowerAssetManager.ninePatch(TowerAssetManager.WHITE_SWATCH, Colors.DARKER_GRAY, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear));
    defaults().pad(scale(8)).fillX();
    add(FontManager.Roboto24.makeLabel("Actions!")).expandX();

  }
}