/*
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */
package com.fluxchess.pulse;

import com.fluxchess.jcpi.models.GenericFile;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static com.fluxchess.test.AssertUtil.assertUtilityClassWellDefined;
import static org.junit.Assert.*;

public class FileTest {

  @Test
  public void testUtilityClass() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    assertUtilityClassWellDefined(File.class);
  }

  @Test
  public void testValues() {
    for (int file : File.values) {
      assertEquals(file, File.values[file]);
    }
  }

  @Test
  public void testIsValid() {
    for (int file : File.values) {
      assertTrue(File.isValid(file));
    }

    assertFalse(File.isValid(File.NOFILE));
  }

}
