/*
 * Copyright 2013-2014 the original author or authors.
 *
 * This file is part of Pulse Chess.
 *
 * Pulse Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pulse Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pulse Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxchess.pulse;

import com.fluxchess.jcpi.models.GenericColor;

final class Color {

  static final int MASK = 0x3;

  static final int WHITE = 0;
  static final int BLACK = 1;
  static final int NOCOLOR = 2;

  static final int[] values = {
      WHITE, BLACK
  };

  private Color() {
  }

  static int valueOf(GenericColor genericColor) {
    assert genericColor != null;

    switch (genericColor) {
      case WHITE:
        return WHITE;
      case BLACK:
        return BLACK;
      default:
        throw new IllegalArgumentException();
    }
  }

  static GenericColor toGenericColor(int color) {
    switch (color) {
      case WHITE:
        return GenericColor.WHITE;
      case BLACK:
        return GenericColor.BLACK;
      case NOCOLOR:
      default:
        throw new IllegalArgumentException();
    }
  }

  static boolean isValid(int color) {
    switch (color) {
      case WHITE:
      case BLACK:
        return true;
      case NOCOLOR:
        return false;
      default:
        throw new IllegalArgumentException();
    }
  }

  static int opposite(int color) {
    switch (color) {
      case WHITE:
        return BLACK;
      case BLACK:
        return WHITE;
      case NOCOLOR:
      default:
        throw new IllegalArgumentException();
    }
  }

}