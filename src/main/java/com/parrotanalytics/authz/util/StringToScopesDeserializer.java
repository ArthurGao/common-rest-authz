package com.arthur.authz.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.arthur.authz.models.ExternalAuthClaim.Scope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StringToScopesDeserializer extends StdDeserializer<List<Scope>> {

  public StringToScopesDeserializer() {
    this(null);
  }

  public StringToScopesDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public List<Scope> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    String scope = node.get(0).get("scope").toString();
    String[] scopes = scope.split("\\s+");
    List<Scope> scopeList = new ArrayList<>();
    for (String scopeStr : scopes) {
      scopeList.add(new Scope(scopeStr));
    }
    return scopeList;
  }
}
