/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.entities;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.happydroids.droidtowers.TowerAssetManager;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.actions.Action;
import com.happydroids.droidtowers.events.GridObjectBoundsChangeEvent;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.math.GridPoint;
import com.happydroids.droidtowers.types.ElevatorType;
import com.happydroids.droidtowers.types.ResizeHandle;

public class Elevator extends Transit {
  private Sprite topSprite;
  private Sprite shaftSprite;
  private Sprite emptyShaftSprite;
  private Sprite bottomSprite;
  private final BitmapFont floorFont;
  private ResizeHandle selectedResizeHandle;
  private boolean drawShaft;
  private Action onResizeAction;
  private ElevatorCar elevatorCar;
  static TextureAtlas elevatorAtlas;

  public Elevator(ElevatorType elevatorType, final GameGrid gameGrid) {
    super(elevatorType, gameGrid);

    if (elevatorAtlas == null) {
      elevatorAtlas = TowerAssetManager.textureAtlas(elevatorType.getAtlasFilename());
    }

    size.set(1, 3);
    topSprite = elevatorAtlas.createSprite("elevator/top");
    bottomSprite = elevatorAtlas.createSprite("elevator/bottom");
    shaftSprite = elevatorAtlas.createSprite("elevator/shaft");
    emptyShaftSprite = elevatorAtlas.createSprite("elevator/empty");
    floorFont = TowerAssetManager.bitmapFont("fonts/bank_gothic_32.fnt");
    drawShaft = true;

    elevatorCar = new ElevatorCar(this, elevatorAtlas);
  }

  @Override
  public Sprite getSprite() {
    return shaftSprite;
  }

  @Override
  public void update(float deltaTime) {
    super.update(deltaTime);

    elevatorCar.update(deltaTime);
  }

  @Override
  public void render(SpriteBatch spriteBatch) {
    Vector2 renderPosition = position.cpy();

    GridPoint gridPoint = new GridPoint(position.x, position.y);

    bottomSprite.setColor(renderColor);
    bottomSprite.setPosition(gridPoint.getWorldX(gameGrid), gridPoint.getWorldY(gameGrid));
    bottomSprite.draw(spriteBatch);

    Sprite shaftToRender = drawShaft ? shaftSprite : emptyShaftSprite;
    shaftToRender.setColor(renderColor);
    for (int y = (int) position.y + 1; y < position.y + size.y - 1; y++) {
      gridPoint.add(0, 1);

      shaftToRender.setPosition(gridPoint.getWorldX(gameGrid), gridPoint.getWorldY(gameGrid));
      shaftToRender.draw(spriteBatch);

      String labelText = String.valueOf(y - TowerConsts.LOBBY_FLOOR + 1);
      if (y == TowerConsts.LOBBY_FLOOR) {
        labelText = "L";
      } else if (y < TowerConsts.LOBBY_FLOOR) {
        labelText = "B" + (TowerConsts.LOBBY_FLOOR - y);
      }

      BitmapFont.TextBounds textBounds = floorFont.getBounds(labelText);
      floorFont.setColor(1, 1, 1, 0.5f);
      floorFont.draw(spriteBatch, labelText, gridPoint.getWorldX(gameGrid) + ((TowerConsts.GRID_UNIT_SIZE - textBounds.width) / 2), gridPoint.getWorldY(gameGrid) + ((TowerConsts.GRID_UNIT_SIZE - textBounds.height) / 2));
    }

    elevatorCar.setColor(renderColor);
    elevatorCar.draw(spriteBatch);

    gridPoint.add(0, 1);
    topSprite.setColor(renderColor);
    topSprite.setPosition(gridPoint.getWorldX(gameGrid), gridPoint.getWorldY(gameGrid));
    topSprite.draw(spriteBatch);
  }

  @Override
  public boolean tap(Vector2 gridPointAtFinger, int count) {
    if (count >= 2) {
      drawShaft = !drawShaft;
    }

    return true;
  }

  @Override
  public boolean touchDown(Vector2 gameGridPoint) {
    if (gameGridPoint.y == position.y + size.y - 1) {
      selectedResizeHandle = ResizeHandle.TOP;
    } else if (gameGridPoint.y == position.y) {
      selectedResizeHandle = ResizeHandle.BOTTOM;
    } else {
      selectedResizeHandle = null;
    }

    return selectedResizeHandle != null;
  }

  @Override
  public boolean pan(Vector2 gridPointAtFinger, Vector2 gridPointDelta) {
    GridPoint prevSize = size.cpy();
    GridPoint prevPosition = position.cpy();

    float newSize = -1;
    float newPosY = -1;
    if (selectedResizeHandle == ResizeHandle.TOP) {
      newSize = Math.max(gridPointDelta.y - position.y, 3);
    } else if (selectedResizeHandle == ResizeHandle.BOTTOM) {
      newPosY = gridPointDelta.y;
      newSize = position.y + size.y - gridPointDelta.y;
    }

    if (newSize < 3 || newSize > 17) {
      return true;
    }

    newSize = Math.max(newSize, 3);
    newSize = Math.min(newSize, 17);

    if (newSize != -1 && newSize != size.y) {
      size.y = newSize;
    }

    if (newPosY != -1) {
      position.y = Math.max(newPosY, 0);
    }

    broadcastEvent(new GridObjectBoundsChangeEvent(this, prevSize, prevPosition));

    return true;
  }

  @Override
  public GridPoint getContentSize() {
    GridPoint cpy = size.cpy();
    cpy.sub(0, 3);
    return cpy;
  }

  @Override
  public GridPoint getContentPosition() {
    GridPoint cpy = position.cpy();
    cpy.add(0, 1);
    return cpy;
  }

  public ElevatorCar getCar() {
    return elevatorCar;
  }
}
