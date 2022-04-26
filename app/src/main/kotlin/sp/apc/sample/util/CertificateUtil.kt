package sp.apc.sample.util

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder
import org.bouncycastle.operator.bc.BcContentSignerBuilder
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder
import java.math.BigInteger
import java.security.PrivateKey
import java.util.Date
import java.util.Locale

object CertificateUtil {
    fun builder(
        issuer: X500Name,
        subject: X500Name,
        serial: BigInteger,
        notBefore: Long,
        validity: Long,
        locale: Locale,
        info: SubjectPublicKeyInfo
    ): X509v3CertificateBuilder {
        return X509v3CertificateBuilder(
            issuer,
            serial,
            Date(notBefore),
            Date(notBefore + validity),
            locale,
            subject,
            info
        )
    }
}

private object ContentSignerUtil {
    fun builder(algorithm: String): BcContentSignerBuilder {
        return when (algorithm) {
            "SHA1WITHRSA", "SHA224WITHRSA", "SHA384WITHRSA", "SHA512WITHRSA" -> {
                val sIdentifier = DefaultSignatureAlgorithmIdentifierFinder().find(algorithm)
                val dIdentifier = DefaultDigestAlgorithmIdentifierFinder().find(sIdentifier)
                BcRSAContentSignerBuilder(sIdentifier, dIdentifier)
            }
            else -> error("Algorithm $algorithm is not supported!")
        }
    }
}

fun X509v3CertificateBuilder.build(algorithm: String, key: PrivateKey): X509CertificateHolder {
    return build(ContentSignerUtil.builder(algorithm = algorithm).build(PrivateKeyFactory.createKey(key.encoded)))
}
