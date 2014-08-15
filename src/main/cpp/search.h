/*
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */
#ifndef PULSE_SEARCH_H
#define PULSE_SEARCH_H

#include "board.h"
#include "movegenerator.h"
#include "evaluation.h"

#include <memory>
#include <chrono>
#include <thread>
#include <condition_variable>

namespace pulse {

/**
 * This class implements our search in a separate thread to keep the main
 * thread available for more commands.
 */
class Search {
public:
  static std::unique_ptr<Search> newDepthSearch(Board& board, int searchDepth);
  static std::unique_ptr<Search> newNodesSearch(Board& board, uint64_t searchNodes);
  static std::unique_ptr<Search> newTimeSearch(Board& board, uint64_t searchTime);
  static std::unique_ptr<Search> newInfiniteSearch(Board& board);
  static std::unique_ptr<Search> newClockSearch(Board& board,
    uint64_t whiteTimeLeft, uint64_t whiteTimeIncrement, uint64_t blackTimeLeft, uint64_t blackTimeIncrement, int movesToGo);
  static std::unique_ptr<Search> newPonderSearch(Board& board,
    uint64_t whiteTimeLeft, uint64_t whiteTimeIncrement, uint64_t blackTimeLeft, uint64_t blackTimeIncrement, int movesToGo);
  void start();
  void stop();
  void ponderhit();
  void run();

private:
  /**
   * This is our search timer for time & clock & ponder searches.
   */
  class Timer {
  public:
    Timer(bool& timerStopped, bool& doTimeManagement, int& currentDepth, int& initialDepth, bool& abort);

    void start(uint64_t searchTime);
    void stop();
  private:
    std::mutex waitMutex;
    std::condition_variable waitCondition;
    std::thread thread;

    bool& timerStopped;
    bool& doTimeManagement;
    int& currentDepth;
    int& initialDepth;

    bool& abort;

    void run(uint64_t searchTime);
  };

  std::thread thread;
  bool running = false;
  std::mutex startMutex;
  std::condition_variable startCondition;

  Board& board;
  Evaluation evaluation;

  // We will store a MoveGenerator for each ply so we don't have to create them
  // in search. (which is expensive)
  std::array<MoveGenerator, Depth::MAX_PLY> moveGenerators;

  // Depth search
  int searchDepth = Depth::MAX_DEPTH;

  // Nodes search
  uint64_t searchNodes = std::numeric_limits<uint64_t>::max();

  // Time & Clock & Ponder search
  uint64_t searchTime = 0;
  Timer timer;
  bool timerStopped = false;
  bool runTimer = false;
  bool doTimeManagement = false;

  // Search parameters
  MoveList rootMoves;
  bool abort = false;
  std::chrono::system_clock::time_point startTime;
  std::chrono::system_clock::time_point statusStartTime;
  uint64_t totalNodes = 0;
  int initialDepth = 1;
  int currentDepth = initialDepth;
  int currentMaxDepth = initialDepth;
  int currentMove = Move::NOMOVE;
  int currentMoveNumber = 0;
  std::array<MoveList::MoveVariation, Depth::MAX_PLY + 1> pv;

  Search(Board& board);

  void checkStopConditions();
  void updateSearch(int ply);
  void searchRoot(int depth, int alpha, int beta);
  int search(int depth, int alpha, int beta, int ply, bool isCheck);
  int quiescent(int depth, int alpha, int beta, int ply, bool isCheck);
  void savePV(int move, MoveList::MoveVariation& src, MoveList::MoveVariation& dest);
  void sendStatus();
  void sendStatus(bool force);
  void sendMove(MoveList::Entry& entry);
};

}

#endif
