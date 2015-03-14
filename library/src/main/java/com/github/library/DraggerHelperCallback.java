/*
* Copyright (C) 2015 Pedro Paulo de Amorim
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.library;

import android.support.v4.widget.ViewDragHelper;
import android.view.View;

public class DraggerHelperCallback extends ViewDragHelper.Callback {

  private int dragState = 0;
  private int dragOffset = 0;

  private DraggerView draggerView;
  private View dragView;

  private DraggerPosition dragPosition;
  private DraggerHelperListener draggerListener;

  public DraggerHelperCallback(DraggerView draggerView, View dragView, DraggerPosition dragPosition,
      DraggerHelperListener draggerListener) {
    this.draggerView = draggerView;
    this.dragView = dragView;
    this.dragPosition = DraggerPosition.values()[dragPosition.getPosition()];
    this.draggerListener = draggerListener;
  }

  @Override public boolean tryCaptureView(View child, int pointerId) {
    return child.equals(dragView);
  }

  @Override public int clampViewPositionHorizontal(View child, int left, int dx) {
    final int leftBound;
    final int rightBound;
    if(dragPosition == DraggerPosition.RIGHT && left > 0) {
      leftBound = draggerView.getPaddingLeft();
      rightBound = (int) draggerListener.dragHorizontalDragRange();
    } else if(dragPosition == DraggerPosition.LEFT && left < 0) {
      leftBound = (int) -draggerListener.dragHorizontalDragRange();
      rightBound = draggerView.getPaddingLeft();
    } else {
      leftBound = 0;
      rightBound = 0;
    }
    return Math.min(Math.max(left, leftBound), rightBound);
  }

  @Override public int clampViewPositionVertical(View child, int top, int dy) {
    final int topBound;
    final int bottomBound;
    if(dragPosition == DraggerPosition.TOP && top > 0) {
      topBound = draggerView.getPaddingTop();
      bottomBound = (int) draggerListener.dragVerticalDragRange();
    } else if(dragPosition == DraggerPosition.BOTTOM && top < 0) {
      topBound = (int) -draggerListener.dragVerticalDragRange();
      bottomBound = draggerView.getPaddingTop();
    } else {
      topBound = 0;
      bottomBound = 0;
    }
    return Math.min(Math.max(top, topBound), bottomBound);
  }

  @Override public int getViewVerticalDragRange(View child) {
    return (int) draggerListener.dragVerticalDragRange();
  }

  @Override public int getViewHorizontalDragRange(View child) {
    return (int) draggerListener.dragHorizontalDragRange();
  }

  @Override public void onViewDragStateChanged(int state) {
    if(state == dragState) {
      return;
    }
    if ((dragState == ViewDragHelper.STATE_DRAGGING
        || dragState == ViewDragHelper.STATE_SETTLING)
        && state == ViewDragHelper.STATE_IDLE) {
      switch (dragPosition) {
        case LEFT:
        case RIGHT:
          if(dragOffset == draggerListener.dragHorizontalDragRange()) {
            draggerListener.finishActivity();
          }
          break;
        default:
        case TOP:
        case BOTTOM:
          if(dragOffset == draggerListener.dragVerticalDragRange()) {
            draggerListener.finishActivity();
          }
          break;
      }
    }
    dragState = state;
  }

  @Override public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
    super.onViewPositionChanged(changedView, left, top, dx, dy);

    if(dragPosition == DraggerPosition.TOP || dragPosition == DraggerPosition.BOTTOM) {
      dragOffset = Math.abs(top);
    } else {
      dragOffset = Math.abs(left);
    }
    float fractionScreen = (float) dragOffset / draggerListener.dragVerticalDragRange();
    if (fractionScreen >= 1) {
      fractionScreen = 1;
    }
    if (draggerListener != null) {
      draggerListener.onViewPositionChanged(fractionScreen);
    }
  }

  @Override public void onViewReleased(View releasedChild, float xVel, float yVel) {
    super.onViewReleased(releasedChild, xVel, yVel);
    switch (dragPosition) {
      case LEFT:
        if (draggerView.isDragViewAboveTheMiddle()) {
          draggerView.closeFromCenterToLeft();
        } else {
          draggerView.moveToCenter();
        }
        break;
      case RIGHT:
        if (draggerView.isDragViewAboveTheMiddle()) {
          draggerView.closeFromCenterToRight();
        } else {
          draggerView.moveToCenter();
        }
        break;
      case TOP:
      default:
        if (draggerView.isDragViewAboveTheMiddle()) {
          draggerView.closeFromCenterToBottom();
        } else {
          draggerView.moveToCenter();
        }
        break;
      case BOTTOM:
        if (draggerView.isDragViewAboveTheMiddle()) {
          draggerView.closeFromCenterToTop();
        } else {
          draggerView.moveToCenter();
        }
        break;
    }
  }

  public DraggerPosition getDragPosition() {
    return dragPosition;
  }

  public void setDragPosition(DraggerPosition dragPosition) {
    this.dragPosition = dragPosition;
  }

}
