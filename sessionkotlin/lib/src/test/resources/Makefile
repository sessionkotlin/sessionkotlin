
TRASH = *.pem *.key *.p12
.PHONY:
keys:
	openssl genrsa -out serverCA.key 4096
	openssl req -x509 -new -nodes -key serverCA.key -sha256 -days 99999 -subj "/C=AU/ST=Some-State/L=/O=/CN=" -out serverCA.pem
	openssl pkcs12 -export -name server-cert -in serverCA.pem -inkey serverCA.key -out serverkeystore.p12 -password pass:password
	openssl genrsa -out clientCA.key 4096
	openssl req -x509 -new -nodes -key clientCA.key -sha256 -days 99999 -subj "/C=AU/ST=Some-State/L=/O=/CN=" -out clientCA.pem
	openssl pkcs12 -export -name client-cert -in clientCA.pem -inkey clientCA.key -out clientkeystore.p12 -password pass:password

	keytool -importkeystore -destkeystore server_keystore.jks -srckeystore serverkeystore.p12 -srcstoretype pkcs12 -alias server-cert -srcstorepass password -deststorepass password
	keytool -import -alias client-cert -file clientCA.pem -keystore server_truststore.jks -deststorepass password -noprompt
	keytool -import -alias server-cert -file serverCA.pem -keystore server_truststore.jks -deststorepass password -noprompt
	keytool -importkeystore -destkeystore client_keystore.jks -srckeystore clientkeystore.p12 -srcstoretype pkcs12 -alias client-cert -srcstorepass password -deststorepass password
	keytool -import -alias server-cert -file serverCA.pem -keystore client_truststore.jks -deststorepass password -noprompt
	keytool -import -alias client-cert -file clientCA.pem -keystore client_truststore.jks -deststorepass password -noprompt

	rm $(TRASH)

.PHONY:
clean:
	rm -f *.jks $(TRASH)
