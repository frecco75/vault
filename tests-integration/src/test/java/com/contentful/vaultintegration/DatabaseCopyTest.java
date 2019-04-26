/*
 * Copyright (C) 2018 Contentful GmbH
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

package com.contentful.vaultintegration;

import com.contentful.vault.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class DatabaseCopyTest extends BaseTest {
  @Space(
      value = "cfexampleapi",
      models = Cat.class,
      locales = {"en-US", "tlh"},
      copyPath = "cfexampleapi.db"
  ) interface Sp {
    String TOKEN = "b4c0n73n7fu1";
  }

  @ContentType("cat")
  static class Cat {
    @Field
    private String name;
    @Field
    private Cat bestFriend;
    @Field
    private Asset image;

    public String name() {
      return name;
    }

    public Cat bestFriend() {
      return bestFriend;
    }

    public Asset image() {
      return image;
    }

    public void name(String name) {
      this.name = name;
    }

    public void bestFriend(Cat bestFriend) {
      this.bestFriend = bestFriend;
    }

    public void image(Asset image) {
      this.image = image;
    }
  }

  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, Sp.class);
  }

  @Test public void testDatabaseCopy() throws Exception {
    List<Cat> cats = vault.fetch(Cat.class).all();
    assertThat(cats).hasSize(3);
  }

  @Ignore("Do not update db on ci.")
  @Test
  public void testSyncDBtoSqlite() throws Exception {
    assertTrue(
        new VaultDatabaseExporter()
            .export(
                RuntimeEnvironment.application,
                Sp.class,
                Sp.TOKEN)
    );
  }
}
