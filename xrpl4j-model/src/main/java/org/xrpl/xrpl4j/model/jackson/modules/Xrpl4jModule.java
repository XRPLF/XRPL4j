package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Jackson module for the xrpl4j-model project.
 */
public class Xrpl4jModule extends SimpleModule {

  private static final String NAME = "Xrpl4jModule";

  /**
   * No-arg constructor.
   */
  public Xrpl4jModule() {
    super(
        NAME,
        new Version(
            1,
            0,
            0,
            null,
            "org.xrpl.xrpl4j",
            "xrpl4j"
        )
    );

    addSerializer(Address.class, new AddressSerializer());
    addDeserializer(Address.class, new AddressDeserializer());

    addSerializer(Hash256.class, new Hash256Serializer());
    addDeserializer(Hash256.class, new Hash256Deserializer());

    addSerializer(XrpCurrencyAmount.class, new XrpCurrencyAmountSerializer());
    addDeserializer(XrpCurrencyAmount.class, new XrpCurrencyAmountDeserializer());

    addDeserializer(CurrencyAmount.class, new CurrencyAmountDeserializer());

    addSerializer(LedgerIndex.class, new OldLedgerIndexSerializer());
    addDeserializer(LedgerIndex.class, new OldLedgerIndexDeserializer());

    addSerializer(org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex.class, new LedgerIndexSerializer());
    addDeserializer(org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex.class, new LedgerIndexDeserializer());

    addSerializer(LedgerSpecifier.class, new LedgerSpecifierSerializer());
    addDeserializer(LedgerSpecifier.class, new LedgerSpecifierDeserializer());

    addDeserializer(Transaction.class, new TransactionDeserializer());

    addSerializer(Marker.class, new MarkerSerializer());
    addDeserializer(Marker.class, new MarkerDeserializer());
  }
}
