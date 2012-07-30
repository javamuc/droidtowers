/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.happydroids.droidtowers.TowerAssetManager;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.controllers.AvatarLayer;
import com.happydroids.droidtowers.controllers.AvatarSteeringManager;
import com.happydroids.droidtowers.controllers.PathSearchManager;
import com.happydroids.droidtowers.grid.GameGrid;
import com.happydroids.droidtowers.grid.GridPosition;
import com.happydroids.droidtowers.math.GridPoint;
import com.happydroids.droidtowers.pathfinding.TransitPathFinder;
import com.happydroids.droidtowers.utils.Random;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.happydroids.droidtowers.types.ProviderType.COMMERCIAL;
import static com.happydroids.droidtowers.types.ProviderType.RESTROOM;

public class Avatar extends GameObject {
  public static final float FRAME_DURATION = 0.25f;
  public static final float WALKING_ANIMATION_DURATION = FRAME_DURATION * 3;
  private static final float PATH_SEARCH_DELAY = 0f;
  private static final Set<Color> colors = Sets.newHashSet(Color.GREEN, Color.RED, Color.ORANGE, Color.MAGENTA, Color.PINK, Color.YELLOW);
  private static Iterator colorIterator;
  private final Animation walkAnimation;

  private float walkAnimationTime;

  private AvatarSteeringManager steeringManager;
  protected final GameGrid gameGrid;
  private boolean isEmployed;
  private boolean isResident;
  private float satisfactionShops;
  private float satisfactionFood;
  private Color myColor;
  private float lastPathFinderSearch;
  protected GridObject movingTo;
  private TransitPathFinder pathFinder;
  private boolean justWandered;
  private Room home;
  private float hungerLevel;
  private LinkedList<Object> lastVisitedPlaces;
  private float lastSearchedForHome = Float.MAX_VALUE;
  private boolean wanderingAround;

  public Avatar(AvatarLayer avatarLayer) {
    super();

    gameGrid = avatarLayer.getGameGrid();

    setPosition(-Random.randomInt(50, 200), TowerConsts.GROUND_HEIGHT);

    pickColor();

    TextureAtlas droidAtlas = getTextureAtlas();
    TextureAtlas.AtlasRegion stationary = droidAtlas.findRegion(addFramePrefix("stationary"));
    setSize(stationary.originalWidth, stationary.originalHeight);
    setRegion(stationary);

    walkAnimation = new Animation(FRAME_DURATION, droidAtlas.findRegions(addFramePrefix("walk")));
    walkAnimationTime = 0f;
    lastVisitedPlaces = Lists.newLinkedList();

    pathFinder = new TransitPathFinder(gameGrid, this instanceof Janitor);
    steeringManager = new AvatarSteeringManager(this, gameGrid, null);
  }

  protected String addFramePrefix(String frameName) {
    return "droid/" + frameName;
  }

  protected TextureAtlas getTextureAtlas() {
    return TowerAssetManager.textureAtlas("characters.txt");
  }

  public void beginNextAction() {
    justWandered = false;

    LinkedList<GridObject> objects = gameGrid.getObjects();
    if (objects != null) {
      if (home != null && !lastVisitedPlaces.contains(home)) {
        navigateToGridObject(home);
      } else {
        GridObject mostDesirable = null;
        for (GridObject gridObject : objects) {
          if (!gridObject.isConnectedToTransport() || lastVisitedPlaces.contains(gridObject)) {
            continue;
          } else if (gridObject.getVisitorQueue().size() > 5) {
//            should get rid of thundering herd problem?
            continue;
          }

          if (gridObject.provides(COMMERCIAL, RESTROOM)) {
            if (mostDesirable == null || mostDesirable.getDesirability() < gridObject.getDesirability()) {
              mostDesirable = gridObject;
            }
          }
        }

        if (mostDesirable != null) {
          navigateToGridObject(mostDesirable);
        }
      }
    } else {
      wanderAround();
    }
  }

