/*
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */
#ifndef PULSE_BOARD_H
#define PULSE_BOARD_H

#include "bitboard.h"
#include "color.h"
#include "castling.h"
#include "square.h"
#include "piece.h"
#include "depth.h"

#include <random>

namespace pulse {

/**
 * This is our internal board.
 */
class Board {
public:
  static const unsigned int STANDARDBOARD = 518;

  static const int MAX_MOVES = Depth::MAX_PLY + 1024;
  static const int BOARDSIZE = 128;

  static const std::vector<std::vector<int>> pawnDirections;
  static const std::vector<int> knightDirections;
  static const std::vector<int> bishopDirections;
  static const std::vector<int> rookDirections;
  static const std::vector<int> queenDirections;
  static const std::vector<int> kingDirections;

  std::array<int, BOARDSIZE> board;

  std::array<Bitboard, Color::SIZE> pawns;
  std::array<Bitboard, Color::SIZE> knights;
  std::array<Bitboard, Color::SIZE> bishops;
  std::array<Bitboard, Color::SIZE> rooks;
  std::array<Bitboard, Color::SIZE> queens;
  std::array<Bitboard, Color::SIZE> kings;

  std::array<int, Color::SIZE> material = {};

  std::array<int, Castling::SIZE> castlingRights;
  int enPassantSquare = Square::NOSQUARE;
  int activeColor = Color::WHITE;
  int halfmoveClock = 0;

  uint64_t zobristKey = 0;

  Board(unsigned int id);
  Board(const std::string& fen);
  Board(const Board& board);

  Board& operator=(const Board& board);
  bool operator==(const Board& board) const;
  bool operator!=(const Board& board) const;

  std::string toString();
  int getFullmoveNumber();
  bool isRepetition();
  bool hasInsufficientMaterial();
  void makeMove(int move);
  void undoMove(int move);
  bool isCheck();
  bool isAttacked(int targetSquare, int attackerColor);

private:
  class Zobrist {
  public:
    std::array<std::array<uint64_t, BOARDSIZE>, Piece::SIZE> board;
    std::array<uint64_t, Castling::SIZE> castlingRights;
    std::array<uint64_t, BOARDSIZE> enPassantSquare;
    uint64_t activeColor;

    static Zobrist& instance();
  private:
    std::independent_bits_engine<std::mt19937, 8, uint64_t> generator;

    Zobrist();

    uint64_t next();
  };

  class State {
  public:
    uint64_t zobristKey = 0;
    std::array<int, Castling::SIZE> castlingRights;
    int enPassantSquare = Square::NOSQUARE;
    int halfmoveClock = 0;

    State();
  };

  int halfmoveNumber = 2;

  // We will save some board parameters in a State before making a move.
  // Later we will restore them before undoing a move.
  std::array<State, MAX_MOVES> stack;
  int stackSize = 0;

  const Zobrist& zobrist;

  Board();

  void setFullmoveNumber(int fullmoveNumber);
  void put(int piece, int square);
  int remove(int square);
  void clearCastlingRights(int castling);
  void clearCastling(int square);
  bool isAttacked(int targetSquare, int attackerPiece, const std::vector<int>& moveDelta);
  bool isAttacked(int targetSquare, int attackerPiece, int queenPiece, const std::vector<int>& moveDelta);
};

}

#endif
