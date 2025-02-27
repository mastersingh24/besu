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
package org.hyperledger.besu.cli;

import org.hyperledger.besu.ethereum.core.Wei;
import org.hyperledger.besu.ethereum.eth.sync.SyncMode;
import org.hyperledger.besu.ethereum.p2p.config.RlpxConfiguration;
import org.hyperledger.besu.nat.NatMethod;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import picocli.CommandLine;

public interface DefaultCommandValues {
  String CONFIG_FILE_OPTION_NAME = "--config-file";

  String MANDATORY_PATH_FORMAT_HELP = "<PATH>";
  String MANDATORY_FILE_FORMAT_HELP = "<FILE>";
  String MANDATORY_DIRECTORY_FORMAT_HELP = "<DIRECTORY>";
  String BESU_HOME_PROPERTY_NAME = "besu.home";
  String DEFAULT_DATA_DIR_PATH = "./build/data";
  String MANDATORY_INTEGER_FORMAT_HELP = "<INTEGER>";
  String MANDATORY_DOUBLE_FORMAT_HELP = "<DOUBLE>";
  String MANDATORY_LONG_FORMAT_HELP = "<LONG>";
  String MANDATORY_MODE_FORMAT_HELP = "<MODE>";
  String MANDATORY_NETWORK_FORMAT_HELP = "<NETWORK>";
  String MANDATORY_NODE_ID_FORMAT_HELP = "<NODEID>";
  Wei DEFAULT_MIN_TRANSACTION_GAS_PRICE = Wei.of(1000);
  long DEFAULT_PRUNING_BLOCKS_RETAINED = 1024;
  long DEFAULT_PRUNING_BLOCK_CONFIRMATIONS = 10;
  BytesValue DEFAULT_EXTRA_DATA = BytesValue.EMPTY;
  long DEFAULT_MAX_REFRESH_DELAY = 3600000;
  long DEFAULT_MIN_REFRESH_DELAY = 1;
  String DOCKER_GENESIS_LOCATION = "/etc/besu/genesis.json";
  String DOCKER_DATADIR_LOCATION = "/var/lib/besu";
  String DOCKER_PLUGINSDIR_LOCATION = "/etc/besu/plugins";
  String DOCKER_RPC_HTTP_AUTHENTICATION_CREDENTIALS_FILE_LOCATION =
      "/etc/besu/rpc_http_auth_config.toml";
  String DOCKER_RPC_WS_AUTHENTICATION_CREDENTIALS_FILE_LOCATION =
      "/etc/besu/rpc_ws_auth_config.toml";
  String DOCKER_PRIVACY_PUBLIC_KEY_FILE = "/etc/besu/privacy_public_key";
  String DOCKER_PERMISSIONS_CONFIG_FILE_LOCATION = "/etc/besu/permissions_config.toml";
  String PERMISSIONING_CONFIG_LOCATION = "permissions_config.toml";
  String MANDATORY_HOST_FORMAT_HELP = "<HOST>";
  String MANDATORY_PORT_FORMAT_HELP = "<PORT>";
  // Default should be FAST for the next release
  // but we use FULL for the moment as Fast is still in progress
  SyncMode DEFAULT_SYNC_MODE = SyncMode.FULL;
  NatMethod DEFAULT_NAT_METHOD = NatMethod.NONE;
  int FAST_SYNC_MIN_PEER_COUNT = 5;
  int DEFAULT_MAX_PEERS = 25;
  float DEFAULT_FRACTION_REMOTE_WIRE_CONNECTIONS_ALLOWED =
      RlpxConfiguration.DEFAULT_FRACTION_REMOTE_CONNECTIONS_ALLOWED;
  String DEFAULT_KEY_VALUE_STORAGE_NAME = "rocksdb";

  static Path getDefaultBesuDataPath(final Object command) {
    // this property is retrieved from Gradle tasks or Besu running shell script.
    final String besuHomeProperty = System.getProperty(BESU_HOME_PROPERTY_NAME);
    final Path besuHome;

    // If prop is found, then use it
    if (besuHomeProperty != null) {
      try {
        besuHome = Paths.get(besuHomeProperty);
      } catch (final InvalidPathException e) {
        throw new CommandLine.ParameterException(
            new CommandLine(command),
            String.format(
                "Unable to define default data directory from %s property.",
                BESU_HOME_PROPERTY_NAME),
            e);
      }
    } else {
      // otherwise use a default path.
      // That may only be used when NOT run from distribution script and Gradle as they all define
      // the property.
      try {
        final String path = new File(DEFAULT_DATA_DIR_PATH).getCanonicalPath();
        besuHome = Paths.get(path);
      } catch (final IOException e) {
        throw new CommandLine.ParameterException(
            new CommandLine(command), "Unable to create default data directory.");
      }
    }

    // Try to create it, then verify if the provided path is not already existing and is not a
    // directory. Otherwise, if it doesn't exist or exists but is already a directory,
    // Runner will use it to store data.
    try {
      Files.createDirectories(besuHome);
    } catch (final FileAlreadyExistsException e) {
      // Only thrown if it exists but is not a directory
      throw new CommandLine.ParameterException(
          new CommandLine(command),
          String.format("%s: already exists and is not a directory.", besuHome.toAbsolutePath()),
          e);
    } catch (final Exception e) {
      throw new CommandLine.ParameterException(
          new CommandLine(command),
          String.format("Error creating directory %s.", besuHome.toAbsolutePath()),
          e);
    }
    return besuHome;
  }
}
