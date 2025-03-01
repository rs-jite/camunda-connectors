/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.connector.api.inbound;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.connector.api.inbound.CorrelationFailureHandlingStrategy.ForwardErrorToUpstream;
import io.camunda.connector.api.inbound.CorrelationFailureHandlingStrategy.Ignore;

public sealed interface CorrelationResult {

  sealed interface Success extends CorrelationResult {

    ProcessElementContext activatedElement();

    record ProcessInstanceCreated(
        @JsonIgnore ProcessElementContext activatedElement,
        Long processInstanceKey,
        String tenantId)
        implements Success {}

    record MessagePublished(
        @JsonIgnore ProcessElementContext activatedElement, Long messageKey, String tenantId)
        implements Success {}

    record MessageAlreadyCorrelated(@JsonIgnore ProcessElementContext activatedElement)
        implements Success {}
  }

  sealed interface Failure extends CorrelationResult {

    String message();

    default CorrelationFailureHandlingStrategy handlingStrategy() {
      return ForwardErrorToUpstream.RETRYABLE;
    }

    record InvalidInput(String message, Throwable error) implements Failure {

      @Override
      public CorrelationFailureHandlingStrategy handlingStrategy() {
        return ForwardErrorToUpstream.NON_RETRYABLE;
      }
    }

    record ActivationConditionNotMet(boolean consumeUnmatched) implements Failure {

      @Override
      public String message() {
        return "Activation condition not met";
      }

      @Override
      public CorrelationFailureHandlingStrategy handlingStrategy() {
        if (consumeUnmatched) {
          return Ignore.INSTANCE;
        } else {
          return ForwardErrorToUpstream.NON_RETRYABLE;
        }
      }
    }

    record ZeebeClientStatus(String status, String message) implements Failure {}

    record Other(Throwable error) implements Failure {

      @Override
      public String message() {
        return error.getMessage();
      }
    }
  }
}
