/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.api.jsonrpc.websocket.subscription.syncing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.ethereum.api.jsonrpc.internal.results.JsonRpcResult;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.results.SyncingResult;
import org.hyperledger.besu.ethereum.api.jsonrpc.websocket.subscription.SubscriptionManager;
import org.hyperledger.besu.ethereum.api.jsonrpc.websocket.subscription.request.SubscriptionType;
import org.hyperledger.besu.ethereum.core.SyncStatus;
import org.hyperledger.besu.ethereum.core.Synchronizer;
import org.hyperledger.besu.plugin.services.BesuEvents.SyncStatusListener;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyncingSubscriptionServiceTest {

  @Mock private SubscriptionManager subscriptionManager;
  @Mock private Synchronizer synchronizer;
  private SyncStatusListener syncStatusListener;

  @Before
  public void before() {
    final ArgumentCaptor<SyncStatusListener> captor =
        ArgumentCaptor.forClass(SyncStatusListener.class);
    when(synchronizer.observeSyncStatus(captor.capture())).thenReturn(1L);
    new SyncingSubscriptionService(subscriptionManager, synchronizer);
    syncStatusListener = captor.getValue();
  }

  @Test
  public void shouldSendSyncStatusWhenReceiveSyncStatus() {
    final SyncingSubscription subscription =
        new SyncingSubscription(9L, "conn", SubscriptionType.SYNCING);
    final List<SyncingSubscription> subscriptions = Collections.singletonList(subscription);
    final SyncStatus syncStatus = new SyncStatus(0L, 1L, 3L);
    final SyncingResult expectedSyncingResult = new SyncingResult(syncStatus);

    doAnswer(
            invocation -> {
              Consumer<List<SyncingSubscription>> consumer = invocation.getArgument(2);
              consumer.accept(subscriptions);
              return null;
            })
        .when(subscriptionManager)
        .notifySubscribersOnWorkerThread(any(), any(), any());

    syncStatusListener.onSyncStatusChanged(syncStatus);

    verify(subscriptionManager)
        .sendMessage(
            ArgumentMatchers.eq(subscription.getSubscriptionId()), eq(expectedSyncingResult));
  }

  @Test
  public void shouldSendNotSyncingStatusWhenReceiveSyncStatusAtHead() {
    final SyncingSubscription subscription =
        new SyncingSubscription(9L, "conn", SubscriptionType.SYNCING);
    final List<SyncingSubscription> subscriptions = Collections.singletonList(subscription);
    final SyncStatus syncStatus = new SyncStatus(0L, 1L, 1L);

    doAnswer(
            invocation -> {
              Consumer<List<SyncingSubscription>> consumer = invocation.getArgument(2);
              consumer.accept(subscriptions);
              return null;
            })
        .when(subscriptionManager)
        .notifySubscribersOnWorkerThread(any(), any(), any());

    syncStatusListener.onSyncStatusChanged(syncStatus);

    verify(subscriptionManager)
        .sendMessage(
            ArgumentMatchers.eq(subscription.getSubscriptionId()),
            any(NotSynchronisingResult.class));
  }

  @Test
  public void shouldNotRepeatOutOfSyncMessages() {
    final SyncingSubscription subscription =
        new SyncingSubscription(9L, "conn", SubscriptionType.SYNCING);
    final List<SyncingSubscription> subscriptions = Collections.singletonList(subscription);
    final SyncStatus syncStatus = new SyncStatus(0L, 1L, 3L);
    final SyncingResult expectedSyncingResult = new SyncingResult(syncStatus);

    doAnswer(
            invocation -> {
              Consumer<List<SyncingSubscription>> consumer = invocation.getArgument(2);
              consumer.accept(subscriptions);
              return null;
            })
        .when(subscriptionManager)
        .notifySubscribersOnWorkerThread(any(), any(), any());

    syncStatusListener.onSyncStatusChanged(syncStatus);
    syncStatusListener.onSyncStatusChanged(syncStatus);

    verify(subscriptionManager, atMostOnce())
        .sendMessage(
            ArgumentMatchers.eq(subscription.getSubscriptionId()), eq(expectedSyncingResult));
  }

  @Test
  public void shouldNotRepeatInSyncMessages() {
    final SyncingSubscription subscription =
        new SyncingSubscription(9L, "conn", SubscriptionType.SYNCING);
    final List<SyncingSubscription> subscriptions = Collections.singletonList(subscription);
    final SyncStatus syncStatus = new SyncStatus(0L, 3L, 3L);
    final SyncingResult expectedSyncingResult = new SyncingResult(syncStatus);

    doAnswer(
            invocation -> {
              Consumer<List<SyncingSubscription>> consumer = invocation.getArgument(2);
              consumer.accept(subscriptions);
              return null;
            })
        .when(subscriptionManager)
        .notifySubscribersOnWorkerThread(any(), any(), any());

    syncStatusListener.onSyncStatusChanged(syncStatus);
    syncStatusListener.onSyncStatusChanged(syncStatus);

    verify(subscriptionManager, atMostOnce())
        .sendMessage(
            ArgumentMatchers.eq(subscription.getSubscriptionId()), eq(expectedSyncingResult));
  }

  @Test
  public void shouldOnlyReportSyncChange() {
    final SyncingSubscription subscription =
        new SyncingSubscription(9L, "conn", SubscriptionType.SYNCING);
    final List<SyncingSubscription> subscriptions = Collections.singletonList(subscription);

    final SyncStatus inSyncStatus = new SyncStatus(0L, 3L, 3L);
    final SyncStatus outOfSyncStatus = new SyncStatus(0L, 1L, 3L);

    doAnswer(
            invocation -> {
              Consumer<List<SyncingSubscription>> consumer = invocation.getArgument(2);
              consumer.accept(subscriptions);
              return null;
            })
        .when(subscriptionManager)
        .notifySubscribersOnWorkerThread(any(), any(), any());

    syncStatusListener.onSyncStatusChanged(outOfSyncStatus);
    syncStatusListener.onSyncStatusChanged(inSyncStatus);
    syncStatusListener.onSyncStatusChanged(inSyncStatus);
    syncStatusListener.onSyncStatusChanged(outOfSyncStatus);
    syncStatusListener.onSyncStatusChanged(outOfSyncStatus);
    syncStatusListener.onSyncStatusChanged(inSyncStatus);
    syncStatusListener.onSyncStatusChanged(inSyncStatus);

    final var resultCaptor = ArgumentCaptor.forClass(JsonRpcResult.class);
    final NotSynchronisingResult inSyncResult = new NotSynchronisingResult();
    final SyncingResult outOfSyncingResult = new SyncingResult(outOfSyncStatus);

    verify(subscriptionManager, times(4)).sendMessage(any(), resultCaptor.capture());
    assertThat(resultCaptor.getAllValues())
        .containsOnly(outOfSyncingResult, inSyncResult, outOfSyncingResult, inSyncResult);
  }
}
