package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link LedgerIndex}s.
 */
public class LedgerIndexDeserializer extends StdDeserializer<LedgerIndex> {

  protected LedgerIndexDeserializer() {
    super(LedgerIndex.class);
  }

  @Override
  public LedgerIndex deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    return LedgerIndex.of(jsonParser.getValueAsString());
  }
}
