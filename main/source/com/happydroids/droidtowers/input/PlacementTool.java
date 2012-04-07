/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.happydroids.droidtowers.entities.GameLayer;
import com.happydroids.droidtowers.entities.GridObject;
import com.happydroids.droidtowers.entities.GridObjectPlacementState;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.gui.HeadsUpDisplay;
import com.happydroids.droidtowers.math.GridPoint;
import com.happydroids.droidtowers.money.PurchaseManager;
import com.happydroids.droidtowers.types.GridObjectType;

import java.util.List;

import static com.happydroids.droidtowers.input.InputSystem.Keys;

public class PlacementTool extends ToolBase {
  private GridObjectType gridObjectType;
  private GridObject gridObject;
  private Vector2 touchDownPointDelta;
  private boolean isDraggingGridObject;
  private PurchaseManager purchaseManager;
  private final InputCallback cancelPlacementInputCallback;
  private final GameGrid gameGrid;

  public PlacementTool(OrthographicCamera camera, List<GameLayer> gameLayers) {
    super(camera, gameLayers);

    gameGrid = getGameGrid();

    cancelPlacementInputCallback = new InputCallback() {
      public boolean run(float timeDelta) {
        InputSystem.instance().switchTool(GestureTool.PICKER, null);
        return true;
      }
    };

    InputSystem.instance().bind(new int[]{Keys.ESCAPE, Keys.BACK}, cancelPlacementInputCallback);
  }

  public void setup(GridObjectType gridObjectType) {
    this.gridObjectType = gridObjectType;
  }

  public boolean touchDown(int x, int y, int pointer) {
    Vector3 worldPoint = camera.getPickRay(x, y).getEndPoint(1);
    GridPoint gridPointAtFinger = gameGrid.closestGridPoint(worldPoint.x, worldPoint.y);
    makeGridObjectAtFinger_whenGridObjectIsNull(gridPointAtFinger);

    isDraggingGridObject = gridObject.getBounds().containsPoint(gridPointAtFinger);

    return true;
  }

  private void makeGridObjectAtFinger_whenGridObjectIsNull(GridPoint gridPointAtFinger) {
    if (gridObject == null) {
      gridObject = gridObjectType.makeGridObject(gameGrid);
      gridObject.setPosition(gridPointAtFinger);

      gameGrid.addObject(gridObject);
    } else {
      touchDownPointDelta = gridPointAtFinger.cpy().sub(gridObject.getPosition());
    }
  }

  public boolean pan(int x, int y, int deltaX, int deltaY) {
    if (isDraggingGridObject) {
      Vector3 worldPoint = camera.getPickRay(x, y).getEndPoint(1);
      Vector3 deltaPoint = camera.getPickRay(x + -deltaX, y + deltaY).getEndPoint(1);
      GridPoint gridPointAtFinger = gameGrid.closestGridPoint(worldPoint.x, worldPoint.y);

      if (touchDownPointDelta != null) {
        gridPointAtFinger.sub(touchDownPointDelta);
      }
      if (gridObject != null) {
        gridObject.setPosition(gridPointAtFinger);
      }

      return true;
    }

    return false;
  }

  @Override
  public void update(float deltaTime) {
    if (Gdx.app.getType().equals(Application.ApplicationType.Desktop)) {
      Vector3 worldPoint = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()).getEndPoint(1);
      GridPoint gridPointAtFinger = gameGrid.closestGridPoint(worldPoint.x, worldPoint.y);

      makeGridObjectAtFinger_whenGridObjectIsNull(gridPointAtFinger);

      if (gridObject != null) {
        gridObject.setPosition(gridPointAtFinger);
      }
    }
  }

  public boolean tap(int x, int y, int count) {
    if (count >= 2) {
      if (!gameGrid.canObjectBeAt(gridObject)) {
        HeadsUpDisplay.instance().showToast("This object cannot be placed here.");
        return false;
      } else {
        gridObject.setPlacementState(GridObjectPlacementState.PLACED);
      }

      if (purchaseManager != null) {
        purchaseManager.makePurchase();
      }

      gridObject = null;
      touchDownPointDelta = null;

      verifyAbilityToPurchase();
    }

    return false;
  }

  private void verifyAbilityToPurchase() {
    if (purchaseManager != null && !purchaseManager.canPurchase()) {
      InputSystem.instance().switchTool(GestureTool.PICKER, null);
    }
  }

  public void enterPurchaseMode() {
    purchaseManager = new PurchaseManager(gridObjectType);

    verifyAbilityToPurchase();
  }

  @Override
  public void cleanup() {
    if (gridObject != null) {
      gameGrid.removeObject(gridObject);
    }

    InputSystem.instance().unbind(new int[]{Keys.ESCAPE, Keys.BACK}, cancelPlacementInputCallback);
  }
}