  protected void wanderAround() {
    lastPathFinderSearch = 0f;
    GridPosition start = gameGrid.positionCache().getPosition(gameGrid.closestGridPoint(getX(), getY()));

    LinkedList<GridPosition> discoveredPath = Lists.newLinkedList();

    GridPoint gridSize = gameGrid.getGridSize();

    if (start.y == TowerConsts.LOBBY_FLOOR) {
      discoveredPath.add(gameGrid.positionCache().getPosition(Random.randomInt(0, gridSize.x), TowerConsts.LOBBY_FLOOR));
      discoveredPath.add(gameGrid.positionCache().getPosition(Random.randomInt(0, gridSize.x), TowerConsts.LOBBY_FLOOR));
    } else {
      for (int i = 1; i < 5; i++) {
        GridPosition positionRight = gameGrid.positionCache().getPosition(start.x + i, start.y);
        if (positionRight != null && positionRight.size() > 0) {
          discoveredPath.add(positionRight);
        } else {
          break;
        }
      }

      for (int i = 1; i < 5; i++) {
        GridPosition positionLeft = gameGrid.positionCache().getPosition(start.x - i, start.y);
        if (positionLeft != null && positionLeft.size() > 0) {
          discoveredPath.add(positionLeft);
        } else {
          break;
        }
      }

      List<GridPosition> positions = Lists.newArrayList(discoveredPath);
      int numPositions = positions.size();
      for (int i = 0; i < numPositions / 4; i++) {
        discoveredPath.add(positions.get(Random.randomInt(numPositions - 1)));
      }
    }

    steeringManager.setPath(discoveredPath);
    steeringManager.start();
  }

  protected void navigateToGridObject(GridObject gridObject) {
    if (gridObject == null) {
      wanderAround();
      return;
    }
    movingTo = gridObject;

    if (movingTo != null) {
      movingTo.getVisitorQueue().add(this);
    }

    GridPosition start = gameGrid.positionCache().getPosition(gameGrid.closestGridPoint(getX(), getY()));
    GridPosition goal = gameGrid.positionCache().getPosition(gridObject.getPosition());

    pathFinder.setStart(start);
    pathFinder.setGoal(goal);
    pathFinder.start();

    PathSearchManager.instance().queue(pathFinder);
  }

  private void createSteeringManagerFromPath() {
    if (pathFinder != null && pathFinder.wasSuccessful()) {
      steeringManager.setPath(pathFinder.getDiscoveredPath());
      steeringManager.start();
    }
  }

  public void afterReachingTarget() {
    if (movingTo != null) {
      movingTo.recordVisitor(this);
      lastVisitedPlaces.add(movingTo);
      if (lastVisitedPlaces.size() > 3) {
        lastVisitedPlaces.poll();
      }
    }

    if (!justWandered) {
      wanderAround();
    }
    movingTo = null;
  }

  @Override
  public void update(float timeDelta) {
    super.update(timeDelta);

    if (home == null) {
      lastSearchedForHome += timeDelta;
      if (lastSearchedForHome > 10f) {
        lastSearchedForHome = 0f;
        List<GridObject> rooms = gameGrid.getInstancesOf(Room.class);
        if (rooms != null) {
          GridObject mostDesirable = rooms.get(0);
          for (GridObject gridObject : rooms) {
            Room room = (Room) gridObject;
            if (room.isConnectedToTransport() && room.getNumSupportedResidents() > 0) {
              if (room.getNumResidents() == 0 || (room.getNumResidents() < room.getNumSupportedResidents() && mostDesirable.getDesirability() < room.getDesirability())) {
                mostDesirable = room;
              }
            }
          }

          setHome(mostDesirable);
        }
      }
    }

    hungerLevel -= 0.001f * timeDelta;

    lastPathFinderSearch += timeDelta;


    if (!steeringManager.isRunning()) {
      wanderAround();
    }
  }

  protected void pickColor() {
    if (colorIterator == null || !colorIterator.hasNext()) {
      colorIterator = colors.iterator();
    }

    setColor((Color) colorIterator.next());
  }

  public void cancelMovement() {
    if (steeringManager != null) {
      steeringManager.finished();
    }

    if (pathFinder != null) {
      PathSearchManager.instance().remove(pathFinder);
    }

    if (movingTo != null) {
      movingTo.getVisitorQueue().remove(this);
    }

    lastPathFinderSearch = PATH_SEARCH_DELAY;
  }

  public void murderDeathKill187() {
    markToRemove(true);
  }

  public void setHome(GridObject newHome) {
    home = (Room) newHome;

    if (home != null) {
      home.addResident(this);
      cancelMovement();
      setPosition(home.getWorldCenterBottom());
    }
  }

  public void recalculateCurrentPath() {
    cancelMovement();

    if (movingTo != null) {
      navigateToGridObject(movingTo);
    } else {
      wanderAround();
    }
  }


  public static final Predicate<GridObject> AVATAR_HOME_FILTER = new Predicate<GridObject>() {
    @Override
    public boolean apply(@Nullable GridObject input) {
      if (input instanceof Room) {
        Room room = (Room) input;
        return room.isConnectedToTransport() && (room.getNumResidents() == 0 || room.getNumResidents() < room.getNumSupportedResidents());
      }

      return false;
    }
  };

  @Override
  public void markToRemove(boolean b) {
    super.markToRemove(b);

    cancelMovement();
  }
}
