/*
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */
#ifndef PULSE_FILE_H
#define PULSE_FILE_H

#include <array>

namespace pulse {

class File {
public:
  static const int a = 0;
  static const int b = 1;
  static const int c = 2;
  static const int d = 3;
  static const int e = 4;
  static const int f = 5;
  static const int g = 6;
  static const int h = 7;
  static const int NOFILE = 8;

  static const int SIZE = 8;
  static const std::array<int, SIZE> values;

  static bool isValid(int file);
  static int fromNotation(char notation);
  static char toNotation(int file);

private:
  static const char a_NOTATION = 'a';
  static const char b_NOTATION = 'b';
  static const char c_NOTATION = 'c';
  static const char d_NOTATION = 'd';
  static const char e_NOTATION = 'e';
  static const char f_NOTATION = 'f';
  static const char g_NOTATION = 'g';
  static const char h_NOTATION = 'h';

  File();
  ~File();
};

}

#endif
