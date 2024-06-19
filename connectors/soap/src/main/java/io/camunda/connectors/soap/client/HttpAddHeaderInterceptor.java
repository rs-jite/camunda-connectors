/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.connectors.soap.client;

import java.io.IOException;
import java.util.Map;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.ClientHttpRequestConnection;

public class HttpAddHeaderInterceptor implements ClientInterceptor {

  private final Map<String, String> headers;

  public HttpAddHeaderInterceptor(Map<String, String> headers) {
    this.headers = headers;
  }

  @Override
  public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
    var ctx = TransportContextHolder.getTransportContext();
    var con = (ClientHttpRequestConnection) ctx.getConnection();
    headers.forEach(
        (s, s2) -> {
          try {
            con.addRequestHeader(s, s2);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    return true;
  }

  @Override
  public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
    return true;
  }

  @Override
  public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
    return true;
  }

  @Override
  public void afterCompletion(MessageContext messageContext, Exception e)
      throws WebServiceClientException {}
}
