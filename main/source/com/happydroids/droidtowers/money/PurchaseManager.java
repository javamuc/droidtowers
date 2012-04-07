/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.money;

import com.badlogic.gdx.Gdx;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.entities.Player;
import com.happydroids.droidtowers.gui.Dialog;
import com.happydroids.droidtowers.gui.OnClickCallback;
import com.happydroids.droidtowers.gui.ResponseType;
import com.happydroids.droidtowers.types.GridObjectType;
import com.happydroids.droidtowers.utils.Random;

import java.text.NumberFormat;

public class PurchaseManager {
  public static final String LOG_TAG = "PurchaseManager";
  private GridObjectType gridObjectType;
  private int purchaseCount;

  public PurchaseManager(GridObjectType gridObjectType) {
    this.gridObjectType = gridObjectType;
    purchaseCount = 0;
  }

  public boolean canPurchase() {
    Gdx.app.log(LOG_TAG, "Checking purchase: " + gridObjectType.getName());
    if (purchaseCount > 0 && !gridObjectType.continuousPlacement()) {
      Gdx.app.log(LOG_TAG, "Cannot continuously purchase: " + gridObjectType.getName());
      return false;
    }

    if (gridObjectType.getCoins() != 0 && Player.instance().getCoins() < gridObjectType.getCoins()) {
      displayCurrencyDialog();
      return false;
    }

    Gdx.app.log(LOG_TAG, "Allowing purchase: " + gridObjectType.getName());
    return true;
  }

  private void displayCurrencyDialog() {
    Gdx.app.log(LOG_TAG, "Out of money for purchase: " + gridObjectType.getName());

    final int moneyFromVinnie = Random.randomInt(1000, 10000);
    String message = String.format("So it looks like you ran out of money.\n\nLuckily, Cousin Vinnie has offered to loan you %s%s.\n\nSo, how about it?", TowerConsts.CURRENCY_SYMBOL, NumberFormat.getInstance().format(moneyFromVinnie));
    new Dialog().setTitle("Not enough money :(").setMessage(message).addButton("Yes", new OnClickCallback() {
      @Override
      public void onClick(Dialog dialog) {
        Player.instance().addCurrency(moneyFromVinnie);
        dialog.dismiss();
      }
    }).addButton(ResponseType.NEGATIVE, "No thanks!", new OnClickCallback() {
      @Override
      public void onClick(Dialog dialog) {
        dialog.dismiss();
      }
    }).show();
  }

  public void makePurchase() {
    Gdx.app.log(LOG_TAG, "Made purchase: " + gridObjectType.getName());
    Player player = Player.instance();

    player.subtractCurrency(gridObjectType.getCoins());
    player.addExperience(gridObjectType.getExperienceAward());
    purchaseCount++;
  }
}