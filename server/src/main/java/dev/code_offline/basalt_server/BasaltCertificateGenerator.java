package dev.code_offline.basalt_server;

import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BasaltCertificateGenerator {
	private static final String FILE_NAME = "basalt.p12";
	
	private static final String ALIAS = "basalt";
	private static final char[] PASSWORD = "offline".toCharArray();
	private static final List<GeneralName> GENERAL_NAMES = new ArrayList<>(List.of(
			new GeneralName(GeneralName.dNSName, "localhost"),
			new GeneralName(GeneralName.iPAddress, "127.0.0.1"),
			new GeneralName(GeneralName.iPAddress, "::1")
	));
	
	public static void generate() throws Exception {
		if (Files.exists(Path.of(FILE_NAME)))
			return;
		
		var myIp = URI.create("http://checkip.amazonaws.com").toURL();
		var in = new BufferedReader(new InputStreamReader(myIp.openStream()));
		
		GENERAL_NAMES.add(new GeneralName(GeneralName.iPAddress, in.readLine()));
		
		in.close();
		
		try (var s = new Socket("8.8.8.8", 7601)) {
			GENERAL_NAMES.add(new GeneralName(GeneralName.iPAddress, s.getLocalAddress().getHostAddress()));
		}
		
		var keyGen = KeyPairGenerator.getInstance("RSA");
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
		
		certificateBuilder.addExtension(Extension.subjectAlternativeName, false, GeneralNames.getInstance(new DERSequence(GENERAL_NAMES.toArray(new GeneralName[0]))));
	
		var signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
		var certificate = new JcaX509CertificateConverter().getCertificate(certificateBuilder.build(signer));
		
		var keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(null, null);
		keyStore.setKeyEntry(ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{ certificate });
		
		try (FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME)) {
			keyStore.store(fileOutputStream, PASSWORD);
		}
	}
}
