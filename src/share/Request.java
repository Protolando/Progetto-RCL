package share;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

public class Request {
  private RequestType requestType;
  private HashMap<String, String> payload;

  @JsonCreator
  public Request(@JsonProperty("RequestType") RequestType requestType, @JsonProperty("Payload") HashMap<String, String> payload) {
    this.requestType = requestType;
    this.payload = payload;
  }

  public Request(RequestType requestType) {
    this.requestType = requestType;
    this.payload = new HashMap<>();
  }

  public HashMap<String, String> getPayload() {
    return payload;
  }

  public void setPayload(HashMap<String, String> payload) {
    this.payload = payload;
  }

  public RequestType getRequestType() {
    return requestType;
  }

  public void setRequestType(RequestType requestType) {
    this.requestType = requestType;
  }

  public void putInPayload(String key, String data) {
    payload.put(key, data);
  }

  @Override
  public String toString() {
    return "Request{" +
        "requestType=" + requestType +
        ", payload=" + payload +
        '}';
  }
}

