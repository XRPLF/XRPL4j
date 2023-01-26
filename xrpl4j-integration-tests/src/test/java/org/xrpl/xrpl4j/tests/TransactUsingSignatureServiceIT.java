package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.MultiSignedTransaction;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureWithPublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Integration tests for submitting payment transactions to the XRPL using a {@link SignatureService} that uses *
 * instances of {@link PrivateKey}  for all signing operations.
 */
public class TransactUsingSignatureServiceIT extends AbstractIT {

  @Test
  public void sendPaymentFromEd25519Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    final PrivateKey sourcePrivateKey = constructPrivateKey("sourceWallet", VersionType.ED25519);
    final PublicKey sourcePublicKey = signatureService.derivePublicKey(sourcePrivateKey);
    final Address sourceWalletAddress = sourcePublicKey.deriveAddress();
    this.fundAccount(sourceWalletAddress);

    final PrivateKey destinationPrivateKey = constructPrivateKey("destinationWallet", VersionType.ED25519);
    final PublicKey destinationWalletPublicKey = signatureService.derivePublicKey(destinationPrivateKey);
    final Address destinationWalletAddress = destinationWalletPublicKey.deriveAddress();
    this.fundAccount(destinationWalletAddress);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(sourceWalletAddress));
    Payment payment = Payment.builder()
      .account(sourceWalletAddress)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWalletAddress)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourcePublicKey.base16Value())
      .build();

    SingleSignedTransaction<Payment> signedTransaction = signatureService.sign(sourcePrivateKey, payment);
    SubmitResult<Payment> result = xrplClient.submit(signedTransaction);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  @Test
  public void sendPaymentFromSecp256k1Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    final PrivateKey sourcePrivateKey = constructPrivateKey("sourceWallet", VersionType.SECP256K1);
    final PublicKey sourceWalletPublicKey = signatureService.derivePublicKey(sourcePrivateKey);
    final Address sourceWalletAddress = sourceWalletPublicKey.deriveAddress();
    this.fundAccount(sourceWalletAddress);

    final PrivateKey destinationPrivateKey = constructPrivateKey("destinationWallet", VersionType.SECP256K1);
    final PublicKey destinationWalletPublicKey = signatureService.derivePublicKey(destinationPrivateKey);
    final Address destinationWalletAddress = destinationWalletPublicKey.deriveAddress();
    this.fundAccount(destinationWalletAddress);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this
      .scanForResult(() -> this.getValidatedAccountInfo(sourceWalletAddress));
    Payment payment = Payment.builder()
      .account(sourceWalletAddress)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWalletAddress)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWalletPublicKey.base16Value())
      .build();

    SingleSignedTransaction<Payment> transactionWithSignature = signatureService.sign(sourcePrivateKey, payment);
    SubmitResult<Payment> result = xrplClient.submit(transactionWithSignature);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/" + result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  @Test
  void multiSigPaymentFromEd25519Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create four accounts: one for the source account; two for the signers; one for the destination
    PrivateKey sourcePrivateKey = constructPrivateKey("source", VersionType.ED25519);
    fundAccount(toAddress(sourcePrivateKey));

    PrivateKey alicePrivateKey = constructPrivateKey("alice", VersionType.ED25519);
    fundAccount(toAddress(alicePrivateKey));

    PrivateKey bobPrivateKey = constructPrivateKey("bob", VersionType.ED25519);
    fundAccount(toAddress(bobPrivateKey));

    PrivateKey destinationPrivateKey = constructPrivateKey("destination", VersionType.ED25519);
    fundAccount(toAddress(destinationPrivateKey));

    this.multiSigSendPaymentHelper(sourcePrivateKey, alicePrivateKey, bobPrivateKey, destinationPrivateKey);
  }

  @Test
  void multiSigPaymentFromSecp256k1Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create four accounts: one for the source account; two for the signers; one for the destination
    PrivateKey sourcePrivateKey = constructPrivateKey("source", VersionType.SECP256K1);
    fundAccount(toAddress(sourcePrivateKey));

    PrivateKey alicePrivateKey = constructPrivateKey("alice", VersionType.SECP256K1);
    fundAccount(toAddress(alicePrivateKey));

    PrivateKey bobPrivateKey = constructPrivateKey("bob", VersionType.SECP256K1);
    fundAccount(toAddress(bobPrivateKey));

    PrivateKey destinationPrivateKey = constructPrivateKey("destination", VersionType.SECP256K1);
    fundAccount(toAddress(destinationPrivateKey));

    this.multiSigSendPaymentHelper(sourcePrivateKey, alicePrivateKey, bobPrivateKey, destinationPrivateKey);
  }

  /**
   * Helper to send a multisign payment using a designated {@link SignatureService}.
   */
  private void multiSigSendPaymentHelper(
    final PrivateKey sourcePrivateKey,
    final PrivateKey alicePrivateKey,
    final PrivateKey bobPrivateKey,
    final PrivateKey destinationPrivateKey
  ) throws JsonRpcClientErrorException, JsonProcessingException {

    Objects.requireNonNull(sourcePrivateKey);
    Objects.requireNonNull(alicePrivateKey);
    Objects.requireNonNull(bobPrivateKey);
    Objects.requireNonNull(destinationPrivateKey);

    /////////////////////////////
    // Wait for all accounts to show up in a validated ledger
    final AccountInfoResult sourceAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(toAddress(sourcePrivateKey))
    );
    scanForResult(() -> this.getValidatedAccountInfo(toAddress(alicePrivateKey)));
    scanForResult(() -> this.getValidatedAccountInfo(toAddress(bobPrivateKey)));
    scanForResult(() -> this.getValidatedAccountInfo(toAddress(destinationPrivateKey)));

    /////////////////////////////
    // And validate that the source account has not set up any signer lists
    assertThat(sourceAccountInfo.accountData().signerLists()).isEmpty();

    /////////////////////////////
    // Then submit a SignerListSet transaction to add alice and bob as signers on the account
    FeeResult feeResult = xrplClient.fee();
    SignerListSet signerListSet = SignerListSet.builder()
      .account(toAddress(sourcePrivateKey))
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sourceAccountInfo.accountData().sequence())
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(toAddress(alicePrivateKey))
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(toAddress(bobPrivateKey))
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .signingPublicKey(toPublicKey(sourcePrivateKey).base16Value())
      .build();

    SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      sourcePrivateKey, signerListSet
    );
    SubmitResult<SignerListSet> signerListSetResult = xrplClient.submit(signedSignerListSet);
    assertThat(signerListSetResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "SignerListSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      signerListSetResult.transactionResult().hash()
    );

    /////////////////////////////
    // Then wait until the transaction enters a validated ledger and the source account's signer list
    // exists
    AccountInfoResult sourceAccountInfoAfterSignerListSet = scanForResult(
      () -> this.getValidatedAccountInfo(toAddress(sourcePrivateKey)),
      infoResult -> infoResult.accountData().signerLists().size() == 1
    );

    assertThat(
      sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        .signerEntries().stream()
        .sorted(Comparator.comparing(entry -> entry.signerEntry().account()))
        .collect(Collectors.toList())
    ).isEqualTo(signerListSet.signerEntries().stream()
      .sorted(Comparator.comparing(entry -> entry.signerEntry().account()))
      .collect(Collectors.toList()));

    /////////////////////////////
    // Construct an unsigned Payment transaction to be multisigned
    Payment unsignedPayment = Payment.builder()
      .account(toAddress(sourcePrivateKey))
      .fee(
        FeeUtils.computeMultisigNetworkFees(
          feeResult,
          sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        ).recommendedFee()
      )
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(toAddress(destinationPrivateKey))
      .build();

    /////////////////////////////
    // Alice and Bob sign the transaction with their private keys using the "multiSign" method.
    Set<SignatureWithPublicKey> signers = Lists.newArrayList(alicePrivateKey, bobPrivateKey).stream()
      .map(privateKey -> {
          Signature signedPayment = signatureService.multiSign(privateKey, unsignedPayment);
          return SignatureWithPublicKey.builder()
            .signingPublicKey(toPublicKey(privateKey))
            .transactionSignature(signedPayment)
            .build();
        }
      )
      .collect(Collectors.toSet());

    /////////////////////////////
    // Then we add the signatures to the Payment object and submit it
    MultiSignedTransaction<Payment> multiSigPayment = MultiSignedTransaction.<Payment>builder()
      .unsignedTransaction(unsignedPayment)
      .signatureWithPublicKeySet(signers)
      .build();

    SubmitMultiSignedResult<Payment> paymentResult = xrplClient.submitMultisigned(multiSigPayment);
    assertThat(paymentResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/{}",
      paymentResult.transaction().hash()
    );
  }

  private PublicKey toPublicKey(final PrivateKey privateKey) {
    Objects.requireNonNull(privateKey);
    return signatureService.derivePublicKey(privateKey);
  }

  private Address toAddress(final PrivateKey privateKey) {
    return toPublicKey(privateKey).deriveAddress();
  }
}
