package org.xrpl.xrpl4j.keypairs;

import com.google.common.collect.ImmutableMap;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;

import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@link KeyPairService} that delegates to specific {@link KeyPairService} implementations.
 */
public class DefaultKeyPairService extends AbstractKeyPairService {

  private static final KeyPairService INSTANCE = new DefaultKeyPairService();

  private static final Map<VersionType, Supplier<KeyPairService>> serviceMap =
      new ImmutableMap.Builder<VersionType, Supplier<KeyPairService>>()
          .put(VersionType.SECP256K1, Secp256k1KeyPairService::getInstance)
          .put(VersionType.ED25519, Ed25519KeyPairService::getInstance)
          .build();

  private static KeyPairService getKeyPairServiceByType(VersionType type) {
    return serviceMap.get(type).get();
  }

  public static KeyPairService getInstance() {
    return INSTANCE;
  }

  /**
   * Generates a seed from the given entropy, encoded with {@link VersionType#ED25519}. Seeds can be generated
   * using secp255k1, but all new seeds generated by this client use the ED25519 algorithm.
   *
   * @param entropy An {@link UnsignedByteArray} containing the bytes of entropy to encode into a seed.
   *
   * @return An ED25519 encoded 16 byte seed value, encoded in Base58Check.
   */
  @Override
  public String generateSeed(UnsignedByteArray entropy) {
    return addressCodec.encodeSeed(entropy, VersionType.ED25519);
  }

  @Override
  public KeyPair deriveKeyPair(String seed) {
    return addressCodec.decodeSeed(seed).type()
        .map(type -> DefaultKeyPairService.getKeyPairServiceByType(type).deriveKeyPair(seed))
        .orElseThrow(() -> new IllegalArgumentException("Unsupported seed type."));
  }

  @Override
  public String sign(UnsignedByteArray message, String privateKey) {
    // ED25519 keys are prefixed with "ED" to make them 33 bytes.
    VersionType privateKeyType = privateKey.startsWith("ED") ? VersionType.ED25519 : VersionType.SECP256K1;
    return DefaultKeyPairService.getKeyPairServiceByType(privateKeyType).sign(message, privateKey);
  }

  @Override
  public boolean verify(UnsignedByteArray message, String signature, String publicKey) {
    // ED25519 keys are prefixed with "ED" to make them 33 bytes.
    VersionType publicKeyType = publicKey.startsWith("ED") ? VersionType.ED25519 : VersionType.SECP256K1;
    return DefaultKeyPairService.getKeyPairServiceByType(publicKeyType).verify(message, signature, publicKey);
  }
}
