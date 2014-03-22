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

import com.fluxchess.jcpi.commands.*;
import com.fluxchess.jcpi.models.*;
import com.fluxchess.jcpi.options.CheckboxOption;
import com.fluxchess.jcpi.options.Options;
import com.fluxchess.jcpi.protocols.IProtocolHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class PulseTest {

  private final BlockingQueue<IEngineCommand> commands = new LinkedBlockingQueue<>();

  @Before
  public void setUp() {
    commands.clear();

    // Put a default command list into the queue for each test
    commands.add(new EngineInitializeRequestCommand());
    CheckboxOption ponderOption = Options.newPonderOption(true);
    commands.add(new EngineSetOptionCommand(
        ponderOption.name,
        ponderOption.defaultValue));
    commands.add(new EngineDebugCommand(false, true));
    commands.add(new EngineDebugCommand(true, false));
    commands.add(new EngineReadyRequestCommand("test"));
    commands.add(new EngineNewGameCommand());
  }

  @Test
  public void testDepth() throws InterruptedException {
    final GenericMove[] bestMove = {null};
    final GenericMove[] ponderMove = {null};
    final int[] depth = {0};

    final Semaphore semaphore = new Semaphore(0);

    // Test searching to a depth of 2
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard(GenericBoard.STANDARDSETUP),
        Arrays.asList(new GenericMove(GenericPosition.c2, GenericPosition.c4))));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setDepth(2);
    commands.add(command);

    new Pulse(new ProtocolHandler() {
      @Override
      public void send(ProtocolBestMoveCommand command) {
        super.send(command);

        bestMove[0] = command.bestMove;
        ponderMove[0] = command.ponderMove;

        semaphore.release();
      }

      @Override
      public void send(ProtocolInformationCommand command) {
        super.send(command);

        if (command.getDepth() != null) {
          depth[0] = command.getDepth();
        }
      }
    }).run();

    assertTrue(semaphore.tryAcquire(10000, TimeUnit.MILLISECONDS));

    assertNotNull(bestMove[0]);
    assertNotNull(ponderMove[0]);
    assertEquals(2, depth[0]);
  }

  @Test
  public void testNodes() throws InterruptedException {
    final GenericMove[] bestMove = {null};
    final GenericMove[] ponderMove = {null};
    final long[] nodes = {0};

    final Semaphore semaphore = new Semaphore(0);

    // Test if we can search only 1 node
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard(GenericBoard.STANDARDSETUP),
        Arrays.asList(new GenericMove(GenericPosition.c2, GenericPosition.c4))));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setNodes(1L);
    commands.add(command);

    new Pulse(new ProtocolHandler() {
      @Override
      public void send(ProtocolBestMoveCommand command) {
        super.send(command);

        bestMove[0] = command.bestMove;
        ponderMove[0] = command.ponderMove;

        semaphore.release();
      }

      @Override
      public void send(ProtocolInformationCommand command) {
        super.send(command);

        if (command.getNodes() != null) {
          nodes[0] = command.getNodes();
        }
      }
    }).run();

    assertTrue(semaphore.tryAcquire(10000, TimeUnit.MILLISECONDS));

    assertNotNull(bestMove[0]);
    assertNull(ponderMove[0]);
    assertEquals(1L, nodes[0]);
  }

  @Test
  public void testMoveTime() throws IllegalNotationException {
    // Test searching for 1 second
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard("8/4K3/8/7p/5QkP/6P1/8/8 b - - 2 76"), new ArrayList<GenericMove>()));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setMoveTime(1000L);
    commands.add(command);

    long startTime = System.currentTimeMillis();
    new Pulse(new ProtocolHandler()).run();
    long stopTime = System.currentTimeMillis();

    assertTrue(stopTime - startTime >= 1000L);
  }

  @Test
  public void testFastMoveTime() {
    // Test seaching for 1 millisecond, which should be stable
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard(GenericBoard.STANDARDSETUP),
        Arrays.asList(new GenericMove(GenericPosition.c2, GenericPosition.c4))));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setMoveTime(1L);
    commands.add(command);

    new Pulse(new ProtocolHandler()).run();
  }

  @Test
  public void testMoves() {
    // Test searching only specific moves
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard(GenericBoard.STANDARDSETUP),
        Arrays.asList(new GenericMove(GenericPosition.c2, GenericPosition.c4))));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setSearchMoveList(Arrays.asList(
        new GenericMove(GenericPosition.b7, GenericPosition.b6),
        new GenericMove(GenericPosition.f7, GenericPosition.f5)));
    commands.add(command);
    new Timer(true).schedule(new TimerTask() {
      @Override
      public void run() {
        commands.add(new EngineStopCalculatingCommand());
      }
    }, 1000);

    new Pulse(new ProtocolHandler()).run();
  }

  @Test
  public void testInfinite() {
    // Test searching infinitely
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard(GenericBoard.STANDARDSETUP),
        Arrays.asList(new GenericMove(GenericPosition.c2, GenericPosition.c4))));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setInfinite();
    commands.add(command);
    new Timer(true).schedule(new TimerTask() {
      @Override
      public void run() {
        commands.add(new EngineStopCalculatingCommand());
      }
    }, 1000);

    new Pulse(new ProtocolHandler()).run();
  }

  @Test
  public void testClock() {
    // Test if our time management works
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard(GenericBoard.STANDARDSETUP),
        Arrays.asList(new GenericMove(GenericPosition.c2, GenericPosition.c4))));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setClock(GenericColor.WHITE, 1000L);
    command.setClockIncrement(GenericColor.WHITE, 0L);
    command.setClock(GenericColor.BLACK, 1000L);
    command.setClockIncrement(GenericColor.BLACK, 0L);
    commands.add(command);

    new Pulse(new ProtocolHandler()).run();
  }

  @Test
  public void testMovesToGo() {
    // Test our time management with moves to go
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard(GenericBoard.STANDARDSETUP),
        Arrays.asList(new GenericMove(GenericPosition.c2, GenericPosition.c4))));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setClock(GenericColor.WHITE, 1000L);
    command.setClockIncrement(GenericColor.WHITE, 0L);
    command.setClock(GenericColor.BLACK, 1000L);
    command.setClockIncrement(GenericColor.BLACK, 0L);
    command.setMovesToGo(20);
    commands.add(command);

    new Pulse(new ProtocolHandler()).run();
  }

  @Test
  public void testPonder() {
    // Test if ponder works with time management
    commands.add(new EngineAnalyzeCommand(
        new GenericBoard(GenericBoard.STANDARDSETUP),
        Arrays.asList(new GenericMove(GenericPosition.c2, GenericPosition.c4))));
    EngineStartCalculatingCommand command = new EngineStartCalculatingCommand();
    command.setClock(GenericColor.WHITE, 1000L);
    command.setClockIncrement(GenericColor.WHITE, 0L);
    command.setClock(GenericColor.BLACK, 1000L);
    command.setClockIncrement(GenericColor.BLACK, 0L);
    command.setPonder();
    commands.add(command);
    new Timer(true).schedule(new TimerTask() {
      @Override
      public void run() {
        commands.add(new EnginePonderHitCommand());
      }
    }, 1000);

    new Pulse(new ProtocolHandler()).run();
  }

  private class ProtocolHandler implements IProtocolHandler {

    @Override
    public IEngineCommand receive() throws IOException {
      IEngineCommand command = null;
      try {
        command = commands.take();
      } catch (InterruptedException e) {
        fail();
      }

      return command;
    }

    @Override
    public void send(ProtocolInitializeAnswerCommand command) {
    }

    @Override
    public void send(ProtocolReadyAnswerCommand command) {
      assertEquals("test", command.token);
    }

    @Override
    public void send(ProtocolBestMoveCommand command) {
      commands.add(new EngineQuitCommand());
    }

    @Override
    public void send(ProtocolInformationCommand command) {
    }

  }

}
