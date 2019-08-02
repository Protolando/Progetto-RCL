package share;

import java.io.Serializable;

public class Request implements Serializable {
  private static final long serialVersionUID = 1;
  private RequestType requestType;
  private String payload;

  public Request(RequestType requestType, String payload) {
    this.requestType = requestType;
    this.payload = payload;
  }

  public Request(RequestType requestType) {
    this.requestType = requestType;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public RequestType getRequestType() {
    return requestType;
  }

  @Override
  public String toString() {
    return "Request{" +
        "requestType=" + requestType +
        ", payload='" + payload + '\'' +
        '}';
  }
}

