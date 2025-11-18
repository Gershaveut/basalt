package dev.code_offline.basalt_server;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

public class BasaltCertificateGenerator {
	private static final String FILE_NAME = "basalt.p12";
	
	private static final String ALIAS = "basalt";
	private static final char[] PASSWORD = "basalt".toCharArray();
	
	public static void generate() throws Exception {
		if (Files.exists(Path.of(FILE_NAME)))
			return;
		
		Security.addProvider(new BouncyCastleProvider());
		
		var keyGen = KeyPairGenerator.getInstance("RSA", "BC");
		keyGen.initialize(4096, new SecureRandom());
		
		var keyPair = keyGen.generateKeyPair();
		
		var name = new X500Name("CN=" + ALIAS);
		long now = System.currentTimeMillis();
		var certificateBuilder = new JcaX509v3CertificateBuilder(
				name,
				BigInteger.valueOf(now),
				new Date(now),
				new Date(253402300799000L),
				name,
				keyPair.getPublic()
		);
		
		var signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
		var certificate = new JcaX509CertificateConverter().getCertificate(certificateBuilder.build(signer));
		
		var keyStore = KeyStore.getInstance("PKCS12", "BC");
		keyStore.load(null, null);
		keyStore.setKeyEntry(ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{ certificate });
		
		try (FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME)) {
			keyStore.store(fileOutputStream, PASSWORD);
		}
	}
}
