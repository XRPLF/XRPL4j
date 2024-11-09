package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenObjectAmount;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.util.Optional;

@Immutable
@JsonSerialize(as = ImmutableMpTokenIssuanceObject.class)
@JsonDeserialize(as = ImmutableMpTokenIssuanceObject.class)
public interface MpTokenIssuanceObject extends LedgerObject {

  /**
   * Construct a {@code MetaMpTokenIssuanceObject} builder.
   *
   * @return An {@link ImmutableMpTokenIssuanceObject.Builder}.
   */
  static ImmutableMpTokenIssuanceObject.Builder builder() {
    return ImmutableMpTokenIssuanceObject.builder();
  }

  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.MP_TOKEN_ISSUANCE;
  }

  @JsonProperty("Flags")
  MpTokenIssuanceFlags flags();

  @JsonProperty("Issuer")
  Address issuer();

  /**
   * A 32-bit unsigned integer that is used to ensure issuances from a given sender may only ever exist once, even if an
   * issuance is later deleted. Whenever a new issuance is created, this value must match the account's current Sequence
   * number.
   *
   * @return An {@link UnsignedInteger} representing the account sequence number.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  @JsonProperty("TransferFee")
  TransferFee transferFee();

  @JsonProperty("AssetScale")
  AssetScale assetScale();

  @JsonProperty("MaximumAmount")
  Optional<MpTokenObjectAmount> maximumAmount();

  @JsonProperty("OutstandingAmount")
  MpTokenObjectAmount outstandingAmount();

  @JsonProperty("MPTokenMetadata")
  Optional<String> mpTokenMetadata();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * A hint indicating which page of the owner directory links to this object, in case the directory consists of
   * multiple pages.
   *
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.</p>
   *
   * @return An {@link Optional} of type {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * The unique ID of this {@link MpTokenIssuanceObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();

  /**
   * The {@link MpTokenIssuanceId} of the issuance. Only present in responses to {@code ledger_data} and
   * {@code account_objects} RPC calls.
   *
   * @return An {@link Optional} {@link MpTokenIssuanceId}.
   */
  Optional<MpTokenIssuanceId> mpTokenIssuanceId();
}
