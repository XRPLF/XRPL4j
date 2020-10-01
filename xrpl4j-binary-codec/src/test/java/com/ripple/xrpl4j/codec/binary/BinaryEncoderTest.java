package com.ripple.xrpl4j.codec.binary;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class BinaryEncoderTest {

  public static final String SIMPLE_JSON = "{\"CloseResolution\":\"01\",\"Method\":\"02\"}";
  public static final String SINGLE_LEVEL_OBJECT_JSON = "{\"Memo\":{\"Memo\":{\"Method\":\"02\"}}}";
  public static final String MULTI_LEVEL_OBJECT_JSON =
      "{\"Memo\":{\"Memo\":{\"CloseResolution\":\"01\",\"Method\":\"02\"}}}";

  public static final String SIMPLE_HEX = "011001021002";
  public static final String SINGLE_OBJECT_HEX = "EAEA021002E1E1";
  public static final String MULTI_LEVEL_OBJECT_HEX = "EAEA011001021002E1E1";

  private XrplBinaryCodec encoder = new XrplBinaryCodec();

  @Test
  void encodeDecodeSimple() throws JsonProcessingException {
    assertThat(encoder.encode(SIMPLE_JSON)).isEqualTo(SIMPLE_HEX);
    assertThat(encoder.decode(SIMPLE_HEX)).isEqualTo(SIMPLE_JSON);
  }

  @Test
  void encodeDecodeSingleChildObject() throws JsonProcessingException {
    assertThat(encoder.encode(SINGLE_LEVEL_OBJECT_JSON)).isEqualTo(SINGLE_OBJECT_HEX);
    assertThat(encoder.decode(SINGLE_OBJECT_HEX)).isEqualTo(SINGLE_LEVEL_OBJECT_JSON);
  }

  @Test
  void encodeJsonWithMultipleEmbeddedObjects() throws JsonProcessingException {
    assertThat(encoder.encode(MULTI_LEVEL_OBJECT_JSON)).isEqualTo(MULTI_LEVEL_OBJECT_HEX);
    assertThat(encoder.decode(MULTI_LEVEL_OBJECT_HEX)).isEqualTo(MULTI_LEVEL_OBJECT_JSON);
  }

}