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

package com.contentful.vault;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

import java.util.List;

public final class ObserveQuery<T> extends AbsQuery<T, ObserveQuery<T>> {
  ObserveQuery(Class<T> type, Vault vault) {
    super(type, vault);
  }

  public Flowable<T> all() {
    return all(null);
  }

  public Flowable<T> all(String locale) {
    return Flowable.create(new AllOnSubscribe<>(this, locale), BackpressureStrategy.BUFFER);
  }

  static class AllOnSubscribe<T> implements FlowableOnSubscribe<T> {
    private final ObserveQuery<T> query;
    private final String locale;

    public AllOnSubscribe(ObserveQuery<T> query, String locale) {
      this.query = query;
      this.locale = locale;
    }

    @Override public void subscribe(FlowableEmitter<T> flowableEmitter) {
      try {
        FetchQuery<T> fetchQuery = query.vault().fetch(query.type());
        fetchQuery.setParams(query.params());
        List<T> items = fetchQuery.all(locale);
        for (T item : items) {
          if (flowableEmitter.isCancelled()) {
            return;
          }
          flowableEmitter.onNext(item);
        }
      } catch (Throwable t) {
        if (!flowableEmitter.isCancelled()) {
          flowableEmitter.onError(t);
        }
        return;
      }
      if (!flowableEmitter.isCancelled()) {
        flowableEmitter.onComplete();
      }
    }
  }
}
