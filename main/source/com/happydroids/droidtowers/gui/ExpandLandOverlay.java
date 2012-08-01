/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.google.common.eventbus.Subscribe;
import com.happydroids.droidtowers.Colors;
import com.happydroids.droidtowers.TowerAssetManager;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.audio.GameSoundController;
import com.happydroids.droidtowers.controllers.AvatarLayer;
import com.happydroids.droidtowers.entities.GridObject;
import com.happydroids.droidtowers.events.GridObjectBoundsChangeEvent;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.gui.events.CameraControllerEvent;
import com.happydroids.droidtowers.input.CameraController;
import com.happydroids.droidtowers.math.GridPoint;
import com.happydroids.droidtowers.platform.Display;
import com.happydroids.platform.Platform;

import static com.happydroids.droidtowers.TowerConsts.GAME_GRID_EXPAND_LAND_SIZE;

public class ExpandLandOverlay extends WidgetGroup {
  private static final int PADDING = 300;
  private final GameGrid gameGrid;
  private final AvatarLayer avatarLayer;
  private final CameraController cameraController;
  private Button leftButton;
  private Button rightButton;

  public ExpandLandOverlay(GameGrid gameGrid, AvatarLayer avatarLayer, CameraController cameraController) {
    this.gameGrid = gameGrid;
    this.avatarLayer = avatarLayer;
    this.cameraController = cameraController;
    cameraController.events().register(this);

    leftButton = new ExpandLandButton("left");
    leftButton.visible = false;
    leftButton.x = 5;
    leftButton.y = (Gdx.graphics.getHeight() - leftButton.height) / 2;
    addActor(leftButton);

    rightButton = new ExpandLandButton("right");
    rightButton.visible = false;
    rightButton.x = Display.getWidth() - rightButton.width - 5;
    rightButton.y = (Display.getHeight() - rightButton.height) / 2;
    addActor(rightButton);

    leftButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        expandLandToWest();
      }
    });

    rightButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        expandLandToEast();
      }
    });
  }

  private void expandLandToEast() {
    if (!Platform.getPurchaseManager().hasPurchasedUnlimitedVersion()) {
      new PurchaseDroidTowersUnlimitedPrompt().show();
      return;
    }

    GameSoundController.setSoundsAllowed(false);
    gameGrid.getGridSize().x += GAME_GRID_EXPAND_LAND_SIZE;
    gameGrid.updateWorldSize(false);

    for (GridObject gridObject : gameGrid.getObjects()) {
      gridObject.setPosition(gridObject.getPosition());
      gridObject.broadcastEvent(new GridObjectBoundsChangeEvent(gridObject, gridObject.getSize(), gridObject.getPosition()));
    }

    cameraController.panTo(gameGrid.getWorldSize().x, cameraController.getCamera().position.y, true);
    GameSoundController.setSoundsAllowed(true);
  }

  private void expandLandToWest() {
    if (!Platform.getPurchaseManager().hasPurchasedUnlimitedVersion()) {
      new PurchaseDroidTowersUnlimitedPrompt().show();
      return;
    }

    GameSoundController.setSoundsAllowed(false);
    gameGrid.getGridSize().x += GAME_GRID_EXPAND_LAND_SIZE;
    gameGrid.updateWorldSize(false);

    for (GridObject gridObject : gameGrid.getObjects()) {
      GridPoint position = gridObject.getPosition();
      gridObject.setPosition(position.x + GAME_GRID_EXPAND_LAND_SIZE, position.y);
      gridObject.adjustToNewLandSize();
    }
    avatarLayer.adjustAvatarPositions(GAME_GRID_EXPAND_LAND_SIZE);

    Vector3 cameraPosition = cameraController.getCamera().position.cpy();
    cameraController.getCamera().position.set(cameraPosition.x + (TowerConsts.GRID_UNIT_SIZE * GAME_GRID_EXPAND_LAND_SIZE), cameraPosition.y, cameraPosition.z);
    cameraController.panTo(0, cameraController.getCamera().position.y, true);
    GameSoundController.setSoundsAllowed(true);
  }

  public float getPrefWidth() {
    return 0;
  }

  public float getPrefHeight() {
    return 0;
  }

  private static class ExpandLandButton extends Button {

    public ExpandLandButton(String textureSuffix) {
      super(new ButtonStyle(
                                   makeNinePatch(textureSuffix, new Color(1f, 1f, 1f, 0.5f)),
                                   makeNinePatch(textureSuffix, Colors.ICS_BLUE),
                                   makeNinePatch(textureSuffix, new Color(1f, 1f, 1f, 0.75f)),
                                   0, 0, 0, 0
      ));
    }

    private static NinePatch makeNinePatch(String textureSuffix, Color color) {
      return new NinePatch(TowerAssetManager.textureFromAtlas("expand-land-" + textureSuffix, "hud/buttons.txt"), color);
    }

  }

  @Subscribe
  public void CameraController_onPan(CameraControllerEvent event) {
    leftButton.visible = event.position.x <= PADDING * event.zoom;
    rightButton.visible = event.position.x + (PADDING * event.zoom) >= gameGrid.getWorldSize().x;
  }
}
