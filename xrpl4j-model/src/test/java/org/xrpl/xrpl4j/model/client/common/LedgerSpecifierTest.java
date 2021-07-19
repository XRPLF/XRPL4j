package org.xrpl.xrpl4j.model.client.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LedgerIndex;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class LedgerSpecifierTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();
  public static final Hash256 LEDGER_HASH = Hash256.of("0000000000000000000000000000000000000000000000000000000000000000");

  @Test
  void specifyOneSpecifier() {
    assertDoesNotThrow(
      () -> LedgerSpecifier.builder()
        .ledgerHash(LEDGER_HASH)
        .build()
    );

    assertDoesNotThrow(
      () -> LedgerSpecifier.builder()
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .build()
    );

    assertDoesNotThrow(
      () -> LedgerSpecifier.builder()
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );
  }

  @Test
  void specifyMoreThanOneThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerHash(LEDGER_HASH)
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerHash(LEDGER_HASH)
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerHash(LEDGER_HASH)
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );
  }

  @Test
  void specifyNoneThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder().build()
    );
  }

  @Test
  void specifyUsingUtilityConstructors() {
    assertDoesNotThrow(() -> LedgerSpecifier.ledgerHash(LEDGER_HASH));
    assertDoesNotThrow(() -> LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE)));
    assertDoesNotThrow(() -> LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.CURRENT));
  }

  @Test
  void handlesAllCorrectly() {
    List<LedgerSpecifier> ledgerSpecifiers = Lists.newArrayList(
      LedgerSpecifier.ledgerHash(LEDGER_HASH),
      LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE)),
      LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
    );

    ledgerSpecifiers.forEach(this::assertHandlesCorrectly);
  }

  @Test
  void handleThrowsWithNullHandlers() {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerHash(LEDGER_HASH);
    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        null,
        $ -> {
        },
        $ -> {
        }
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        $ -> {
        },
        null,
        $ -> {
        }
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        $ -> {
        },
        $ -> {
        },
        null
      )
    );
  }

  @Test
  void mapsAllCorrectly() {
    List<LedgerSpecifier> ledgerSpecifiers = Lists.newArrayList(
      LedgerSpecifier.ledgerHash(LEDGER_HASH),
      LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE)),
      LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
    );

    ledgerSpecifiers.forEach(
      specifier -> {
        final String mapped = specifier.map(
          ledgerHash -> "ledgerHash",
          ledgerIndex -> "ledgerIndex",
          ledgerIndexShortcut -> "ledgerIndexShortcut"
        );

        assertThat(mapped).isNotNull();

        if (specifier.ledgerHash().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerHash");
        } else if (specifier.ledgerIndex().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerIndex");
        } else if (specifier.ledgerIndexShortcut().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerIndexShortcut");
        }
      }
    );
  }

  @Test
  void mapThrowsWithNullMappers() {
    final LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerHash(LEDGER_HASH);
    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        null,
        $ -> "",
        $ -> ""
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        $ -> "",
        null,
        $ -> ""
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        $ -> "",
        $ -> "",
        null
      )
    );
  }

  @Test
  void testLedgerHashJson() throws JsonProcessingException, JSONException {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerHash(LEDGER_HASH);
    LedgerSpecifierWrapper wrapper = LedgerSpecifierWrapper.of(ledgerSpecifier);
    final String serialized = objectMapper.writeValueAsString(wrapper);
    String json = "{\"ledger_hash\": \"" + LEDGER_HASH + "\"}";
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
  }

  @Test
  void testLedgerIndexJson() throws JsonProcessingException, JSONException {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE));
    LedgerSpecifierWrapper wrapper = LedgerSpecifierWrapper.of(ledgerSpecifier);
    final String serialized = objectMapper.writeValueAsString(wrapper);
    String json = "{\"ledger_index\": 1}";
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
  }

  @Test
  void testLedgerIndexShortcutJson() throws JsonProcessingException, JSONException {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED);
    LedgerSpecifierWrapper wrapper = LedgerSpecifierWrapper.of(ledgerSpecifier);
    final String serialized = objectMapper.writeValueAsString(wrapper);
    String json = "{\"ledger_index\": \"validated\"}";
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
  }

  private void assertHandlesCorrectly(LedgerSpecifier ledgerSpecifier) {
    AtomicBoolean ledgerHashHandled = new AtomicBoolean(false);
    AtomicBoolean ledgerIndexHandled = new AtomicBoolean(false);
    AtomicBoolean ledgerIndexShortcutHandled = new AtomicBoolean(false);

    ledgerSpecifier.handle(
      ledgerHash -> ledgerHashHandled.set(true),
      ledgerIndex -> ledgerIndexHandled.set(true),
      ledgerIndexShortcut -> ledgerIndexShortcutHandled.set(true)
    );

    if (ledgerSpecifier.ledgerHash().isPresent()) {
      assertThat(ledgerHashHandled).isTrue();
    } else {
      assertThat(ledgerHashHandled).isFalse();
    }

    if (ledgerSpecifier.ledgerIndex().isPresent()) {
      assertThat(ledgerIndexHandled).isTrue();
    } else {
      assertThat(ledgerIndexHandled).isFalse();
    }

    if (ledgerSpecifier.ledgerIndexShortcut().isPresent()) {
      assertThat(ledgerIndexShortcutHandled).isTrue();
    } else {
      assertThat(ledgerIndexShortcutHandled).isFalse();
    }
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerSpecifierWrapper.class)
  @JsonDeserialize(as = ImmutableLedgerSpecifierWrapper.class)
  interface LedgerSpecifierWrapper {

    static LedgerSpecifierWrapper of(LedgerSpecifier ledgerSpecifier) {
      return ImmutableLedgerSpecifierWrapper.builder().ledgerSpecifier(ledgerSpecifier).build();
    }

    @JsonUnwrapped
    LedgerSpecifier ledgerSpecifier();
  }
}