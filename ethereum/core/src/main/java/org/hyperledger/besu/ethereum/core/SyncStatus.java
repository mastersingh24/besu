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
package org.hyperledger.besu.ethereum.core;

import java.util.Objects;

public final class SyncStatus implements org.hyperledger.besu.plugin.data.SyncStatus {

  private final long startingBlock;
  private final long currentBlock;
  private final long highestBlock;
  private final boolean inSync;

  public SyncStatus(final long startingBlock, final long currentBlock, final long highestBlock) {
    this(startingBlock, currentBlock, highestBlock, currentBlock == highestBlock);
  }

  public SyncStatus(
      final long startingBlock,
      final long currentBlock,
      final long highestBlock,
      final boolean inSync) {
    this.startingBlock = startingBlock;
    this.currentBlock = currentBlock;
    this.highestBlock = highestBlock;
    this.inSync = inSync;
  }

  @Override
  public long getStartingBlock() {
    return startingBlock;
  }

  @Override
  public long getCurrentBlock() {
    return currentBlock;
  }

  @Override
  public long getHighestBlock() {
    return highestBlock;
  }

  @Override
  public boolean inSync() {
    return inSync;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SyncStatus that = (SyncStatus) o;
    return startingBlock == that.startingBlock
        && currentBlock == that.currentBlock
        && highestBlock == that.highestBlock
        && inSync == that.inSync;
  }

  @Override
  public int hashCode() {
    return Objects.hash(startingBlock, currentBlock, highestBlock, inSync);
  }
}
