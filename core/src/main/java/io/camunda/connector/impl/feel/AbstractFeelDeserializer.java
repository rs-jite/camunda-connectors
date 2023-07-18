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
package io.camunda.connector.impl.feel;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public abstract class AbstractFeelDeserializer<T> extends StdDeserializer<T>
    implements ContextualDeserializer {
  protected FeelEngineWrapper feelEngineWrapper;
  protected boolean relaxed;

  protected AbstractFeelDeserializer(FeelEngineWrapper feelEngineWrapper, boolean relaxed) {
    super(String.class);
    this.feelEngineWrapper = feelEngineWrapper;
    this.relaxed = relaxed;
  }

  @Override
  public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    JsonNode node = parser.getCodec().readTree(parser);

    if (node != null && node.isTextual()) {
      String value = node.textValue();
      // if not relaxed, we expect only a FEEL expression
      // otherwise we accept any string
      if (relaxed || isFeelExpression(value)) {
        return doDeserialize(value);
      }
    }
    throw new IOException(
        "Invalid input: expected a FEEL expression, but got '" + node + "' instead.");
  }

  protected boolean isFeelExpression(String value) {
    return value.startsWith("=");
  }

  protected abstract T doDeserialize(String expression);
}